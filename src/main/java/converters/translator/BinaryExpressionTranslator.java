package converters.translator;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.BinaryExpression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BinaryExpressionTranslator extends AbstractExpressionTranslator {

    private List<String> arithmeticOperators = new ArrayList<String>() {{
        add("+");
        add("-");
        add("*");
        add("/");
    }};

    // TODO make sure this mapping is true
    private HashMap<String, String> binaryLogicalOperatorMappings = new HashMap<String, String>(){{
        put("==","=");
        put("!=","!=");
        put(">=",">=");
        put("<=","<=");
        put(">",">");
        put("<","<");
        put("&&"," ");
        put("||","||");
    }};

    @Override
    public String translate(Expression expression) throws ExpressionTranslationException {
        final BinaryExpression bExpression = (BinaryExpression) expression;
        final String left = ExpressionTranslatorContainer.translate(bExpression.getLeft());
        String op = bExpression.getOperator();
        final String right = ExpressionTranslatorContainer.translate(bExpression.getRight());
        final boolean isLeftANumber = Utils.isNumber(left);
        final boolean isRightANumber = Utils.isNumber(right);
        if (isLeftANumber && isRightANumber && arithmeticOperators.contains(op)) {
            return Utils.manipulateArithmeticExpression(left, op, right);
        } else if (binaryLogicalOperatorMappings.containsKey(op)) {
            op = binaryLogicalOperatorMappings.get(op);
        }
        return "(" + left + " " + op + " " + right + ")";
    }

}
