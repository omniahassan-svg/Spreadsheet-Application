import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class PostfixEvaluator {

    private final Spreadsheet sheet;
    private static final List<Double> ARG_MARK = List.of();


    public PostfixEvaluator(Spreadsheet sheet) {
        this.sheet = sheet;
    }

    private double parseNumber(Token token) {
        try {
            return Double.parseDouble(token.text);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid numeric literal: " + token.text
            );
        }
    }



    private void applyFunction(Token token, Deque<List<Double>> stack) {
        List<Double> allValues = new ArrayList<>();

        while (!stack.isEmpty()) {
            List<Double> top = stack.pop();


            if (top == ARG_MARK) {
                break;
            }


            allValues.addAll(0, top);
        }

        if (allValues.isEmpty()) {
            throw new IllegalStateException(
                    "Function " + token.text + " has no arguments"
            );
        }

        double result;
        String name = token.text.toUpperCase();

        switch (name) {
            case "SUM":
                result = allValues.stream().mapToDouble(Double::doubleValue).sum();
                break;
            case "MIN":
                result = allValues.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
                break;
            case "MAX":
                result = allValues.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
                break;
            case "MEAN":
                result = allValues.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
                break;
            default:
                throw new IllegalStateException("Unknown function: " + name);
        }

        stack.push(List.of(result));
    }





    private void applyOperator(Token token, Deque<List<Double>> stack) {

        if (stack.size() < 2) {
            throw new IllegalStateException("Operator " + token.text + " missing operands");
        }

        List<Double> b = stack.pop();
        List<Double> a = stack.pop();

        if (a.size() != 1 || b.size() != 1) {
            throw new IllegalStateException("Operators require scalar values");
        }

        double x = a.get(0);
        double y = b.get(0);
        double result;

        switch (token.text) {
            case "+": result = x + y; break;
            case "-": result = x - y; break;
            case "*": result = x * y; break;
            case "/":
                if (y == 0) throw new IllegalStateException("Division by zero");
                result = x / y;
                break;
            default:
                throw new IllegalStateException("Unknown operator");
        }

        stack.push(List.of(result));
    }




    private List<Double> resolveCellReference(Token token) {
        if (!token.text.contains(":")) {
            int[] rc = Spreadsheet.coordToIndices(token.text);
            Cell cell = sheet.getCell(rc[0], rc[1]);
            return List.of(cell.getContent().getNumericValue(sheet));
        }
        return expandRange(token.text);
    }

    private List<Double> expandRange(String range) {
        String[] parts = range.split(":");
        int[] start = Spreadsheet.coordToIndices(parts[0]);
        int[] end = Spreadsheet.coordToIndices(parts[1]);

        int startRow = start[0];
        int endRow   = end[0];
        int startCol = start[1];
        int endCol   = end[1];

        int rFrom = Math.min(startRow, endRow);
        int rTo   = Math.max(startRow, endRow);
        int cFrom = Math.min(startCol, endCol);
        int cTo   = Math.max(startCol, endCol);


        List<Double> values = new ArrayList<>();

        for (int r = rFrom; r <= rTo; r++) {
            for (int c = cFrom; c <= cTo; c++) {
                values.add(
                        sheet.getCell(r, c)
                                .getContent()
                                .getNumericValue(sheet)
                );
            }
        }

        return values;
    }



    private double evaluateRange(String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("Invalid range: " + range);
        }

        int[] start = Spreadsheet.coordToIndices(parts[0]);
        int[] end = Spreadsheet.coordToIndices(parts[1]);

        if (start == null || end == null) {
            throw new IllegalStateException("Invalid range: " + range);
        }

        int r1 = Math.min(start[0], end[0]);
        int r2 = Math.max(start[0], end[0]);
        int c1 = Math.min(start[1], end[1]);
        int c2 = Math.max(start[1], end[1]);

        double sum = 0.0;
        int count = 0;

        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                sum += sheet.getCell(r, c).getContent().getNumericValue(sheet);
                count++;
            }
        }

        return sum;
    }



    public double evaluate(List<Token> postfixTokens) {
        Deque<List<Double>> stack = new ArrayDeque<>();

        for (Token token : postfixTokens) {
            switch (token.type) {
                case NUMBER:
                    stack.push(List.of(Double.parseDouble(token.text)));
                    break;

                case CELL_REF:
                    stack.push(resolveCellReference(token));
                    break;

                case OPERATOR:
                    applyOperator(token, stack);
                    break;

                case FUNCTION:
                    applyFunction(token, stack);
                    break;

                case ARG_MARKER:
                    stack.push(ARG_MARK);
                    break;


                default:
                    throw new IllegalStateException(
                            "Unsupported token in postfix evaluation: " + token
                    );
            }

        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid formula: leftover values");
        }

        List<Double> result = stack.pop();

        if (result.size() != 1) {
            throw new IllegalStateException("Formula did not reduce to a single value");
        }

        return result.get(0);
    }

}
