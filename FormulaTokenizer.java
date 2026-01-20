import java.util.ArrayList;
import java.util.List;

class FormulaTokenizer {

    private final String input;
    private int pos;


    public FormulaTokenizer(String input) {
        this.input = input;
        this.pos = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char ch = input.charAt(pos);

            // Skip whitespace
            if (Character.isWhitespace(ch)) {
                pos++;
                continue;
            }

            // Number
            if (Character.isDigit(ch) || ch == '.') {
                tokens.add(readNumber());
                continue;
            }

            // Letter: function or cell reference
            if (Character.isLetter(ch)) {
                tokens.add(readWord());
                continue;
            }

            // Operators
            if (isOperator(ch)) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(ch)));
                pos++;
                continue;
            }

            // Parentheses and separators
            switch (ch) {
                case '(':
                    tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                    pos++;
                    break;
                case ';':
                    tokens.add(new Token(TokenType.ARG_SEPARATOR, ";"));
                    pos++;
                    break;
                /*case ':':
                    tokens.add(new Token(TokenType.RANGE_SEPARATOR, ":"));
                    pos++;
                    break;*/
                default:
                    throw new IllegalStateException(
                            "Unexpected character '" + ch + "' at position " + pos
                    );
            }
        }

        return tokens;
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private Token readNumber() {
        int start = pos;
        boolean hasDot = false;

        while (pos < input.length()) {
            char ch = input.charAt(pos);
            if (Character.isDigit(ch)) {
                pos++;
            } else if (ch == '.' && !hasDot) {
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }

        String text = input.substring(start, pos);
        return new Token(TokenType.NUMBER, text);
    }

    private Token readWord() {
        int start = pos;


        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            pos++;
        }

        String letters = input.substring(start, pos).toUpperCase();


        int digitStart = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }


        if (digitStart != pos) {
            String cell = letters + input.substring(digitStart, pos);


            if (pos < input.length() && input.charAt(pos) == ':') {
                pos++; // skip ':'

                int rangeStart = pos;


                while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
                    pos++;
                }
                if (rangeStart == pos) {
                    throw new IllegalStateException("Invalid range syntax");
                }


                int rangeDigitStart = pos;
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    pos++;
                }
                if (rangeDigitStart == pos) {
                    throw new IllegalStateException("Invalid range syntax");
                }

                String cell2 = input.substring(rangeStart, pos).toUpperCase();
                return new Token(TokenType.CELL_REF, cell + ":" + cell2);
            }

            return new Token(TokenType.CELL_REF, cell);
        }


        return new Token(TokenType.FUNCTION, letters);
    }


}
