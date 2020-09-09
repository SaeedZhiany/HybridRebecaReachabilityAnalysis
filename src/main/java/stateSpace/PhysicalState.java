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

public class PhysicalState extends ActorState {

    /**
     * key: variable name
     * value: variable current value
     */
    @Nonnull
    protected HashMap<String, ContinuousVariable> continuousVariableValuation;

    /**
     * current mode of physical actor
     */
    @Nullable
    private String mode;

    public PhysicalState(
            @Nonnull String actorName,
            @Nullable String mode,
            @Nonnull HashMap<String, DiscreteVariable> discreteVariableValuation,
            @Nonnull HashMap<String, ContinuousVariable> continuousVariableValuation,
            @Nonnull Queue<Map.Entry<String, HashMap<String, Number>>> queue,
            @Nonnull List<Statement> sigma,
            float localTime
    ) {
        super(actorName, discreteVariableValuation, queue, sigma, localTime);
        this.mode = mode;
        this.continuousVariableValuation = continuousVariableValuation;
    }

    @Nullable
    public String getMode() {
        return mode;
    }

    public void setMode(@Nullable String mode) {
        this.mode = mode;
    }

    @Nonnull
    public HashMap<String, ContinuousVariable> getContinuousVariableValuation() {
        return continuousVariableValuation;
    }

    @Nullable
    public ContinuousVariable getContinuousVariable(String name) {
        return this.continuousVariableValuation.get(name);
    }

    public void updateContinuousVariable(ContinuousVariable continuousVariable) {
        this.continuousVariableValuation.put(continuousVariable.getName(), continuousVariable);
    }
}
