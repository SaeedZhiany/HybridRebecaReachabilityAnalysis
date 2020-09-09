package converters.translator;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.UnaryExpression;

public class UnaryExpressionTranslator extends AbstractExpressionTranslator {

    public String translate(Expression expression) throws ExpressionTranslationException {
        UnaryExpression uExpression = (UnaryExpression) expression;
        final String op = uExpression.getOperator();
        final String right = ExpressionTranslatorContainer.translate(uExpression.getExpression());
        
        if ((op.equals("+") || op.equals("-")) && (right.startsWith("+") || right.startsWith("-"))) {
            final String aggregatedOp = op.charAt(0) == right.charAt(0) ? "+" : "-";
            return aggregatedOp + " " + right.substring(1);
        }
        return op + " " + right;
    }

}
