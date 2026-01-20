import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public final class S2VSerializer {

    public void save(Spreadsheet sheet, Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            int rows = sheet.getInternalRowCount();
            int cols = sheet.getInternalColCount();
            for (int r = 0; r < rows; r++) {
                int last = -1;
                for (int c = cols - 1; c >= 0; c--) {
                    if (!sheet.getCellInternal(r, c).getContent().isEmpty()) { last = c; break; }
                }
                if (last == -1) { w.write(""); w.newLine(); continue; }
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c <= last; c++) {
                    if (c > 0) sb.append(';');
                    CellContent cc = sheet.getCellInternal(r, c).getContent();
                    String raw = cc.getRaw();
                    if (raw == null) raw = "";
                    if (cc instanceof FormulaContent) raw = convertSemicolonToCommaInParens(raw);
                    sb.append(raw);
                }
                w.write(sb.toString());
                w.newLine();
            }
        }
    }

    public void load(Spreadsheet sheet, Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int rows = sheet.getInternalRowCount();
        int cols = sheet.getInternalColCount();
        for (int r = 0; r < rows; r++) {
            String line = r < lines.size() ? lines.get(r) : "";
            String[] parts = line.split(";", -1);
            for (int c = 0; c < cols; c++) {
                String raw = c < parts.length ? parts[c] : "";
                if (raw == null) raw = "";
                if (raw.startsWith("=")) raw = convertCommaToSemicolonInParens(raw);
                CellContent cc;
                if (raw.startsWith("=")) cc = new FormulaContent(raw);
                else {
                    try { double v = Double.parseDouble(raw); cc = new NumericContent(v); }
                    catch (NumberFormatException ex) { cc = new TextContent(raw); }
                }
                sheet.setCellFromLoad(r + 1, c + 1, cc);

            }
        }
    }


    private static String convertSemicolonToCommaInParens(String s) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (char ch : s.toCharArray()) {
            if (ch == '(') { depth++; sb.append(ch); continue; }
            if (ch == ')') { depth = Math.max(0, depth - 1); sb.append(ch); continue; }
            if (ch == ';' && depth > 0) sb.append(','); else sb.append(ch);
        }
        return sb.toString();
    }

    private static String convertCommaToSemicolonInParens(String s) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (char ch : s.toCharArray()) {
            if (ch == '(') { depth++; sb.append(ch); continue; }
            if (ch == ')') { depth = Math.max(0, depth - 1); sb.append(ch); continue; }
            if (ch == ',' && depth > 0) sb.append(';'); else sb.append(ch);
        }
        return sb.toString();
    }
}
