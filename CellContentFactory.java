class CellContentFactory {

    static CellContent createText(String s) {
        return new TextContent(s);
    }

    static CellContent createNumeric(String s) {
        return new NumericContent(Double.parseDouble(s));
    }

    static CellContent createFormula(String s) {
        return new FormulaContent(s);
    }
}
