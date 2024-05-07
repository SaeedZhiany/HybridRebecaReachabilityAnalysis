package stateSpace;

import javax.annotation.Nonnull;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.util.List;

import com.rits.cloning.Cloner;
import dataStructure.Variable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.DotPrimary;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.FormalParameterDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.TermPrimary;
import utils.CompilerUtil;
import utils.StringSHA256;
import dataStructure.ContinuousVariable;
import visitors.ExpressionEvaluatorVisitor;

public class HybridState {

    // CHECKME: should global time be non-null?
    @Nonnull
    private ContinuousVariable globalTime;
    @Nonnull
    private HashMap<String, SoftwareState> softwareStates;
    @Nonnull
    private HashMap<String, PhysicalState> physicalStates;
    @Nonnull
    private CANNetworkState CANNetworkState;
    @Nonnull
    private String hashString;

    public HybridState() {
        // FIXME: is this the correct way to initialize globalTime?
        // ContinuousVariable globalTime = new ContinuousVariable("globalTime");
        this(new ContinuousVariable("globalTime"), new HashMap<>(), new HashMap<>(), new CANNetworkState());
    }

    public HybridState(HybridState hybridState) {
        // CHECKME: aren't this attributes private?
        this.globalTime = new ContinuousVariable(hybridState.globalTime);
        HashMap<String, SoftwareState> newSoftwareStates = new HashMap<>();
        for (SoftwareState softwareState : hybridState.softwareStates.values()) {
            newSoftwareStates.put(softwareState.actorName, new SoftwareState(softwareState));
        }
        this.softwareStates = newSoftwareStates;
        HashMap<String, PhysicalState> newPhysicalStates = new HashMap<>();
        for (PhysicalState physicalState : hybridState.physicalStates.values()) {
            newPhysicalStates.put(physicalState.actorName, new PhysicalState(physicalState));
        }
        this.physicalStates = newPhysicalStates;
        this.CANNetworkState = new CANNetworkState(hybridState.CANNetworkState);
    }

    public HybridState(
            @Nonnull ContinuousVariable globalTime,
            @Nonnull HashMap<String, SoftwareState> softwareStates,
            @Nonnull HashMap<String, PhysicalState> physicalStates,
            @Nonnull stateSpace.CANNetworkState CANNetworkState
    ) {
        this.globalTime = globalTime;
        this.softwareStates = softwareStates;
        this.physicalStates = physicalStates;
        this.CANNetworkState = CANNetworkState;
        // CHECKME: is this the correct way to handle hashString exception?
        try {
            this.hashString = updateHash();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean equals(HybridState state) {
        String thisHashString = this.getHash();
        String stateHashString = state.getHash();
        if (thisHashString != stateHashString) {
            return false;
        }
        // TODO: make sure that the 2 states are actually equal
        return true;
    }

    private void replaceSoftwareState(SoftwareState softwareState) {
        softwareStates.replace(softwareState.actorName, softwareState);
    }

    private void replacePhysicalState(PhysicalState physicalState) {
        physicalStates.replace(physicalState.actorName, physicalState);
    }

    public void replaceActorState(ActorState actorState) {
        if (actorState instanceof SoftwareState) {
            replaceSoftwareState((SoftwareState) actorState);
        } else if (actorState instanceof PhysicalState) {
            replacePhysicalState((PhysicalState) actorState);
        }
        // CHECKME: else
        try {
            this.updateHash();
        } catch (Exception e) {
            // FIXME: is this the correct way to handle this exception?
            throw new RuntimeException(e);
        }
    }
    @Override
    public String toString() {
        // CHECKME: the order of the states is not guaranteed, is it a problem?
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(globalTime.toString());

        for (SoftwareState softwareState : softwareStates.values()) {
            stringBuilder.append(softwareState.toString());
            stringBuilder.append(";");
        }
        for (PhysicalState physicalState : physicalStates.values()) {
            stringBuilder.append(physicalState.toString());
            stringBuilder.append(";");
        }
        // stringBuilder.append(CANNetworkState.toString());
        return stringBuilder.toString();
    }

    // CHECKME: when should we call this method?
    private String updateHash() throws Exception {
        return StringSHA256.hashString(this.toString());
    }

    private String getHash() {
        return this.hashString;
    }

    public HashMap<String, PhysicalState> getPhysicalStates() {
        return this.physicalStates;
    }

    public HashMap<String, SoftwareState> getSoftwareStates() {
        return this.softwareStates;
    }

    // CHECKME: check conditions
    public boolean isSuspended(ContinuousVariable resumeTime) {
        if ((resumeTime.getLowerBound().compareTo(this.globalTime.getLowerBound()) == 0) &&
                (resumeTime.getUpperBound().compareTo(this.globalTime.getUpperBound()) >= 0)) {
            return true;
        }
        return false;
    }

    public List<ActorState> takeMessage(ActorState actorState) {
        // TODO: call takeMessage method on actorState and retrieve the new actorStates
        // TODO: takeMessage method of SoftwareState can and should return multiple (at most 2?!) newSoftwareStates
        // CHECKME: does it call on correct class? (software and physical)
        return actorState.takeMessage(globalTime);
    }

    public HybridState sendStatement(ActorState actorState) {
        Cloner cloner = new Cloner();
        HybridState newHybridState = cloner.deepClone(this);
//        RunUnchangeableStatementsVisitors runner = new RunUnchangeableStatementsVisitors(actorState);
        // TODO: do it better for another type og statements
        ActorState newActorState = cloner.deepClone(actorState);
        // CHECKME: maybe shouldn't delete
        DotPrimary sendStatement = (DotPrimary) actorState.nextStatement();

        String sender = actorState.actorName;
        String receiver = RebecInstantiationMapping.getInstance().getKnownRebecBinding(sender, ((TermPrimary) sendStatement.getLeft()).getName());
        String serverName = ((TermPrimary) sendStatement.getRight()).getName();
        List<FormalParameterDeclaration> serverParams = CompilerUtil.getServerParameters(
                RebecInstantiationMapping.getInstance().getRebecReactiveClassType(receiver),
                serverName
        );
        HashMap<String, Variable > callParameters = new HashMap<>();
        ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(actorState.getVariableValuation());
        for (int i = 0; i < serverParams.size(); i++) {
            Variable paramValue = evaluatorVisitor.visit(((TermPrimary) sendStatement.getRight()).getParentSuffixPrimary().getArguments().get(i));
            paramValue.setName(serverParams.get(i).getName());
            callParameters.put(
                    serverParams.get(i).getName(),
                    paramValue
            );
        }
        Message message = new Message(actorState.actorName, receiver, serverName, callParameters, globalTime);
        ActorState receiverActorState = getActorState(receiver);
        receiverActorState.addMessage(message);
        newHybridState.replaceActorState(newActorState);
        newHybridState.replaceActorState(receiverActorState);
        return newHybridState;
    }

    public ContinuousVariable getGlobalTime() {
        return this.globalTime;
    }

    public ActorState getActorState(String actorName) {
        SoftwareState softwareState = softwareStates.get(actorName);
        return softwareState != null ? softwareState : physicalStates.get(actorName);
    }
}
