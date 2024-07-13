package sos;

import stateSpace.HybridState;

import java.util.ArrayList;
import java.util.List;

public class SOSExecutorContainer {

    private static final List<AbstractSOSExecutor> sosExecutors = new ArrayList<AbstractSOSExecutor>() {{
        add(new AssignmentStatementSOSExecutor());
        add(new TimeProgressSOSExecutor());
        add(new TakeMessageSOSExecutor());
        // TODO add all SOSExecutors here
    }};

    public static List<HybridState> generateNextStates(HybridState hybridState) {
        final List<HybridState> hybridStates = new ArrayList<>();
        sosExecutors.forEach(SOSExecutor -> hybridStates.addAll(SOSExecutor.tryToExecute(hybridState)));
        return hybridStates;
    }
}
