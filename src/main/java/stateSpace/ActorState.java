package stateSpace;

import dataStructure.DiscreteVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
     * key: message server name
     * value: a hashMap of parameters values
     * key: parameter name
     * value: parameter value
     */
    @Nonnull
    protected Queue<Map.Entry<String, HashMap<String, Number>>> queue;

    /**
     * list of statements that must be executed
     */
    @Nonnull
    protected List<Statement> sigma;

    /**
     * local time of actor
     */
    protected float localTime;

    public ActorState(
            @Nonnull String actorName,
            @Nonnull HashMap<String, DiscreteVariable> discreteVariablesValuation,
            @Nonnull Queue<Map.Entry<String, HashMap<String, Number>>> queue,
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

    @Nullable
    public Map.Entry<String, HashMap<String, Number>> nextMessage() {
        return queue.poll();
    }

    public void addMessage(Map.Entry<String, HashMap<String, Number>> message) {
        this.queue.add(message);
    }

    @Nonnull
    public List<Statement> getSigma() {
        return sigma;
    }

    public void setSigma(@Nonnull List<Statement> sigma) {
        this.sigma = sigma;
    }

    public float getLocalTime() {
        return localTime;
    }

    public void setLocalTime(float localTime) {
        if (localTime >= 0) {
            this.localTime = localTime;
        }
    }
}
