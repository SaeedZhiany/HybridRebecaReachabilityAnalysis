package stateSpace;

import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import utils.CompilerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PhysicalState extends ActorState {

    @Nullable
    private String mode;
    @Nullable
    private final Map<String, List<Double>> ODEsResult;
    private double lastTimeModeChangedLowerBound = 0;

    private boolean guardExecuted;

    public PhysicalState(
            @Nonnull String actorName,
            @Nullable String mode,
            @Nonnull HashMap<String, Variable> variableValuation,
            @Nonnull Set<Message> messageBag,
            @Nonnull List<Statement> sigma,
            float localTime
    ) {
        super(actorName, variableValuation, messageBag, sigma, localTime);
        this.mode = mode;
        this.ODEsResult = new HashMap<>();
        guardExecuted = false;
    }

    public PhysicalState(PhysicalState physicalState) {
        super(physicalState.getActorName(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        this.actorName = physicalState.getActorName();
        this.mode = physicalState.getMode();
        HashMap<String, Variable> newVariableValuation = new HashMap<>();
        for (Map.Entry<String, Variable> entry : physicalState.getVariableValuation().entrySet()) {
            if (entry.getValue() instanceof DiscreteDecimalVariable) {
                newVariableValuation.put(entry.getKey(), new DiscreteDecimalVariable((DiscreteDecimalVariable) entry.getValue()));
            } else if (entry.getValue() instanceof ContinuousVariable) {
                newVariableValuation.put(entry.getKey(), new ContinuousVariable((ContinuousVariable) entry.getValue()));
            } else if (entry.getValue() instanceof IntervalRealVariable) {
                newVariableValuation.put(entry.getKey(), new IntervalRealVariable((IntervalRealVariable) entry.getValue()));
            } else if (entry.getValue() instanceof DiscreteBoolVariable) {
                newVariableValuation.put(entry.getKey(), new DiscreteBoolVariable((DiscreteBoolVariable) entry.getValue()));
            }
        }
        this.variablesValuation = newVariableValuation;
        Set<Message> newMessageBag = new HashSet<>();
        for (Message message : physicalState.getMessageBag()) {
            newMessageBag.add(new Message(message));
        }
        this.messageBag = newMessageBag;
        List<Statement> newSigma = new ArrayList<>();
        Map<String, List<Double>> newODEsResult = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : physicalState.ODEsResult.entrySet()) {
            newODEsResult.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        this.sigma = newSigma;
        this.localTime = physicalState.getLocalTime();
        this.ODEsResult = newODEsResult;
        this.guardExecuted = physicalState.isGuardExecuted();
        this.lastTimeModeChangedLowerBound = physicalState.getLastTimeModeChangedLowerBound();
    }

    @Nullable
    public String getMode() {
        return mode;
    }

    public void setMode(@Nullable String mode) {
        this.mode = mode;
    }

    public boolean isIdle() {
        return mode.equals("none");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Actor: ").append(getActorName()).append("\n");
        stringBuilder.append("Mode: ").append(getMode()).append("\n");
        stringBuilder.append("Local Time: ").append(getLocalTime()).append("\n");

        stringBuilder.append("Variable Valuation: ").append("\n");
        TreeMap<String, Variable> treeVariableValuation = new TreeMap<>(getVariablesValuation());
        for (Map.Entry<String, Variable> entry : treeVariableValuation.entrySet()) {
            stringBuilder.append(entry.toString()).append("\n");
        }

        stringBuilder.append("Message Bag: ").append("\n");
        List<Message> sortedMessageBag = new ArrayList<>(getMessageBag());
        sortedMessageBag.sort(Comparator.comparing(Message::getId));
        for (Message message : sortedMessageBag) {
            stringBuilder.append(message.toString()).append("\n");
        }

        stringBuilder.append("Sigma: ").append("\n");
        for (Statement statement : getSigma()) {
            stringBuilder.append(statement.toString()).append("\n");
        }

        return stringBuilder.toString();
    }

    public boolean messageCanBeTaken(ContinuousVariable globalTime) {
        for (Message message : messageBag) {
            if (message.checkBounds(globalTime)) {
                if (!message.getServerName().equals("setMode")) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Message> getMessagesToBeTaken(ContinuousVariable globalTime) {
        List<Message> result = new ArrayList<>();
        for (Message message : getMessageBag()) {
            if (message.checkBounds(globalTime) && !message.getServerName().equals("setMode")) {
                result.add(message);
            }
        }
        return result;
    }

    public List<ActorState> takeMessage(ContinuousVariable globalTime) {
        List<ActorState> result = new ArrayList<>();
        List<Message> messagesToBeTaken = getMessagesToBeTaken(globalTime);
        for (Message message : messagesToBeTaken) {
            PhysicalState newPhysicalState = new PhysicalState(this);
            newPhysicalState.addVariables(message.getParameters());
            newPhysicalState.removeMessage(message);
            String physicalClassType = RebecInstantiationMapping.getInstance().getRebecReactiveClassType(newPhysicalState.getActorName());
            List<Statement> messageBody = CompilerUtil.getMessageBody(physicalClassType, message.getServerName());
            newPhysicalState.addStatements(messageBody);
            result.add(newPhysicalState);

            if (globalTime.getUpperBound().compareTo(message.getArrivalTime().getUpperBound()) < 0) {
                newPhysicalState = new PhysicalState(this);
                Message newMessage = new Message(
                        message.getSenderActor(),
                        message.getReceiverActor(),
                        message.getServerName(),
                        message.getParameters(),
                        new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), message.getArrivalTime().getUpperBound())
                );
                newPhysicalState.removeMessage(message);
                newPhysicalState.addMessage(newMessage);
                newPhysicalState.setSigma(new ArrayList<>());
                result.add(newPhysicalState);
            }
        }
        return result;
    }

    @Nullable
    public Map<String, List<Double>> getODEsResult() {
        return ODEsResult;
    }

    public List<Double> getODEResult(String ODEVariable) {
        return ODEsResult.get(ODEVariable);
    }

    public double getLastTimeModeChangedLowerBound() {
        return lastTimeModeChangedLowerBound;
    }

    public void setLastTimeModeChangedLowerBound(double lastTimeModeChangedLowerBound) {
        this.lastTimeModeChangedLowerBound = lastTimeModeChangedLowerBound;
    }

    public void computeODEBoundsForTimeRange(ContinuousVariable globalTime, double stepSize, double endSimulation) {
        int startIndex = calculateStartIndex(globalTime.getLowerBound(), stepSize);
        int endIndex = calculateEndIndex(globalTime.getUpperBound(), stepSize);

        for (Map.Entry<String, List<Double>> entry : ODEsResult.entrySet()) {
            String variableName = entry.getKey();
            List<Double> results = entry.getValue();

            Bounds bounds = extractBounds(results, startIndex, endIndex);
            IntervalRealVariable interval = (IntervalRealVariable) variablesValuation.get(variableName);
            interval.setLowerBound(bounds.min);
            interval.setUpperBound(bounds.max);
        }
    }

    private int calculateStartIndex(double lowerBound, double stepSize) {
        int index = (int) Math.floor((lowerBound - lastTimeModeChangedLowerBound) / stepSize);
        return Math.max(index, 0);
    }

    private int calculateEndIndex(double upperBound, double stepSize) {
        return (int) Math.ceil((upperBound - lastTimeModeChangedLowerBound) / stepSize);
    }

    private Bounds extractBounds(List<Double> results, int startIndex, int endIndex) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (int i = startIndex; i < endIndex; i++) {
            int baseIndex = i * 2;
            if (baseIndex + 1 >= results.size()) {
                double fallback = results.get(results.size() - 1);
                return new Bounds(fallback, fallback);
            }

            double lower = results.get(baseIndex);
            double upper = results.get(baseIndex + 1);
            min = Math.min(min, lower);
            max = Math.max(max, upper);
        }

        return new Bounds(min, max);
    }

    public void addODEResult(String ODEVaribale, List<Double> results) {
        results.remove(0);
        results.remove(0);
        ODEsResult.put(ODEVaribale, results);
    }

    public boolean isGuardExecuted() {
        return guardExecuted;
    }

    public void setGuardExecuted(boolean guardExecuted) {
        this.guardExecuted = guardExecuted;
    }

    private static class Bounds {
        double min;
        double max;
        Bounds(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
}
