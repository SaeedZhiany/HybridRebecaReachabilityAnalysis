package stateSpace;

import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.timedrebeca.objectmodel.TimedRebecaParentSuffixPrimary;
import utils.CompilerUtil;
import utils.StringSHA256;
import visitors.ExpressionEvaluatorVisitor;

import javax.annotation.Nonnull;
import java.util.*;

public class HybridState {
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
    private String parentHash;


    public HybridState() {
        this(new ContinuousVariable("globalTime"), new HashMap<>(), new HashMap<>(), new CANNetworkState());
    }

    public HybridState(HybridState hybridState) {
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
        this.parentHash = hybridState.getParentHash();
        this.updateHash();
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
        try {
            this.hashString = updateHash();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean equals(HybridState state) {
        String thisHashString = this.getHash();
        String stateHashString = state.getHash();
        return thisHashString == stateHashString;
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
        try {
            this.updateHash();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(globalTime).append("\n");
        for (SoftwareState softwareState : softwareStates.values()) {
            stringBuilder.append(softwareState.toString());
            stringBuilder.append(";");
        }
        stringBuilder.append("\n");
        for (PhysicalState physicalState : physicalStates.values()) {
            stringBuilder.append(physicalState.toString());
            stringBuilder.append(";");
        }
        return stringBuilder.toString();
    }

    public String updateHash() {
        this.hashString = StringSHA256.hashString(this.toString());
        return this.hashString;
    }

    public String getHash() {
        return this.updateHash();
    }

    public HashMap<String, PhysicalState> getPhysicalStates() {
        return this.physicalStates;
    }

    public HashMap<String, SoftwareState> getSoftwareStates() {
        return this.softwareStates;
    }

    private void resetResumeTime(ActorState actorState) {
        if (actorState instanceof SoftwareState) {
            ((SoftwareState) actorState).setResumeTime(new ContinuousVariable("resumeTime"));
        }
    }

    private HybridState createSuspendedState(ActorState actorState) {
        if (actorState instanceof SoftwareState softwareState) {
            SoftwareState newActorState = new SoftwareState(softwareState);
            HybridState newHybridState = new HybridState(this);
            if (isNonDeterministicInResumeTime(newActorState.getResumeTime())) {
                ContinuousVariable resumeTime = newActorState.getResumeTime();
                newActorState.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getUpperBound(), resumeTime.getUpperBound()));
                newHybridState.replaceActorState(newActorState);
                return newHybridState;
            }
        }
        return null;
    }

    private void resetStateVarsIfLastStmt(ActorState actorState) {
        if (actorState.getSigma().isEmpty()) {
            String actorClassType = RebecInstantiationMapping.getInstance().getRebecReactiveClassType(actorState.getActorName());
            Set<String> stateVarsName = CompilerUtil.getStateVars(actorClassType);
            actorState.getVariableValuation().keySet().retainAll(stateVarsName);
        }
    }

    public boolean isSuspended(ContinuousVariable resumeTime) {
        return resumeTime.getLowerBound().compareTo(this.globalTime.getLowerBound()) > 0;
    }

    private boolean isNonDeterministicInResumeTime(ContinuousVariable resumeTime) {
        return (resumeTime.getLowerBound().compareTo(this.globalTime.getLowerBound()) <= 0) &&
                (resumeTime.getUpperBound().compareTo(this.globalTime.getUpperBound())) > 0;
    }

    public List<HybridState> takeMessage(ActorState actorState) {
        List<HybridState> result = new ArrayList<>();
        List<ActorState> generatedActorStates = actorState.takeMessage(globalTime);
        for (ActorState actorStateItr : generatedActorStates) {
            HybridState newHybridState = new HybridState(this);
            ActorState newActorState = actorStateItr instanceof SoftwareState ?
                    new SoftwareState((SoftwareState) actorStateItr) :
                    new PhysicalState((PhysicalState) actorStateItr);
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
        Variable lowerBound = evaluateIfNotNull(lowerBoundExp, evaluatorVisitor);
        Variable upperBound = evaluateIfNotNull(upperBoundExp, evaluatorVisitor);
        Variable after = evaluateIfNotNull(exp, evaluatorVisitor);

        ContinuousVariable messageArrivalTime = new ContinuousVariable(globalTime);

        if (after != null) {
            messageArrivalTime.setLowerBound(globalTime.getLowerBound() + extractLowerBound(after));
            messageArrivalTime.setUpperBound(globalTime.getUpperBound() + extractUpperBound(after));
        }

        if (lowerBound != null) {
            messageArrivalTime.setLowerBound(globalTime.getLowerBound() + extractLowerBound(lowerBound));
        }

        if (upperBound != null) {
            messageArrivalTime.setUpperBound(globalTime.getUpperBound() + extractUpperBound(upperBound));
        }

        return messageArrivalTime;
    }

    private Variable evaluateIfNotNull(Expression expr, ExpressionEvaluatorVisitor visitor) {
        return (expr != null) ? visitor.visit(expr) : null;
    }

    private double extractLowerBound(Variable variable) {
        if (variable instanceof IntervalRealVariable) {
            return ((IntervalRealVariable) variable).getLowerBound();
        } else if (variable instanceof DiscreteDecimalVariable) {
            return ((DiscreteDecimalVariable) variable).getValue().doubleValue();
        }
        throw new IllegalArgumentException("Unsupported variable type for lower bound extraction.");
    }

    private double extractUpperBound(Variable variable) {
        if (variable instanceof IntervalRealVariable) {
            return ((IntervalRealVariable) variable).getUpperBound();
        } else if (variable instanceof DiscreteDecimalVariable) {
            return ((DiscreteDecimalVariable) variable).getValue().doubleValue();
        }
        throw new IllegalArgumentException("Unsupported variable type for upper bound extraction.");
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

    private void addExtractedStatement(ActorState actorState, Statement statement) {
        if (statement != null) {
            if (statement instanceof BlockStatement) {
                actorState.addStatementsToFront(((BlockStatement) statement).getStatements());
            } else {
                actorState.addStatementsToFront(List.of(statement));
            }
        }
    }

    private ActorState cloneActorState(ActorState actorState) {
        return actorState instanceof SoftwareState ?
                new SoftwareState((SoftwareState) actorState) :
                new PhysicalState((PhysicalState) actorState);
    }

    private String resolveReceiver(String sender, DotPrimary statement) {
        String targetName = ((TermPrimary) statement.getLeft()).getName();
        String receiver = RebecInstantiationMapping.getInstance().getKnownRebecBinding(sender, targetName);
        return (receiver == null && "self".equals(targetName)) ? sender : receiver;
    }

    private ContinuousVariable computeArrivalTime(DotPrimary statement, ExpressionEvaluatorVisitor evaluator) {
        TimedRebecaParentSuffixPrimary suffix = (TimedRebecaParentSuffixPrimary)
                ((TermPrimary) statement.getRight()).getParentSuffixPrimary();
        return getDelayAfterTime(
                suffix.getStartAfterExpression(),
                suffix.getEndAfterExpression(),
                suffix.getAfterExpression(),
                evaluator
        );
    }

    public List<HybridState> sendStatement(ActorState actorState) {
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = new HybridState(this);

        ActorState newActorState = cloneActorState(actorState);
        newActorState.nextStatement();

        DotPrimary sendStatement = (DotPrimary) actorState.getSigma().get(0);
        String sender = actorState.actorName;
        String receiver = resolveReceiver(sender, sendStatement);
        String serverName = ((TermPrimary) sendStatement.getRight()).getName();

        ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(newActorState.getVariableValuation());
        HashMap<String, Variable> callParameters = getMessageCallParameter(receiver, serverName, evaluatorVisitor, sendStatement);
        ContinuousVariable arrivalTime = computeArrivalTime(sendStatement, evaluatorVisitor);
        arrivalTime.setName("arrivalTime");

        Message message = new Message(sender, receiver, serverName, callParameters, arrivalTime);
        resetResumeTime(newActorState);
        applyMessageToHybridState(newHybridState, newActorState, receiver, message);

        resetStateVarsIfLastStmt(newActorState);
        result.add(newHybridState);

        HybridState suspendedState = createSuspendedState(actorState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }
        return result;
    }

    private void applyMessageToHybridState(HybridState state, ActorState senderState, String receiver, Message message) {
        if (senderState.actorName.equals(receiver)) {
            senderState.addMessage(message);
            state.replaceActorState(senderState);
        } else {
            ActorState receiverState = state.getActorState(receiver);
            receiverState.addMessage(message);
            state.replaceActorState(senderState);
            state.replaceActorState(receiverState);
        }
    }

    public List<HybridState> assignStatement(ActorState actorState) {
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = new HybridState(this);
        ActorState newActorState = cloneActorState(actorState);
        BinaryExpression assignStatement = (BinaryExpression) newActorState.nextStatement();

        String variableName = ((TermPrimary) assignStatement.getLeft()).getName();
        Variable variableValue = new ExpressionEvaluatorVisitor(actorState.getVariableValuation()).visit(assignStatement.getRight());
        variableValue.setName(variableName);
        newActorState.updateVariable(variableValue);
        resetResumeTime(newActorState);
        newHybridState.replaceActorState(newActorState);

        resetStateVarsIfLastStmt(newActorState);
        result.add(newHybridState);

        HybridState suspendedState = createSuspendedState(actorState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }

        return result;
    }

    public List<HybridState> delayStatement(SoftwareState softwareState) {
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = new HybridState(this);
        SoftwareState newSoftwareState = new SoftwareState(softwareState);
        final TermPrimary delayStatement = (TermPrimary) softwareState.getSigma().get(0);
        newSoftwareState.nextStatement();

        ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(newSoftwareState.getVariableValuation());
        ContinuousVariable delayTime = getDelayAfterTime(
                delayStatement.getParentSuffixPrimary().getArguments().get(0),
                delayStatement.getParentSuffixPrimary().getArguments().get(1),
                delayStatement.getParentSuffixPrimary().getArguments().get(0),
                evaluatorVisitor
        );
        delayTime.setName("resumeTime");

        newSoftwareState.setResumeTime(delayTime);
        newHybridState.replaceActorState(newSoftwareState);
        resetStateVarsIfLastStmt(newSoftwareState);
        result.add(newHybridState);

        HybridState suspendedState = createSuspendedState(softwareState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }
        return result;
    }

    public List<HybridState> ifStatement(ActorState actorState) {
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = new HybridState(this);

        ActorState newActorState = cloneActorState(actorState);
        ConditionalStatement conditionalStatement = (ConditionalStatement) actorState.getSigma().get(0);
        newActorState.nextStatement();

        ExpressionEvaluatorVisitor evaluator = new ExpressionEvaluatorVisitor(actorState.getVariableValuation());
        DiscreteBoolVariable conditionResult = (DiscreteBoolVariable) evaluator.visit(conditionalStatement.getCondition());

        if (conditionResult.getDefinite()) {
            handleDefiniteCondition(newActorState, conditionalStatement, conditionResult, newHybridState);
            result.add(newHybridState);
        } else {
            result.addAll(handleIndefiniteCondition(actorState, conditionalStatement, newHybridState));
        }

        HybridState suspendedState = createSuspendedState(actorState);
        if (suspendedState != null) {
            result.add(suspendedState);
        }

        return result;
    }

    private void handleDefiniteCondition(ActorState actorState, ConditionalStatement conditionalStatement,
                                         DiscreteBoolVariable conditionResult, HybridState hybridState) {
        if (conditionResult.getValue()) {
            addExtractedStatement(actorState, conditionalStatement.getStatement());
        } else {
            addExtractedStatement(actorState, conditionalStatement.getElseStatement());
        }
        resetResumeTime(actorState);
        hybridState.replaceActorState(actorState);
        resetStateVarsIfLastStmt(actorState);
    }

    private List<HybridState> handleIndefiniteCondition(ActorState originalActorState, ConditionalStatement conditionalStatement,
                                                        HybridState baseHybridState) {
        List<HybridState> states = new ArrayList<>();

        // First branch - condition true
        ActorState trueBranchState = cloneActorState(originalActorState);
        trueBranchState.nextStatement();
        addExtractedStatement(trueBranchState, conditionalStatement.getStatement());

        // Second branch - condition false
        ActorState falseBranchState = cloneActorState(originalActorState);
        falseBranchState.nextStatement();
        addExtractedStatement(falseBranchState, conditionalStatement.getElseStatement());

        // Reset times
        resetResumeTime(trueBranchState);
        resetResumeTime(falseBranchState);

        // Create HybridStates
        baseHybridState.replaceActorState(trueBranchState);
        HybridState falseHybridState = new HybridState(baseHybridState);
        falseHybridState.replaceActorState(falseBranchState);

        resetStateVarsIfLastStmt(trueBranchState);
        resetStateVarsIfLastStmt(falseBranchState);

        states.add(baseHybridState);
        states.add(falseHybridState);

        return states;
    }

    public List<HybridState> setModeStatement(PhysicalState physicalState) {
        List<HybridState> result = new ArrayList<>();
        HybridState newHybridState = new HybridState(this);
        PhysicalState newPhysicalState = new PhysicalState(physicalState);
        final TermPrimary setModeStatement = (TermPrimary) physicalState.getSigma().get(0);
        newPhysicalState.nextStatement();
        String mode = ((TermPrimary) setModeStatement.getParentSuffixPrimary().getArguments().get(0)).getName();
        newPhysicalState.setMode(mode);
        newPhysicalState.setLastTimeModeChangedLowerBound(globalTime.getLowerBound());
        newHybridState.replaceActorState(newPhysicalState);
        resetStateVarsIfLastStmt(newPhysicalState);
        result.add(newHybridState);
        return result;
    }

    public ContinuousVariable getGlobalTime() {
        return this.globalTime;
    }

    public ActorState getActorState(String actorName) {
        SoftwareState softwareState = softwareStates.get(actorName);
        return softwareState != null ? softwareState : physicalStates.get(actorName);
    }

    public List<Set<String>> getGlobalStateModes() {
        List<Set<String>> globalStateModes = new ArrayList<>();
        for (Map.Entry<String, PhysicalState> entry : physicalStates.entrySet()) {
            String key = entry.getKey();
            PhysicalState value = entry.getValue();
            if (!("none".equals(value.getMode()))) {
                Set<String> new_set = new HashSet<>();
                new_set.add(key);
                new_set.add(value.getMode());
                globalStateModes.add(new_set);
            }
        }
        return globalStateModes;
    }

    public double[] getIntervals(String[] ODEs) {
        ArrayList<Double> intervalsList = new ArrayList<>();
        for (String ODE : ODEs) {
            String[] components = extractVariableNames(ODE);
            String physicalClassName = components[0], odeVariableName = components[1];
            for (Map.Entry<String, PhysicalState> entry : physicalStates.entrySet()) {
                PhysicalState it = entry.getValue();
                String itName = entry.getKey();
                if (physicalClassName.equals(itName)) {
                    for (Map.Entry<String, Variable> VariablesValuation : it.getVariablesValuation().entrySet()) {
                        String variable = VariablesValuation.getKey();
                        Variable valuation = VariablesValuation.getValue();
                        if (odeVariableName.equals(variable)) {
                            intervalsList.add(((IntervalRealVariable) valuation).getLowerBound());
                            intervalsList.add(((IntervalRealVariable) valuation).getUpperBound());
                        }
                    }
                }
            }
        }
        double[] intervalsArray = new double[intervalsList.size()];
        for (int i = 0; i < intervalsList.size(); i++)
            intervalsArray[i] = intervalsList.get(i);
        return intervalsArray;
    }

    public static String[] extractVariableNames(String input) {
        String[] result = new String[2];
        String[] firstSplit = input.split("_");
        result[0] = firstSplit[0];
        String secondPart = firstSplit[1].replace("'", "").split("=")[0];
        result[1] = secondPart;
        return result;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public double[] getEvents(double globalTimeLowerBound, double timeInterval) {
        ArrayList<Double> resumeTimes = getSoftwareStatesResumeTimes();
        ArrayList<Double> arrivalTimes = getMessageArrivalTimes();
        ArrayList<Double> combinedList = new ArrayList<>(resumeTimes);
        combinedList.addAll(arrivalTimes);
        combinedList.removeIf(value -> value <= globalTimeLowerBound);
        double[] Events = combinedList.stream().mapToDouble(Double::doubleValue).toArray();
        Arrays.sort(Events);
        return Events;
    }

    private ArrayList<Double> getSoftwareStatesResumeTimes() {
        ArrayList<Double> resumeTimes = new ArrayList<>();
        for (SoftwareState softwareState : softwareStates.values()) {
            resumeTimes.add(softwareState.getResumeTime().getLowerBound().doubleValue());
            resumeTimes.add(softwareState.getResumeTime().getUpperBound().doubleValue());
        }
        return resumeTimes;
    }

    private ArrayList<Double> getMessageArrivalTimes() {
        ArrayList<Double> arrivalTimes = new ArrayList<>();
        for (SoftwareState softwareState : softwareStates.values()) {
            for (Message message : softwareState.messageBag) {
                arrivalTimes.add(message.getArrivalTime().getLowerBound().doubleValue());
                arrivalTimes.add(message.getArrivalTime().getUpperBound().doubleValue());
            }
        }
        return arrivalTimes;
    }

    public void setPhysicalStates(HashMap<String, PhysicalState> physicalStates) {
        this.physicalStates = physicalStates;
    }

    public void setSoftwareStates(HashMap<String, SoftwareState> softwareStates) {
        this.softwareStates = softwareStates;
    }

    public void updateGlobalTime(double lowerBound, double upperBound) {
        this.globalTime.setLowerBound(lowerBound);
        this.globalTime.setUpperBound(upperBound);
    }
}
