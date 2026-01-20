import java.util.ArrayList;
import java.util.List;


public class ArrayStorage implements SpreadsheetStorage {
    private Cell[][] grid;
    public ArrayStorage(int rows, int cols) {
        if (rows <= 0 || cols <= 0) throw new IllegalArgumentException("rows/cols must be positive");
        grid = new Cell[rows][cols];
        for (int r=0;r<rows;r++) for (int c=0;c<cols;c++) grid[r][c] = new Cell(new EmptyContent());
    }

    @Override public int getRowCount() { return grid.length; }
    @Override public int getColCount() { return grid[0].length; }

    @Override
    public Cell getCell(int row0, int col0) {
        checkBounds(row0, col0);
        return grid[row0][col0];
    }

    @Override
    public void setCell(int row0, int col0, Cell cell) {
        checkBounds(row0, col0);
        grid[row0][col0] = (cell == null ? new Cell(new EmptyContent()) : cell);
    }

    @Override
    public Iterable<Cell[]> rowsIterable() {
        List<Cell[]> rows = new ArrayList<>(grid.length);
        for (Cell[] row : grid) rows.add(row);
        return rows;
    }

    private void checkBounds(int r, int c) {
        if (r < 0 || r >= getRowCount() || c < 0 || c >= getColCount())
            throw new IndexOutOfBoundsException("Index out of bounds: " + r + "," + c);
    }
}
