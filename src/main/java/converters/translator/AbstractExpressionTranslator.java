package converters.translator;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Expression;
import org.rebecalang.compiler.utils.ExceptionContainer;

public abstract class AbstractExpressionTranslator {

    protected ExceptionContainer container;

    public AbstractExpressionTranslator() {
        container = new ExceptionContainer();
    }

    public abstract String translate(Expression expression) throws ExpressionTranslationException;

    public void fillExceptionContainer(ExceptionContainer container) {
        container.addAll(this.container);
    }
}
