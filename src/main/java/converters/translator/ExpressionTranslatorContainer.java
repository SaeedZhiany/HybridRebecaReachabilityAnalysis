package converters.translator;

import dataStructure.DiscreteVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.TermPrimary;
import org.rebecalang.compiler.utils.ExceptionContainer;

import java.util.HashMap;
import java.util.Hashtable;

public class ExpressionTranslatorContainer {

    private static Hashtable<Class<? extends Expression>, AbstractExpressionTranslator> translatorsRepository =
            new Hashtable<>();

    static {
        translatorsRepository.put(Expression.class, getEmptyTranslator());
    }

    private static EmptyExpressionTranslator getEmptyTranslator() {
        return new EmptyExpressionTranslator();
    }

    public static void clearTranslator() {
        translatorsRepository = new Hashtable<>();
        translatorsRepository.put(Expression.class, getEmptyTranslator());
    }

    public static ExceptionContainer getExceptions() {
        ExceptionContainer container = new ExceptionContainer();

        for (AbstractExpressionTranslator translator : translatorsRepository.values()) {
            translator.fillExceptionContainer(container);
        }

        return container;
    }

    public static void registerTranslator(Class<? extends Expression> type, AbstractExpressionTranslator translator) {
        translatorsRepository.put(type, translator);
    }

    public static void unregisterTranslator(Class<? extends Expression> type) {
        translatorsRepository.remove(type);
    }

    public static AbstractExpressionTranslator getTranslator(Class<? extends Expression> type) {
        return translatorsRepository.get(type);
    }

    static String translate(Expression expression) throws ExpressionTranslationException {
        try {
            return translatorsRepository.get(expression.getClass()).translate(expression);
        } catch (NullPointerException e) {
            throw new ExpressionTranslationException("Unknown translator for expression of type \"" +
                    expression.getClass() + "\".", expression.getLineNumber(), expression.getCharacter());
        }
    }

    public static String translate(Expression expression, HashMap<String, DiscreteVariable> discreteVariables)
            throws ExpressionTranslationException {
        if (translatorsRepository.containsKey(TermPrimary.class)) {
            ((TermPrimaryExpressionTranslator) translatorsRepository.get(TermPrimary.class)).initialize(discreteVariables);
        }
        return translate(expression);
    }

    private static class EmptyExpressionTranslator extends AbstractExpressionTranslator {
        public String translate(Expression expression) {
            return "";
        }
    }
}
