package sos;

import stateSpace.HybridState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonTimeProgressSOSExecutor {


    private final List<AbstractSOSExecutor> sosExecutors = new ArrayList<>() {{
        add(new AssignmentStatementSOSExecutor());
        add(new DelaySOSExecuter());
        add(new TakeMessageSOSExecutor());
        add(new SendStatementSOSExecuter());
        add(new IfStatementSOSExecutor());
    }};

    public List<HybridState> generateNextStates(HybridState hybridState) {
        final Map<String, HybridState> totalStates = new HashMap<>();
        Map<String, HybridState> nextRoundStates = new HashMap<>();
        Map<String, HybridState> currentRoundStates = new HashMap<>();

        hybridState.updateHash();
        currentRoundStates.put(hybridState.getHash(), hybridState);
        boolean isProcessEnd = false;
        while (!isProcessEnd) {
            isProcessEnd = true;
            for (AbstractSOSExecutor executor : sosExecutors) {
                for (Map.Entry<String, HybridState> entry : currentRoundStates.entrySet()) {
                    if (executor.isApplicable(entry.getValue())) {
                        isProcessEnd = false;
                        List<HybridState> generatedHybridStates = executor.execute(entry.getValue());
                        for (HybridState generatedHybridState : generatedHybridStates) {
                            generatedHybridState.updateHash();
                            nextRoundStates.put(generatedHybridState.getHash(), generatedHybridState);
                        }
                    }
                }
            }
            totalStates.putAll(currentRoundStates);
            currentRoundStates = new HashMap<>(nextRoundStates);
            nextRoundStates.clear();
        }
        return new ArrayList<>(totalStates.values());
    }
}
