package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.BlockStatement;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.DotPrimary;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import stateSpace.ActorState;
import stateSpace.HybridState;

import java.util.List;

public class SendStatementApplicableVisitor extends Visitor<Boolean> {
    public Boolean checkStatementApplicability(ActorState actorState) {
        Statement statement = actorState.getSigma().get(0);
        if (statement instanceof DotPrimary) {
            return this.visit((DotPrimary) statement);
        }
        if (statement instanceof BlockStatement) {
            return this.visit((BlockStatement) statement);
        }
        return false;
    }

    @Override
    public Boolean visit(DotPrimary dotPrimary) {
        return true;
    }

    @Override
    public Boolean visit(BlockStatement blockStatement) {
        Statement statement = blockStatement.getStatements().get(0);
        if (statement instanceof DotPrimary) {
            return this.visit((DotPrimary) statement);
        } else if (statement instanceof BlockStatement) {
            return this.visit((BlockStatement) statement);
        }
        return false;
    }
}
