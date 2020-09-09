package utils;

import dataStructure.AnalysisResult;
import dataStructure.AnalysisSafetyResult;
import sos.SOSExecutorContainer;
import stateSpace.HybridState;

import javax.annotation.Nonnull;
import java.util.*;

public class ReachabilityAnalyzer {

    private static final List<HybridState> processedStates = new ArrayList<>();
    private static final Queue<HybridState> unprocessedStates = new PriorityQueue<>();

    @Nonnull
    public static AnalysisResult analyzeHybridReachability(@Nonnull String modeFileName) throws Exception {
        CompilerUtil.compile(modeFileName);
        /*
         *  TODO create initial stateSpace and put into unprocessedStateSpaces
         *
         */
        do {
            final HybridState curHybridState = unprocessedStates.poll();
            final List<HybridState> newHybridStates = SOSExecutorContainer.generateNextStates(curHybridState);
            for (HybridState newHybridState : newHybridStates) {
                if (!isStateSpaceDuplicated(newHybridState)) {
                    unprocessedStates.add(newHybridState);
                }
            }
            processedStates.add(curHybridState);
        } while (!unprocessedStates.isEmpty());
        return new AnalysisResult(AnalysisSafetyResult.UNKNOWN, 0, new HashMap<>()); // TODO
    }

    private static boolean isStateSpaceDuplicated(HybridState newHybridState) {
        for (HybridState processedHybridState : processedStates) {
            // TODO check if it's generated before
        }
        return false;
    }
}
