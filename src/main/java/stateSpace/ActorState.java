package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class ActorState {

    @Nonnull
    protected String actorName;

    @Nonnull
    protected HashMap<String, Variable> variablesValuation;

    @Nonnull
    protected Set<Message> messageBag;

    @Nonnull
    protected List<Statement> sigma;

    protected float localTime;

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
        if (!sigma.isEmpty()) {
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

    public void addStatementsToFront(List<Statement> statements) {
        this.sigma.addAll(0, statements);
    }

    public void setVariablesValuation(@Nonnull HashMap<String, Variable> variablesValuation) {
        this.variablesValuation = variablesValuation;
    }
}
