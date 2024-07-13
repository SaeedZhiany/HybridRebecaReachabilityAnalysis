package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import stateSpace.HybridState;

public interface IVisitor<T> {

    public T visit(DotPrimary dotPrimary);
    public T visit(UnaryExpression unaryExpression);
    public T visit(BinaryExpression binaryExpression);
    public T visit(TermPrimary termPrimary);
    public T visit(Literal literal);
    public T visit(BlockStatement blockStatement);
    public T visit(FieldDeclaration fieldDeclaration);

}
