package stateSpace;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.lang.StringBuilder;

public abstract class ActorState {

    @Nonnull
    protected String actorName;

    /**
     * key: variable name
     * value: variable current value
     */
    @Nonnull
    protected HashMap<String, DiscreteVariable> discreteVariablesValuation;

    /**
     * queue of messages that must be taken care of
     */
    @Nonnull
    protected Queue<Message> queue;

    // CHECKME: what exactly are statements? how to implement toString()?
    /**
     * list of statements that must be executed
     */
    @Nonnull
    protected List<Statement> sigma;

    /**
     * local time of actor
     */
    // CHECKME: what does local time used for?
    protected float localTime;

    public ActorState(
            @Nonnull String actorName,
            @Nonnull HashMap<String, DiscreteVariable> discreteVariablesValuation,
            @Nonnull Queue<Message> queue,
            @Nonnull List<Statement> sigma,
            float localTime
    ) {
        this.actorName = actorName;
        this.discreteVariablesValuation = discreteVariablesValuation;
        this.queue = queue;
        this.sigma = sigma;
        this.localTime = localTime;
    }

    @Nonnull
    public String getActorName() {
        return actorName;
    }

    @Nonnull
    public HashMap<String, DiscreteVariable> getDiscreteVariablesValuation() {
        return discreteVariablesValuation;
    }

    @Nullable
    public DiscreteVariable getVariable(String name) {
        return this.discreteVariablesValuation.get(name);
    }

    public void updateVariable(DiscreteVariable discreteVariable) {
        this.discreteVariablesValuation.put(discreteVariable.getName(), discreteVariable);
    }

    @Nonnull
    public Queue<Message> getQueue() {
        return queue;
    }

    @Nullable
    public Message nextMessage() {
        return queue.poll();
    }

    public void addMessage(Message message) {
        this.queue.add(message);
    }

    @Nonnull
    public List<Statement> getSigma() {
        return sigma;
    }

    public void setSigma(@Nonnull List<Statement> sigma) {
        this.sigma = sigma;
    }

    @Nullable
    public Statement nextStatement() {
        if (sigma.size() > 0) {
            return sigma.remove(0);
        }
        return null;
    }

    public float getLocalTime() {
        return localTime;
    }

    public void setLocalTime(float localTime) {
        if (localTime >= 0) {
            this.localTime = localTime;
        }
    }

    public HashMap<String, DiscreteVariable> getDiscreteVariableValuation() {
        return discreteVariablesValuation;
    }

    public List<ActorState> takeMessage (ContinuousVariable globalTime) {
        return null;
    }

}
