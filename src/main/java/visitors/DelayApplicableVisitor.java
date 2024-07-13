package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.TermPrimary;
import stateSpace.ActorState;

public class DelayApplicableVisitor extends Visitor<Boolean> {
    public Boolean checkStatementApplicability(ActorState actorState) {
        Statement statement = actorState.getSigma().get(0);
        if (statement instanceof TermPrimary) {
            return this.visit((TermPrimary) statement);
        }
        return false;
    }

    @Override
    public Boolean visit(TermPrimary termPrimary) {
        return termPrimary.getName().equals("delay");
    }
}