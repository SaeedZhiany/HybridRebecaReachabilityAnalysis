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
}
