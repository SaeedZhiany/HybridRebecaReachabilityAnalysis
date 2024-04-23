package visitors;

import com.rits.cloning.Cloner;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.DotPrimary;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.TermPrimary;
import stateSpace.ActorState;
import stateSpace.HybridState;

import java.util.List;

public class SendStatementExecutorVisitor extends Visitor<HybridState> {
    private HybridState hybridState;
    private ActorState senderActorState;

    public SendStatementExecutorVisitor(HybridState hybridState, ActorState actorState) {
        this.hybridState = hybridState;
        this.senderActorState = actorState;
    }

    public HybridState execute() {
        List<Statement> statements = senderActorState.getSigma();
        if (statements instanceof DotPrimary) {
            return this.visit((DotPrimary) statements);
        }
        return null;
    }

    @Override
    public HybridState visit(DotPrimary dotPrimary) {
        Cloner cloner = new Cloner();
        HybridState newHybridState = cloner.deepClone(hybridState);
        String receiverActorName = ((TermPrimary) dotPrimary.getLeft()).getName();
        // TODO: change base on knownRebec Table
        return null;
    }
}
