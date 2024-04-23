package sos;

import stateSpace.ActorState;
import stateSpace.HybridState;
import stateSpace.PhysicalState;
import stateSpace.SoftwareState;
import visitors.SendStatementApplicableVisitor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SendStatementSOSExecuter extends AbstractSOSExecutor {
    private SendStatementApplicableVisitor sendStatementApplicableVisitor = new SendStatementApplicableVisitor();

    @Override
    public boolean isApplicable(HybridState hybridState) {
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    return sendStatementApplicableVisitor.checkStatementApplicability(softwareState);
                }
            }
        }
        for (PhysicalState physicalState : hybridState.getPhysicalStates().values()) {
            if (physicalState.hasStatement()) {
                // CHECKME: isIdle is same as isSuspended?
                if (!physicalState.isIdle()) {
                    return sendStatementApplicableVisitor.checkStatementApplicability(physicalState);
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    protected List<HybridState> execute(HybridState hybridState) {
        List<HybridState> result = new ArrayList<>();
        for (SoftwareState softwareState : applicableSoftwareStates(hybridState)) {
            // TODO: takeMessage method of HybridState can and should return multiple (at most 2?!) newHybridStates
            List<ActorState> generatedActorStates = hybridState.takeMessage(softwareState);
            for (ActorState actorState : generatedActorStates) {
                // FIXME: make a copy of this software state
                HybridState newHybridState = new HybridState(hybridState);
                newHybridState.replaceActorState(actorState);
                result.add(newHybridState);
            }
        }
        // TODO: do the same thing for physical states
        for (PhysicalState physicalState : applicablePhysicalStates(hybridState)) {
            List<ActorState> generatedActorStates = hybridState.takeMessage(physicalState);
            for (ActorState actorState : generatedActorStates) {
                HybridState newHybridState = new HybridState(hybridState);
                newHybridState.replaceActorState(actorState);
                result.add(newHybridState);
            }
        }
        return result;
    }

    @Nonnull
    private List<SoftwareState> applicableSoftwareStates(HybridState hybridState) {
        List<SoftwareState> applicableStates = new ArrayList<>();
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    if(sendStatementApplicableVisitor.checkStatementApplicability(softwareState)) {
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
            if (physicalState.hasStatement()) {
                // CHECKME: isIdle is same as isSuspended?
                if (!physicalState.isIdle()) {
                    if(sendStatementApplicableVisitor.checkStatementApplicability(physicalState)) {
                        applicableStates.add(physicalState);
                    }
                }
            }
        }
        return applicableStates;
    }
}