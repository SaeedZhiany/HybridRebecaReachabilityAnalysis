package converters.translator;

import org.rebecalang.compiler.utils.CodeCompilationException;

@SuppressWarnings("serial")
public class ExpressionTranslationException extends CodeCompilationException {

	public ExpressionTranslationException(String message, int line, int column) {
		super(message, line, column);
	}
}
