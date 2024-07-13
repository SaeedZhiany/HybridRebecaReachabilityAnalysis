package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.timedrebeca.objectmodel.TimedRebecaParentSuffixPrimary;
import utils.CompilerUtil;
import utils.StringSHA256;
import visitors.ExpressionEvaluatorVisitor;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HybridState {

    // CHECKME: should global time be non-null?
//    @Nonnull
    private ContinuousVariable globalTime;
    //    @Nonnull
    private HashMap<String, SoftwareState> softwareStates;
    //    @Nonnull
    private HashMap<String, PhysicalState> physicalStates;
    //    @Nonnull
    private CANNetworkState CANNetworkState;
    //    @Nonnull
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
//        this.CANNetworkState = new CANNetworkState(hybridState.CANNetworkState);
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
//        this.CANNetworkState = CANNetworkState;
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

        stringBuilder.append(globalTime);

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

    private void resetResumeTime(ActorState actorState) {
        if (actorState instanceof SoftwareState) {
            ((SoftwareState) actorState).setResumeTime(new ContinuousVariable("resume time"));
        }
    }

    private HybridState createSuspendedState(ActorState actorState) {
        if (actorState instanceof SoftwareState) {
            Cloner cloner = new Cloner();
            ActorState newActorState = cloner.deepClone(actorState);
            HybridState newHybridState = cloner.deepClone(this);
            if (isNonDeterministicInResumeTime(((SoftwareState) newActorState).getResumeTime())) {
                ContinuousVariable resumeTime = ((SoftwareState) newActorState).getResumeTime();
                ((SoftwareState) newActorState).setResumeTime(new ContinuousVariable("resume time", globalTime.getUpperBound(), resumeTime.getUpperBound()));
                newHybridState.replaceActorState(newActorState);
                return newHybridState;
            }
        }
        return null;
    }

    public boolean isSuspended(ContinuousVariable resumeTime) {
        if ((resumeTime.getLowerBound().compareTo(this.globalTime.getLowerBound()) <= 0)) { // the less than never happens, just for the sake of completeness
            return false;
        }
        return true;
    }

    private boolean isNonDeterministicInResumeTime(ContinuousVariable resumeTime) {
        if ((resumeTime.getLowerBound().compareTo(this.globalTime.getLowerBound()) <= 0) &&
                (resumeTime.getUpperBound().compareTo(this.globalTime.getUpperBound())) > 0) {
            return true;
        }
        return false;
    }

    public List<HybridState> takeMessage(ActorState actorState) {
        // TODO: call takeMessage method on actorState and retrieve the new actorStates
        // TODO: takeMessage method of SoftwareState can and should return multiple (at most 2?!) newSoftwareStates
        // CHECKME: does it call on correct class? (software and physical)
        List<HybridState> result = new ArrayList<>();
        List<ActorState> generatedActorStates = actorState.takeMessage(globalTime);
        for (ActorState actorStateItr : generatedActorStates) {
            Cloner cloner = new Cloner();
            HybridState newHybridState = cloner.deepClone(this);
            ActorState newActorState = cloner.deepClone(actorStateItr);
            resetResumeTime(newActorState);
            newHybridState.replaceActorState(newActorState);
            result.add(newHybridState);
        }
        HybridState suspendedState = createSuspendedState(actorState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }
        return result;
    }

    private ContinuousVariable getDelayAfterTime(Expression lowerBoundExp, Expression upperBoundExp, Expression exp, ExpressionEvaluatorVisitor evaluatorVisitor) {
        Variable lowerBound = (lowerBoundExp != null) ? evaluatorVisitor.visit(lowerBoundExp) : null;
        Variable upperBound = (upperBoundExp != null) ? evaluatorVisitor.visit(upperBoundExp) : null;
        Variable after = (exp != null) ? evaluatorVisitor.visit(exp) : null;

        ContinuousVariable messageArrivalTime = new ContinuousVariable(globalTime);
        if (after != null) {
            if (after instanceof IntervalRealVariable) {
                messageArrivalTime.setLowerBound(globalTime.getLowerBound().add(BigDecimal.valueOf(((IntervalRealVariable) after).getLowerBound())));
                messageArrivalTime.setUpperBound(globalTime.getUpperBound().add(BigDecimal.valueOf(((IntervalRealVariable) after).getUpperBound())));
            } else {
                messageArrivalTime.setLowerBound(globalTime.getLowerBound().add(((DiscreteDecimalVariable) after).getValue()));
                messageArrivalTime.setUpperBound(globalTime.getUpperBound().add(((DiscreteDecimalVariable) after).getValue()));
            }
        }

        if (lowerBound != null) {
            if (after instanceof IntervalRealVariable) {
                messageArrivalTime.setLowerBound(globalTime.getLowerBound().add(BigDecimal.valueOf(((IntervalRealVariable) lowerBound).getLowerBound())));
            } else {
                messageArrivalTime.setLowerBound(globalTime.getLowerBound().add(((DiscreteDecimalVariable) lowerBound).getValue()));
            }
        }

        if (upperBound != null) {
            if (after instanceof IntervalRealVariable) {
                messageArrivalTime.setUpperBound(globalTime.getUpperBound().add(BigDecimal.valueOf(((IntervalRealVariable) upperBound).getUpperBound())));
            } else {
                messageArrivalTime.setUpperBound(globalTime.getUpperBound().add(((DiscreteDecimalVariable) upperBound).getValue()));
            }
        }

        return messageArrivalTime;
    }

    private HashMap<String, Variable> getMessageCallParameter(String receiver, String serverName, ExpressionEvaluatorVisitor evaluatorVisitor, DotPrimary sendStatement) {
        List<FormalParameterDeclaration> serverParams = CompilerUtil.getServerParameters(
                RebecInstantiationMapping.getInstance().getRebecReactiveClassType(receiver),
                serverName
        );
        HashMap<String, Variable> callParameters = new HashMap<>();
        for (int i = 0; i < serverParams.size(); i++) {
            Variable paramValue = evaluatorVisitor.visit(((TermPrimary) sendStatement.getRight()).getParentSuffixPrimary().getArguments().get(i));
            paramValue.setName(serverParams.get(i).getName());
            callParameters.put(
                    serverParams.get(i).getName(),
                    paramValue
            );
        }
        return callParameters;
    }

    public List<HybridState> sendStatement(ActorState actorState) {
        Cloner cloner = new Cloner();
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = cloner.deepClone(this);
//        RunUnchangeableStatementsVisitors runner = new RunUnchangeableStatementsVisitors(actorState);
        // TODO: do it better for another type og statements
        ActorState newActorState = cloner.deepClone(actorState);
        // CHECKME: maybe shouldn't delete
        final DotPrimary sendStatement = (DotPrimary) actorState.getSigma().get(0);
        newActorState.nextStatement();

        String sender = actorState.actorName;
        String receiver = RebecInstantiationMapping.getInstance().getKnownRebecBinding(sender, ((TermPrimary) sendStatement.getLeft()).getName());
        String serverName = ((TermPrimary) sendStatement.getRight()).getName();

        ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(newActorState.getVariableValuation());
        HashMap<String, Variable> callParameters = getMessageCallParameter(receiver, serverName, evaluatorVisitor, sendStatement);

        ContinuousVariable messageArrivalTime = getDelayAfterTime(
                ((TimedRebecaParentSuffixPrimary) ((TermPrimary) sendStatement.getRight()).getParentSuffixPrimary()).getStartAfterExpression(),
                ((TimedRebecaParentSuffixPrimary) ((TermPrimary) sendStatement.getRight()).getParentSuffixPrimary()).getEndAfterExpression(),
                ((TimedRebecaParentSuffixPrimary) ((TermPrimary) sendStatement.getRight()).getParentSuffixPrimary()).getAfterExpression(),
                evaluatorVisitor
        );

        Message message = new Message(actorState.actorName, receiver, serverName, callParameters, messageArrivalTime);
        ActorState receiverActorState = newHybridState.getActorState(receiver);
        receiverActorState.addMessage(message);
        resetResumeTime(newActorState);
        newHybridState.replaceActorState(newActorState);
        newHybridState.replaceActorState(receiverActorState);
        result.add(newHybridState);

        HybridState suspendedState = createSuspendedState(actorState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }

        return result;
    }

    public List<HybridState> assignStatement(ActorState actorState) {
        Cloner cloner = new Cloner();
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = cloner.deepClone(this);
        ActorState newActorState = cloner.deepClone(actorState);
        // CHECKME: maybe shouldn't delete
        BinaryExpression assignStatement = (BinaryExpression) newActorState.nextStatement();

        String variableName = ((TermPrimary) assignStatement.getLeft()).getName();
        Variable variableValue = new ExpressionEvaluatorVisitor(actorState.getVariableValuation()).visit(assignStatement.getRight());
        variableValue.setName(variableName);
        newActorState.updateVariable(variableValue);
        resetResumeTime(newActorState);
        newHybridState.replaceActorState(newActorState);
        result.add(newHybridState);

        HybridState suspendedState = createSuspendedState(actorState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }

        return result;
    }

    public List<HybridState> delayStatement(SoftwareState softwareState) {
        Cloner cloner = new Cloner();
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = cloner.deepClone(this);
        SoftwareState newSoftwareState = cloner.deepClone(softwareState);
        // CHECKME: maybe shouldn't delete
        final TermPrimary delayStatement = (TermPrimary) softwareState.getSigma().get(0);
        newSoftwareState.nextStatement();

        ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(newSoftwareState.getVariableValuation());
        ContinuousVariable delayTime = getDelayAfterTime(
                delayStatement.getParentSuffixPrimary().getArguments().get(0),
                delayStatement.getParentSuffixPrimary().getArguments().get(1),
                delayStatement.getParentSuffixPrimary().getArguments().get(0),
                evaluatorVisitor
        );

        newSoftwareState.setResumeTime(delayTime);
        newHybridState.replaceActorState(newSoftwareState);
        result.add(newHybridState);

        HybridState suspendedState = createSuspendedState(softwareState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }
        return result;
    }

    public ContinuousVariable getGlobalTime() {
        return this.globalTime;
    }

    public ActorState getActorState(String actorName) {
        SoftwareState softwareState = softwareStates.get(actorName);
        return softwareState != null ? softwareState : physicalStates.get(actorName);
    }
}
