package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import utils.CompilerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.lang.StringBuilder;

public class PhysicalState extends ActorState {

    /**
     * mode of actor
     * the none mode represents as "none"
     */

    @Nullable
    private String mode;

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
    }

    public PhysicalState(PhysicalState physicalState) {
        super("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
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
        Set<Message> newMessageBag = new HashSet<>();
        for (Message message : physicalState.getMessageBag()) {
            newMessageBag.add(new Message(message));
        }
        this.messageBag = newMessageBag;
        List<Statement> newSigma = new ArrayList<>();
        Cloner cloner = new Cloner();
        for (Statement statement : physicalState.getSigma()) {
            // FIXME: this is a shallow copy, should we use a deep copy?
            Statement copiedStatement = cloner.deepClone(statement);
            newSigma.add(copiedStatement);
        }
        this.sigma = newSigma;
        this.localTime = physicalState.getLocalTime();
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
        // CHECKME: which variables should be included in the string?
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
        /**
         * TODO: START FROM HERE
         */
        List<ActorState> result = new ArrayList<>();
        List<Message> messagesToBeTaken = getMessagesToBeTaken(globalTime);
        Cloner cloner = new Cloner();
        for (Message message : messagesToBeTaken) {
            PhysicalState newPhysicalState = cloner.deepClone(this);
            // TODO: !!!START FROM HERE!!!
//            BigDecimal tMin = globalTime.getUpperBound().min(message.getArrivalTime().getUpperBound());
            // updating actor valuation function
            // CHECKME: what should we do if parameters have same name as a valuation variable? we are overwriting them here
            // CHECKME: shouldn't we get a copy of parameters and then add them to variable valuation? (to avoid overwriting)
            newPhysicalState.addVariables(message.getParameters());
            // removing message from message bag
            newPhysicalState.removeMessage(message);
            // TODO: add body of the message to list of statement to be executed
            List<Statement> messageBody = CompilerUtil.getMessageBody(newPhysicalState.getActorName(), message.getServerName());
            newPhysicalState.addStatements(messageBody);
            // TODO: update resume time
            // CHECKME: why we should update resume time?
            // FIXME: what should be the name of the ContinuousVariable?
            result.add(newPhysicalState);

            // CHECKME: shouldn't it be <= instead of <?
            if (globalTime.getUpperBound().compareTo(message.getArrivalTime().getUpperBound()) < 0) {
                newPhysicalState = cloner.deepClone(this);
                Message newMessage = new Message(
                        message.getSenderActor(),
                        message.getReceiverActor(),
                        message.getServerName(),
                        message.getParameters(),
                        new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), message.getArrivalTime().getUpperBound())
                );
                newPhysicalState.removeMessage(message);
                newPhysicalState.addMessage(newMessage);
                // FIXME: what epsilon means for sigma? should we set it to null? or should we set it to empty list?
                newPhysicalState.setSigma(new ArrayList<>());
                // FIXME: what epsilon means for resumeTime?
                result.add(newPhysicalState);
            }
        }

        return result;
    }
}
