package converters.translator;

import dataStructure.DiscreteVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.TermPrimary;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class TermPrimaryExpressionTranslator extends AbstractExpressionTranslator {

    @Nonnull
    HashMap<String, DiscreteVariable> discreteVariables = new HashMap<>();

    public void initialize(@Nonnull HashMap<String, DiscreteVariable> discreteVariables) {
        this.discreteVariables = discreteVariables;
    }

    public String translate(Expression expression) throws ExpressionTranslationException {
        TermPrimary termPrimary = (TermPrimary) expression;
        if (termPrimary.getLabel().getName().equals("state-variable") &&
                discreteVariables.containsKey(termPrimary.getName())) {
            return discreteVariables.get(termPrimary.getName()).getValue().toString();
        }
        return termPrimary.getName();
    }
}
