class TextContent extends CellContent {

    private final String text;

    public TextContent(String text) {
        this.text = text == null ? "" : text;
    }

    @Override public String getRaw() { return text; }

    @Override public String getType() { return "TEXT"; }

    @Override
    public double getNumericValue(Spreadsheet sheet) {
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Text is not numeric: " + text);
        }
    }

    @Override public String getTextValue(Spreadsheet sheet) { return text; }
}

