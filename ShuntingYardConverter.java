import java.util.*;

class ShuntingYardConverter {

    public List<Token> toPostfix(List<Token> infixTokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> operatorStack = new ArrayDeque<>();

        for (Token token : infixTokens) {

            switch (token.type) {

                case NUMBER:
                case CELL_REF:
                    // Operands go directly to output
                    output.add(token);
                    break;

                case FUNCTION:
                    operatorStack.push(token);

                    output.add(new Token(TokenType.ARG_MARKER, "|"));
                    break;


                case ARG_SEPARATOR:
                    // Pop operators until '('
                    while (!operatorStack.isEmpty()
                            && operatorStack.peek().type != TokenType.LEFT_PAREN) {
                        output.add(operatorStack.pop());
                    }
                    if (operatorStack.isEmpty()) {
                        throw new IllegalStateException("Misplaced ';' in function call");
                    }
                    break;


                case OPERATOR:
                    while (!operatorStack.isEmpty()
                            && operatorStack.peek().type == TokenType.OPERATOR
                            && precedence(operatorStack.peek()) >= precedence(token)) {

                        output.add(operatorStack.pop());
                    }
                    operatorStack.push(token);
                    break;

                case LEFT_PAREN:
                    operatorStack.push(token);
                    break;

                case RIGHT_PAREN:
                    while (!operatorStack.isEmpty()
                            && operatorStack.peek().type != TokenType.LEFT_PAREN) {
                        output.add(operatorStack.pop());
                    }

                    if (operatorStack.isEmpty()) {
                        throw new IllegalStateException("Mismatched parentheses");
                    }

                    operatorStack.pop(); // remove '('

                    if (!operatorStack.isEmpty()
                            && operatorStack.peek().type == TokenType.FUNCTION) {
                        output.add(operatorStack.pop()); // just emit function
                    }

                    break;


                default:
                    throw new IllegalStateException(
                            "Unsupported token in shunting yard: " + token
                    );
            }
        }

        while (!operatorStack.isEmpty()) {
            Token t = operatorStack.pop();
            if (t.type == TokenType.LEFT_PAREN || t.type == TokenType.RIGHT_PAREN) {
                throw new IllegalStateException("Mismatched parentheses");
            }
            output.add(t);
        }

        return output;
    }

    private int precedence(Token token) {
        if (token.text.equals("*") || token.text.equals("/")) return 2;
        if (token.text.equals("+") || token.text.equals("-")) return 1;
        throw new IllegalStateException("Unknown operator: " + token.text);
    }
}