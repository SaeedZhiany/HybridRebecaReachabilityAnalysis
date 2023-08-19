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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Actor: ").append(actorName).append("\n");
        stringBuilder.append("Mode: ").append(mode).append("\n");
        stringBuilder.append("Discrete Variables: ").append("\n");
        // CHECKME: order of the variables is not guaranteed, is it a problem?
        for (Map.Entry<String, DiscreteVariable> entry : discreteVariableValuation.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        stringBuilder.append("Continuous Variables: ").append("\n");
        // CHECKME: order of the variables is not guaranteed, is it a problem?
        for (Map.Entry<String, ContinuousVariable> entry : continuousVariableValuation.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        stringBuilder.append("Queue: ").append("\n");
        // CHECKME: order of the messages is not guaranteed, is it a problem?
        for (Map.Entry<String, HashMap<String, Number>> entry : queue) {
            stringBuilder.append(entry.getKey()).append(": ").append("\n");
            for (Map.Entry<String, Number> entry1 : entry.getValue().entrySet()) {
                // CHECKME: entry1.getValue() is Number, is it a problem?
                stringBuilder.append(entry1.getKey()).append(": ").append(entry1.getValue()).append("\n");
            }
        }

        stringBuilder.append("Sigma: ").append("\n");
        // CHECKME: order of the statements is not guaranteed, is it a problem?
        for (Statement statement : sigma) {
            stringBuilder.append(statement.toString()).append("\n");
        }

        stringBuilder.append("Local Time: ").append(localTime).append("\n");
        
        return stringBuilder.toString();
    }
}
