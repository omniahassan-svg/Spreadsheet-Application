import java.util.*;

class Spreadsheet {

    private final SpreadsheetStorage storage;
    private final Map<String, Set<String>> dependsOn = new HashMap<>();
    private final Map<String, Set<String>> dependents = new HashMap<>();

    public Spreadsheet(int rows, int cols) {
        this.storage = new ArrayStorage(rows, cols);
    }

    // ---------- Dependency handling ----------


    public void setCellFromLoad(int row, int col, CellContent content) {
        String coord = indicesToCoord(row, col);

        Set<String> newDeps = Set.of();
        if (content instanceof FormulaContent fc) {
            newDeps = new HashSet<>(expandDependencies(fc.getParsed().referencedCells));

        }

        removeOldDependencies(coord);
        addNewDependencies(coord, newDeps);


        storage.setCell(row - 1, col - 1, new Cell(content));
    }

    void clearAllFormulaCaches() {
        for (Cell[] row : storage.rowsIterable()) {
            for (Cell c : row) {
                if (c.getContent() instanceof FormulaContent fc) {
                    fc.clearCache();
                }
            }
        }
    }






    public void setCell(int row, int col, CellContent content) {
        String coord = indicesToCoord(row, col);

        Set<String> newDeps = Set.of();
        if (content instanceof FormulaContent fc) {
            newDeps = expandDependencies(fc.getParsed().referencedCells);

            if (wouldCreateCycle(coord, newDeps))
                throw new IllegalStateException("Circular dependency detected");
        }

        removeOldDependencies(coord);
        addNewDependencies(coord, newDeps);

        storage.setCell(row - 1, col - 1, new Cell(content));
        invalidate(coord);
    }

    private boolean wouldCreateCycle(String cell, Set<String> newDeps) {
        for (String dep : newDeps) {
            if (detectCycle(cell, dep, new HashSet<>())) return true;
        }
        return false;
    }

    private boolean detectCycle(String start, String current, Set<String> visited) {
        if (start.equals(current)) return true;
        if (!visited.add(current)) return false;
        for (String d : dependsOn.getOrDefault(current, Set.of())) {
            if (detectCycle(start, d, visited)) return true;
        }
        return false;
    }

    private void removeOldDependencies(String coord) {
        for (String dep : dependsOn.getOrDefault(coord, Set.of())) {
            dependents.getOrDefault(dep, Set.of()).remove(coord);
        }
        dependsOn.remove(coord);
    }

    private void addNewDependencies(String coord, Set<String> newDeps) {
        if (newDeps.isEmpty()) return;
        dependsOn.put(coord, new HashSet<>(newDeps));
        for (String dep : newDeps) {
            dependents.computeIfAbsent(dep, k -> new HashSet<>()).add(coord);
        }
    }

    private void invalidate(String coord) {
        //System.out.println("Invalidating: " + coord);
        CellContent cc = getCell(coord).getContent();
        if (cc instanceof FormulaContent fc) fc.clearCache();
        for (String d : dependents.getOrDefault(coord, Set.of())) {
            invalidate(d);
        }
    }

    // ---------- Access ----------

    public Cell getCell(int row, int col) {
        return storage.getCell(row - 1, col - 1);
    }

    public Cell getCell(String coord) {
        int[] rc = coordToIndices(coord);
        return getCell(rc[0], rc[1]);
    }

    // ---------- Utilities ----------

    static int[] coordToIndices(String coord) {
        int i = 0;
        while (i < coord.length() && Character.isLetter(coord.charAt(i))) i++;
        int col = colLabelToIndex(coord.substring(0, i));
        int row = Integer.parseInt(coord.substring(i));
        return new int[]{row, col};
    }

    private static int colLabelToIndex(String label) {
        int res = 0;
        for (char ch : label.toCharArray())
            res = res * 26 + (ch - 'A' + 1);
        return res;
    }

    private static String indicesToCoord(int row, int col) {
        StringBuilder sb = new StringBuilder();
        while (col > 0) {
            col--;
            sb.insert(0, (char) ('A' + col % 26));
            col /= 26;
        }
        return sb + Integer.toString(row);
    }

    public void printRawRegion(int r1, int c1, int r2, int c2) {
        int rr1 = Math.min(r1, r2);
        int rr2 = Math.max(r1, r2);
        int cc1 = Math.min(c1, c2);
        int cc2 = Math.max(c1, c2);

        if (rr1 < 1 || rr2 > storage.getRowCount()
                || cc1 < 1 || cc2 > storage.getColCount()) {
            throw new IndexOutOfBoundsException("Region out of bounds");
        }

        for (int r = rr1; r <= rr2; r++) {
            for (int c = cc1; c <= cc2; c++) {
                CellContent content = getCell(r, c).getContent();
                String raw = content.getRaw();
                if (raw == null || raw.isEmpty()) raw = "(empty)";
                System.out.print("[" + raw + "]");
                if (c < cc2) System.out.print("\t");
            }
            System.out.println();
        }
    }


    // ---------- Load recompute ----------

    /*void recomputeAll() {
        for (Cell[] row : storage.rowsIterable())
            for (Cell c : row)
                if (c.getContent() instanceof FormulaContent fc)
                    fc.getNumericValue(this);
    }*/

    void recomputeAll() {
        clearAllFormulaCaches();
        for (String coord : dependsOn.keySet()) {
            getCell(coord).getContent().getNumericValue(this);
        }
    }

    private Set<String> expandDependencies(Set<String> refs) {
        Set<String> expanded = new HashSet<>();

        for (String ref : refs) {
            if (!ref.contains(":")) {
                expanded.add(ref);
                continue;
            }

            // Expand range A1:B2 → A1,A2,B1,B2
            String[] parts = ref.split(":");
            int[] start = coordToIndices(parts[0]);
            int[] end   = coordToIndices(parts[1]);

            int rFrom = Math.min(start[0], end[0]);
            int rTo   = Math.max(start[0], end[0]);
            int cFrom = Math.min(start[1], end[1]);
            int cTo   = Math.max(start[1], end[1]);

            for (int r = rFrom; r <= rTo; r++) {
                for (int c = cFrom; c <= cTo; c++) {
                    expanded.add(indicesToCoord(r, c));
                }
            }
        }

        return expanded;
    }



    // internal helpers for serializer
    int getInternalRowCount() { return storage.getRowCount(); }
    int getInternalColCount() { return storage.getColCount(); }
    Cell getCellInternal(int r, int c) { return storage.getCell(r, c); }
    void setCellInternal(int r, int c, Cell cell) { storage.setCell(r, c, cell); }
}

