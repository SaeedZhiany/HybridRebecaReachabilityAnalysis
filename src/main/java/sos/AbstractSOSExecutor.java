package sos;

import stateSpace.HybridState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSOSExecutor {

    @Nonnull
    protected List<HybridState> tryToExecute(HybridState hybridState) {
        if (isApplicable(hybridState)) {
            return execute(hybridState);
        }
        return new ArrayList<>();
    }

    public abstract boolean isApplicable(HybridState hybridState);

    @Nonnull
    protected abstract List<HybridState> execute(HybridState hybridState);
}
