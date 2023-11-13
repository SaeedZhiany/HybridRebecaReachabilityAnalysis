package sos;

import stateSpace.ActorState;
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
        // FIXME: do this for physical states
        return false;
    }
    

    @Nonnull
    @Override
    protected List<HybridState> execute(HybridState hybridState) {
        List<HybridState> result = new ArrayList<>();
        for (SoftwareState softwareState : applicableStates(hybridState)) {
            // TODO: takeMessage method of HybridState can and should return multiple (at most 2?!) newHybridStates
            List<ActorState> generatedActorStates = hybridState.takeMessage(softwareState);
            for (ActorState actorState : generatedActorStates) {
                // FIXME: make a copy of this software state
                HybridState newHybridState = hybridState.copy();
                newHybridState.replaceActorState(actorState);
                result.add(newHybridState);
            }
        }
        // TODO: do the same thing for physical states
        return result;
    }

    @Nonnull
    private List<SoftwareState> applicableStates(HybridState hybridState) {
        List<SoftwareState> applicableStates = new ArrayList<>();
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (!softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    if (!softwareState.messageCanBeTaken(hybridState.getGlobalTime())) {
                        applicableStates.add(softwareState);
                    }
                } 
            }
        }
        return applicableStates;
    }

}
