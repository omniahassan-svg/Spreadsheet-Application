import java.util.List;
import java.util.Set;

final class ParsedFormula {
    final List<Token> postfix;
    final Set<String> referencedCells;

    ParsedFormula(List<Token> postfix, Set<String> referencedCells) {
        this.postfix = postfix;
        this.referencedCells = referencedCells;
    }
}


