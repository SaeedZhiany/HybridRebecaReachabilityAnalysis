package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.BinaryExpression;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.BlockStatement;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.DotPrimary;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import stateSpace.ActorState;

import java.util.List;

public class AssignmentStatementApplicableVisitor extends Visitor<Boolean> {

    public Boolean checkStatementApplicability(ActorState actorState) {
        List<Statement> statements = actorState.getSigma();
        if (statements instanceof BinaryExpression) {
            return this.visit((BinaryExpression) statements);
        }
        if (statements instanceof BlockStatement) {
            return this.visit((BlockStatement) statements);
        }
        return false;
    }

    @Override
    public Boolean visit(BinaryExpression binaryExpression) {
        return binaryExpression.getOperator().equals("=");
    }

    @Override
    public Boolean visit(BlockStatement blockStatement) {
        Statement statement = blockStatement.getStatements().get(0);
        if (statement instanceof BinaryExpression) {
            return this.visit((BinaryExpression) statement);
        } else if (statement instanceof BlockStatement) {
            return this.visit((BlockStatement) statement);
        }
        return false;
    }
}
