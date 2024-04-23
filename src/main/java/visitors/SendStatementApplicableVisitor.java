package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.DotPrimary;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import stateSpace.ActorState;
import stateSpace.HybridState;

import java.util.List;

public class SendStatementApplicableVisitor extends Visitor<Boolean> {
    public Boolean checkStatementApplicability(ActorState actorState) {
        List<Statement> statements = actorState.getSigma();
        if (statements instanceof DotPrimary) {
            return this.visit((DotPrimary) statements);
        }
        return false;
    }

    @Override
    public Boolean visit(DotPrimary dotPrimary) {
        return true;
    }
}
