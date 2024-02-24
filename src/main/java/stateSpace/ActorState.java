package stateSpace;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteVariable;
import dataStructure.Variable;
import jdk.internal.net.http.common.Pair;
import org.checkerframework.checker.units.qual.A;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import com.rits.cloning.Cloner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.lang.StringBuilder;

public abstract class ActorState {

    @Nonnull
    protected String actorName;

    /**
     * key: variable name
     * value: variable current value
     */
    @Nonnull
    protected HashMap<String, Variable> variablesValuation;

    /**
     * queue of messages that must be taken care of
     */
    @Nonnull
    protected Set<Message> messageBag;

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

    private Cloner cloner;

    @Nonnull
    protected HashMap<String, ActorState> childStates;
    @Nonnull
    protected HashMap<String, ActorState> parentStates;

    public ActorState(
            @Nonnull String actorName,
            @Nonnull HashMap<String, Variable> variablesValuation,
            @Nonnull Set<Message> messageBag,
            @Nonnull List<Statement> sigma,
            float localTime
    ) {
        this.actorName = actorName;
        this.variablesValuation = variablesValuation;
        this.messageBag = messageBag;
        this.sigma = sigma;
        this.localTime = localTime;
    }

    @Nonnull
    public String getActorName() {
        return actorName;
    }

    @Nonnull
    public HashMap<String, Variable> getVariablesValuation() {
        return variablesValuation;
    }

    @Nullable
    public Variable getVariable(String name) {
        return this.variablesValuation.get(name);
    }

    public void updateVariable(Variable variable) {
        this.variablesValuation.put(variable.getName(), variable);
    }

    @Nonnull
    public Set<Message> getMessageBag() {
        return messageBag;
    }

    @Nullable
    // CHECKME: is this the correct way to write this method?
    public Message nextMessage() {
        return messageBag.iterator().next();
    }

    public void addMessage(Message message) {
        this.messageBag.add(message);
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

    public HashMap<String, Variable> getVariableValuation() {
        return variablesValuation;
    }

    public List<ActorState> takeMessage (ContinuousVariable globalTime) {
        return null;
    }

    // CHECKME: should put it here?
    public boolean hasStatement() {
        return !getSigma().isEmpty();
    }

    public void addVariable(Variable variable) {
        this.variablesValuation.put(variable.getName(), variable);
    }

    public void addVariables(HashMap<String, Variable> variables) {
        this.variablesValuation.putAll(variables);
    }

    public void removeMessage(Message message) {
        this.messageBag.remove(message);
    }

    public void addStatements(List<Statement> statements) {
        this.sigma.addAll(statements);
    }
}
