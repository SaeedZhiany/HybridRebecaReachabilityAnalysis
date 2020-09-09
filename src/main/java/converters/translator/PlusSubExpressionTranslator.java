package converters.translator;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.PlusSubExpression;

public class PlusSubExpressionTranslator extends AbstractExpressionTranslator {

    public String translate(Expression expression) throws ExpressionTranslationException {
        PlusSubExpression pspExpression = (PlusSubExpression) expression;
        return "(" + ExpressionTranslatorContainer.translate(pspExpression.getValue()) + pspExpression.getOperator() + ")";
    }
}