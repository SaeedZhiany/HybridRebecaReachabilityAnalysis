package stateSpace;

import dataStructure.DiscreteVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SoftwareState extends ActorState {

    /**
     * resume time of actor
     */
    private float resumeTime;

    public SoftwareState(
            @Nonnull String actorName,
            @Nonnull HashMap<String, DiscreteVariable> discreteVariableValuation,
            @Nonnull Queue<Map.Entry<String, HashMap<String, Number>>> queue,
            @Nonnull List<Statement> sigma,
            float localTime,
            float resumeTime
    ) {
        super(actorName, discreteVariableValuation, queue, sigma, localTime);
        this.resumeTime = resumeTime;
    }

    public float getResumeTime() {
        return resumeTime;
    }

    public void setResumeTime(float resumeTime) {
        if (resumeTime >= 0) {
            this.resumeTime = resumeTime;
        }
    }

    @Override
    public String toString() {
        // CHECKME: which variables should be included in the string?
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Actor: ").append(getActorName()).append("\n");
        stringBuilder.append("Resume Time: ").append(getResumeTime()).append("\n");
        stringBuilder.append("Local Time: ").append(getLocalTime()).append("\n");

        stringBuilder.append("Discrete Variable Valuation: ").append("\n");
        // CHECKME: order of the variables is not guaranteed, is it a problem?
        for (Map.Entry<String, DiscreteVariable> entry : getDiscreteVariableValuation().entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        stringBuilder.append("Queue: ").append("\n");
        // CHECKME: order of the messages is not guaranteed, is it a problem?
        for (Map.Entry<String, HashMap<String, Number>> entry : getQueue().entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append("\n");
            for (Map.Entry<String, Number> entry1 : entry.getValue().entrySet()) {
                // CHECKME: entry1.getValue() is Number, is it a problem?
                stringBuilder.append(entry1.getKey()).append(": ").append(entry1.getValue()).append("\n");
            }
        }

        stringBuilder.append("Sigma: ").append("\n");
        // CHECKME: order of the statements is not guaranteed, is it a problem?
        for (Statement statement : getSigma()) {
            stringBuilder.append(statement.toString()).append("\n");
        }

        return stringBuilder.toString();
    }
}
