package visitors;

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
        return false;
    }

    @Override
    public Boolean visit(DotPrimary dotPrimary) {
        return true;
    }
}
