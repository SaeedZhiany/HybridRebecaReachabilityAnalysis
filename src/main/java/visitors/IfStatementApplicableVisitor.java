package visitors;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import stateSpace.ActorState;

public class IfStatementApplicableVisitor extends Visitor<Boolean> {
    public Boolean checkStatementApplicability(ActorState actorState) {
        Statement statement = actorState.getSigma().get(0);
        if (statement instanceof ConditionalStatement) {
            return this.visit((ConditionalStatement) statement);
        }
        if (statement instanceof BlockStatement) {
            return this.visit((BlockStatement) statement);
        }
        return false;
    }

    @Override
    public Boolean visit(ConditionalStatement conditionalStatement) {
        return true;
    }

    @Override
    public Boolean visit(BlockStatement blockStatement) {
        Statement statement = blockStatement.getStatements().get(0);
        if (statement instanceof ConditionalStatement) {
            return this.visit((ConditionalStatement) statement);
        } else if (statement instanceof BlockStatement) {
            return this.visit((BlockStatement) statement);
        }
        return false;
    }
}
