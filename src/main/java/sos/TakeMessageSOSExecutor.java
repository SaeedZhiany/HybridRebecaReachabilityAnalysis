package sos;

import stateSpace.HybridState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TakeMessageSOSExecutor extends AbstractSOSExecutor {

    @Override
    public boolean isApplicable(HybridState hybridState) {
        return false;
    }

    @Nonnull
    @Override
    protected List<HybridState> execute(HybridState hybridState) {
        return new ArrayList<>();
    }
}
