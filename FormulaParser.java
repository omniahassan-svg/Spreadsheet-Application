import java.util.*;

class FormulaParser {

    public ParsedFormula parse(String raw) {
        if (raw == null || !raw.startsWith("="))
            throw new IllegalArgumentException("Formula must start with '='");

        String expr = raw.substring(1);

        FormulaTokenizer tokenizer = new FormulaTokenizer(expr);
        List<Token> infix = tokenizer.tokenize();

        ShuntingYardConverter converter = new ShuntingYardConverter();
        List<Token> postfix = converter.toPostfix(infix);

        Set<String> refs = new HashSet<>();
        for (Token t : infix) {
            if (t.type == TokenType.CELL_REF) {
                refs.add(t.text);
            }
        }

        return new ParsedFormula(postfix, refs);
    }
}


