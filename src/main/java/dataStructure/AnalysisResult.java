package dataStructure;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.HashMap;

public class AnalysisResult {
    @Nonnull
    private AnalysisSafetyResult analysisSafetyResult;

    private float timeElapsed;

    @Nonnull
    private HashMap<String, BigDecimal> continuesVariablesValues;


    public AnalysisResult(
            @Nonnull AnalysisSafetyResult analysisSafetyResult,
            float timeElapsed,
            @Nonnull HashMap<String, BigDecimal> continuesVariablesValues
    ) {
        this.analysisSafetyResult = analysisSafetyResult;
        this.timeElapsed = timeElapsed;
        this.continuesVariablesValues = continuesVariablesValues;
    }

    @Nonnull
    public AnalysisSafetyResult getAnalysisSafetyResult() {
        return analysisSafetyResult;
    }

    public float getTimeElapsed() {
        return timeElapsed;
    }

    @Nonnull
    public HashMap<String, BigDecimal> getContinuesVariablesValues() {
        return continuesVariablesValues;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "analysisSafetyResult=" + analysisSafetyResult +
                ", timeElapsed=" + timeElapsed +
                ", continuesVariablesValues=" + continuesVariablesValues +
                '}';
    }
}
