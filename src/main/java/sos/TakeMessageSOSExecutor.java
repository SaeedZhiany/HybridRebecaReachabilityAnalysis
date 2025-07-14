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
                    if (softwareState.messageCanBeTaken(hybridState.getGlobalTime())) {
                        return true;
                    }
                } 
            }
        }
        // FIXME: do this for physical states
        for (PhysicalState physicalState : hybridState.getPhysicalStates().values()) {
            if (!physicalState.hasStatement()) {
                if (!physicalState.isIdle()) {
                    if (physicalState.messageCanBeTaken(hybridState.getGlobalTime())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    protected List<HybridState> execute(HybridState hybridState) {
        List<HybridState> result = new ArrayList<>();
        List<ActorState> actorStates = new ArrayList<>();
        actorStates.addAll(applicableSoftwareStates(hybridState));
        actorStates.addAll(applicablePhysicalStates(hybridState));
        for (ActorState actorState : actorStates) {
            // TODO: takeMessage method of HybridState can and should return multiple (at most 2?!) newHybridStates
            List<HybridState> generatedHybridState = hybridState.takeMessage(actorState);
            result.addAll(generatedHybridState);
        }
        return result;
    }

    @Nonnull
    private List<SoftwareState> applicableSoftwareStates(HybridState hybridState) {
        List<SoftwareState> applicableStates = new ArrayList<>();
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (!softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    if (softwareState.messageCanBeTaken(hybridState.getGlobalTime())) {
                        applicableStates.add(softwareState);
                    }
                } 
            }
        }
        return applicableStates;
    }

    @Nonnull
    private List<PhysicalState> applicablePhysicalStates(HybridState hybridState) {
        List<PhysicalState> applicableStates = new ArrayList<>();
        for (PhysicalState physicalState : hybridState.getPhysicalStates().values()) {
            if (!physicalState.hasStatement()) {
                if (!physicalState.isIdle()) {
                    if (physicalState.messageCanBeTaken(hybridState.getGlobalTime())) {
                        applicableStates.add(physicalState);
                    }
                }
            }
        }
        return applicableStates;
    }
}
