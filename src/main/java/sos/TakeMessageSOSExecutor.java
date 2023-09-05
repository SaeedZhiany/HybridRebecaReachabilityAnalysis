package sos;

import stateSpace.HybridState;
import stateSpace.SoftwareState;
import stateSpace.PhysicalState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TakeMessageSOSExecutor extends AbstractSOSExecutor {

    @Override
    public boolean isApplicable(HybridState hybridState) {
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (!softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    if (!softwareState.messageCanBeTaken(hybridState.getGlobalTime())) {
                        return true;
                    }
                } 
            }
        }
        // CHECKME : should we do these for physical states?
        return false;
    }
    

    @Nonnull
    @Override
    protected List<HybridState> execute(HybridState hybridState) {
        return new ArrayList<>();
    }
}
