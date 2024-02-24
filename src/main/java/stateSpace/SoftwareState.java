package stateSpace;

import org.apache.commons.lang3.SerializationUtils;
import dataStructure.DiscreteVariable;
import dataStructure.ContinuousVariable;
import dataStructure.Variable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import utils.CompilerUtil;

import javax.annotation.Nonnull;
import java.lang.StringBuilder;
import java.math.BigDecimal;
import java.util.*;

public class SoftwareState extends ActorState {

    /**
     * resume time of actor
     */
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
        super("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        this.actorName = softwareState.getActorName();
        HashMap<String, Variable> newVariableValuation = new HashMap<>();
        for (Map.Entry<String, Variable> entry : softwareState.getVariableValuation().entrySet()) {
            if (entry.getValue() instanceof DiscreteVariable) {
                newVariableValuation.put(entry.getKey(), new DiscreteVariable((DiscreteVariable) entry.getValue()));
            } else if (entry.getValue() instanceof ContinuousVariable) {
                newVariableValuation.put(entry.getKey(), new ContinuousVariable((ContinuousVariable) entry.getValue()));
            }
        }
        this.variablesValuation = newVariableValuation;
        Set<Message> newMessageBag = new HashSet<>();
        for (Message message : softwareState.getMessageBag()) {
            newMessageBag.add(new Message(message));
        }
        this.messageBag = newMessageBag;
        List<Statement> newSigma = new ArrayList<>();
        for (Statement statement : softwareState.getSigma()) {
            Statement copiedStatement = SerializationUtils.clone(statement);
            newSigma.add(copiedStatement);
        }
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
        // CHECKME: which variables should be included in the string?
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Actor: ").append(getActorName()).append("\n");
        stringBuilder.append("Resume Time: ").append(getResumeTime().toString()).append("\n");
        stringBuilder.append("Local Time: ").append(getLocalTime()).append("\n");

        stringBuilder.append("Variable Valuation: ").append("\n");
        // CHECKME: order of the variables is not guaranteed, is it a problem?
        for (Map.Entry<String, Variable> entry : getVariableValuation().entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        stringBuilder.append("Message Bag: ").append("\n");
        // CHECKME: order of the messages is not guaranteed, is it a problem?
        for (Message message : getMessageBag()) {
            stringBuilder.append(message.toString()).append("\n");
        }

        stringBuilder.append("Sigma: ").append("\n");
        // CHECKME: order of the statements is not guaranteed, is it a problem?
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
            SoftwareState newSoftwareState = new SoftwareState(
                    getActorName(),
                    getVariableValuation(),
                    getMessageBag(),
                    getSigma(),
                    getLocalTime(),
                    getResumeTime()
            );
            // TODO: !!!START FROM HERE!!!
            BigDecimal tMin = globalTime.getUpperBound().min(message.getArrivalTime().getUpperBound());
            // updating actor valuation function
            // CHECKME: what should we do if parameters have same name as a valuation variable? we are overwriting them here
            // CHECKME: shouldn't we get a copy of parameters and then add them to variable valuation? (to avoid overwriting)
            newSoftwareState.addVariables(message.getParameters());
            // removing message from message bag
            newSoftwareState.removeMessage(message);
            // TODO: add body of the message to list of statement to be executed
            // FIXME: implement getMessageBody method
            List<Statement> messageBody = CompilerUtil.getMessageBody(newSoftwareState.getActorName(), message.getServerName());
            this.addStatements(messageBody);
            // TODO: update resume time
            // CHECKME: why we should update resume time?
            // FIXME: what should be the name of the ContinuousVariable?
            this.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound()));

            result.add(newSoftwareState);


            // CHECKME: shouldn't it be <= instead of <?
            if (globalTime.getUpperBound().compareTo(message.getArrivalTime().getUpperBound()) < 0) {
                newSoftwareState = new SoftwareState(
                        getActorName(),
                        getVariableValuation(),
                        getMessageBag(),
                        getSigma(),
                        getLocalTime(),
                        getResumeTime()
                );
                Message newMessage = new Message(
                        message.getSenderActor(),
                        message.getReceiverActor(),
                        message.getServerName(),
                        message.getParameters(),
                        new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), message.getArrivalTime().getUpperBound())
                );
                newSoftwareState.removeMessage(message);
                newSoftwareState.addMessage(newMessage);
                // FIXME: what epsilon means for sigma? should we set it to null? or should we set it to empty list?
                // newSoftwareState.setSigma(new ArrayList<>());
                // FIXME: what epsilon means for resumeTime?
                // newSoftwareState.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound()));
            }
        }

        return result;
    }

}
