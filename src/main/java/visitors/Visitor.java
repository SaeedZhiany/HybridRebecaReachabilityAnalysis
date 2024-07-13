package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;

public class Visitor<T> implements IVisitor<T>{

    @Override
    public T visit(DotPrimary dotPrimary) {
        return null;
    }

    @Override
    public T visit(UnaryExpression unaryExpression) {
        return null;
    }

    @Override
    public T visit(BinaryExpression binaryExpression) {
        return null;
    }

    @Override
    public T visit(TermPrimary termPrimary) {
        return null;
    }

    @Override
    public T visit(Literal literal) {
        return null;
    }

    @Override
    public T visit(BlockStatement blockStatement) {
        return null;
    }

    @Override
    public T visit(FieldDeclaration fieldDeclaration) {
        return null;
    }
}
