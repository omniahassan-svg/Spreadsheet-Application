class NumericContent extends CellContent {

    private final double value;

    public NumericContent(double value) {
        this.value = value;
    }

    @Override public String getRaw() { return Double.toString(value); }

    @Override public String getType() { return "NUMERIC"; }

    @Override public double getNumericValue(Spreadsheet sheet) { return value; }

    @Override public String getTextValue(Spreadsheet sheet) {
        return Double.toString(value);
    }
}
