class FormulaContent extends CellContent {

    private final String raw;
    private final ParsedFormula parsed;
    private Double cachedValue;

    public FormulaContent(String raw) {
        this.raw = raw == null ? "" : raw;
        this.parsed = new FormulaParser().parse(this.raw);
    }

    ParsedFormula getParsed() {
        return parsed;
    }

    void clearCache() {
        cachedValue = null;
    }

    @Override public String getRaw() { return raw; }

    @Override public String getType() { return "FORMULA"; }

    @Override
    public double getNumericValue(Spreadsheet sheet) {
        if (cachedValue != null) return cachedValue;

        PostfixEvaluator evaluator = new PostfixEvaluator(sheet);
        cachedValue = evaluator.evaluate(parsed.postfix);
        return cachedValue;
    }

    @Override
    public String getTextValue(Spreadsheet sheet) {
        return Double.toString(getNumericValue(sheet));
    }
}
