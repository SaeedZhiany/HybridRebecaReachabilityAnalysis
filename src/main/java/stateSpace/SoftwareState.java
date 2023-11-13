package stateSpace;

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
            @Nonnull HashMap<String, DiscreteVariable> discreteVariableValuation,
            @Nonnull Queue<Message> queue,
            @Nonnull List<Statement> sigma,
            float localTime,
            ContinuousVariable resumeTime
    ) {
        super(actorName, discreteVariableValuation, queue, sigma, localTime);
        this.resumeTime = resumeTime;
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

        stringBuilder.append("Discrete Variable Valuation: ").append("\n");
        // CHECKME: order of the variables is not guaranteed, is it a problem?
        for (Map.Entry<String, DiscreteVariable> entry : getDiscreteVariableValuation().entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        stringBuilder.append("Queue: ").append("\n");
        // CHECKME: order of the messages is not guaranteed, is it a problem?
        for (Message message : getQueue()) {
            stringBuilder.append(message.toString()).append("\n");
        }

        stringBuilder.append("Sigma: ").append("\n");
        // CHECKME: order of the statements is not guaranteed, is it a problem?
        for (Statement statement : getSigma()) {
            stringBuilder.append(statement.toString()).append("\n");
        }

        return stringBuilder.toString();
    }

    public boolean hasStatement() {
        return !getSigma().isEmpty();
    }

    public boolean messageCanBeTaken(ContinuousVariable globalTime) {
        for (Message message : getQueue()) {
            if (message.checkBounds(globalTime)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ActorState> takeMessage(ContinuousVariable globalTime) {
        List<ActorState> result = new ArrayList<>();
        SoftwareState newSoftwareState = new SoftwareState(
                getActorName(),
                getDiscreteVariableValuation(),
                getQueue(),
                getSigma(),
                getLocalTime(),
                getResumeTime()
        );
        // TODO: !!!START FROM HERE!!!
        // FIXME: what should we do if we have more than one message that can be taken?
        Message message = newSoftwareState.queue.poll();
        BigDecimal tMin = globalTime.getUpperBound().min(message.getArrivalTime().getUpperBound());
        // TODO: update sigma(not the sigma in code, the evaluation function which is sigma in paper)
        // TODO: add message's parameter evaluation to discrete variable valuation (which is sigma in paper)
        // FIXME: message parameters are variables, not discrete variables. what should we about intervals?
        Map<String, Variable> parameters = message.getParameters();
        for (Map.Entry<String, Variable> entry : parameters.entrySet()) {
            if (entry.getValue() instanceof DiscreteVariable) {
                newSoftwareState.getDiscreteVariableValuation().put(entry.getKey(), (DiscreteVariable) entry.getValue());
            }
        }
        // TODO: remove the proper message from message bag
        // TODO: add body of the message to list of statement to be executed
        // FIXME: implement getMessageBody method
        CompilerUtil.getMessageBody(newSoftwareState.getActorName(), message.getServerName());
        // TODO: update resume time
        // CHECKME: why we should update resume time?
        // FIXME: what should be the name of the ContinuousVariable?
        this.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound()));

        result.add(newSoftwareState);

        // CHECKME: shouldn't it be <= instead of <?
        if (globalTime.getUpperBound().compareTo(message.getArrivalTime().getUpperBound()) < 0) {
            newSoftwareState = new SoftwareState(
                    getActorName(),
                    getDiscreteVariableValuation(),
                    getQueue(),
                    getSigma(),
                    getLocalTime(),
                    getResumeTime()
            );
            message = newSoftwareState.queue.poll();
            Message newMessage = new Message(
                    message.getSenderActor(),
                    message.getReceiverActor(),
                    message.getServerName(),
                    message.getParameters(),
                    new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), message.getArrivalTime().getUpperBound())
            );
            newSoftwareState.addMessage(newMessage);
            // FIXME: what epsilon means for sigma? should we set it to null? or should we set it to empty list?
            newSoftwareState.setSigma(new ArrayList<>());
            // FIXME: what epsilon means for resumeTime?
            newSoftwareState.setResumeTime(new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound()));

        }

        return result;
    }

}
