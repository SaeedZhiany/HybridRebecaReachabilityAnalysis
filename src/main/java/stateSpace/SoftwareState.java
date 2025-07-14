package stateSpace;

import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import utils.CompilerUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class SoftwareState extends ActorState {
    private ContinuousVariable resumeTime;

    public SoftwareState(
            @Nonnull String actorName,
            @Nonnull HashMap<String, Variable> variableValuation,
            @Nonnull Set<Message> messageBag,
            @Nonnull List<Statement> sigma,
            float localTime,
            ContinuousVariable resumeTime
    ) {
        super(actorName, variableValuation, messageBag, sigma, localTime);
        this.resumeTime = resumeTime;
    }

    public SoftwareState(SoftwareState softwareState) {
        super(softwareState.getActorName(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        this.actorName = softwareState.getActorName();
        HashMap<String, Variable> newVariableValuation = new HashMap<>();
        for (Map.Entry<String, Variable> entry : softwareState.getVariableValuation().entrySet()) {
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
        for (Message message : softwareState.getMessageBag()) {
            newMessageBag.add(new Message(message));
        }
        this.messageBag = newMessageBag;
        List<Statement> newSigma = new ArrayList<>();
        newSigma = new ArrayList<>(softwareState.getSigma());
        this.sigma = newSigma;
        this.localTime = softwareState.getLocalTime();
        this.resumeTime = new ContinuousVariable(softwareState.getResumeTime());
    }

    public ContinuousVariable getResumeTime() {
        return resumeTime;
    }

    public void setResumeTime(ContinuousVariable resumeTime) {
        if (resumeTime.isValid()) {
            this.resumeTime = resumeTime;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Actor: ").append(getActorName()).append("\n");
        stringBuilder.append("Resume Time: ").append(getResumeTime().toString()).append("\n");
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
        for (Message message : getMessageBag()) {
            if (message.checkBounds(globalTime)) {
                return true;
            }
        }
        return false;
    }

    public List<Message> getMessagesToBeTaken(ContinuousVariable globalTime) {
        List<Message> result = new ArrayList<>();
        for (Message message : getMessageBag()) {
            if (message.checkBounds(globalTime)) {
                result.add(message);
            }
        }
        return result;
    }

    @Override
    public List<ActorState> takeMessage(ContinuousVariable globalTime) {
        List<ActorState> result = new ArrayList<>();
        List<Message> messagesToBeTaken = getMessagesToBeTaken(globalTime);
        for (Message message : messagesToBeTaken) {
            SoftwareState newSoftwareState = new SoftwareState(this);
            newSoftwareState.addVariables(message.getParameters());
            newSoftwareState.removeMessage(message);
            String reactiveClassType = RebecInstantiationMapping.getInstance().getRebecReactiveClassType(newSoftwareState.getActorName());
            List<Statement> messageBody = CompilerUtil.getMessageBody(reactiveClassType, message.getServerName());
            newSoftwareState.addStatements(messageBody);
            newSoftwareState.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound()));
            result.add(newSoftwareState);

            if (globalTime.getUpperBound().compareTo(message.getArrivalTime().getUpperBound()) < 0) {
                newSoftwareState = new SoftwareState(this);
                Message newMessage = new Message(
                        message.getSenderActor(),
                        message.getReceiverActor(),
                        message.getServerName(),
                        message.getParameters(),
                        new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), message.getArrivalTime().getUpperBound())
                );
                newSoftwareState.removeMessage(message);
                newSoftwareState.addMessage(newMessage);
                newSoftwareState.setSigma(new ArrayList<>());
                newSoftwareState.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound()));
                result.add(newSoftwareState);
            }
        }
        return result;
    }

}
