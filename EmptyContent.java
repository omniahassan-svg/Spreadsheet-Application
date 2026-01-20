class EmptyContent extends CellContent {

    @Override public String getRaw() { return ""; }

    @Override public String getType() { return "EMPTY"; }

    @Override public double getNumericValue(Spreadsheet sheet) { return 0.0; }

    @Override public String getTextValue(Spreadsheet sheet) { return ""; }
}

