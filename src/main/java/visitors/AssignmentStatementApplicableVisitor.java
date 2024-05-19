package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.BinaryExpression;
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
        return false;
    }

    @Override
    public Boolean visit(BinaryExpression binaryExpression) {
        return binaryExpression.getOperator().equals("=");
    }
}
