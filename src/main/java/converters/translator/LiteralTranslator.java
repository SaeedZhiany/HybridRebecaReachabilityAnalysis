package converters.translator;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Literal;
import utils.Utils;

public class LiteralTranslator extends AbstractExpressionTranslator {

    public String translate(Expression expression) throws ExpressionTranslationException {
        final String literalValue = ((Literal) expression).getLiteralValue();
        if (Utils.isNumber(literalValue)) {
            return Utils.removeEndLetter(literalValue);
        }
        return literalValue;
    }
}