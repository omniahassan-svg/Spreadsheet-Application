abstract class CellContent {

    public abstract String getRaw();

    public boolean isEmpty() {
        String r = getRaw();
        return r == null || r.isEmpty();
    }

    public abstract String getType();

    public abstract double getNumericValue(Spreadsheet sheet);

    public abstract String getTextValue(Spreadsheet sheet);
}

