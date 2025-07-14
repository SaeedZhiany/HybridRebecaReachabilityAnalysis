package sos;

import stateSpace.ActorState;
import stateSpace.HybridState;
import stateSpace.PhysicalState;
import stateSpace.SoftwareState;
import visitors.IfStatementApplicableVisitor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class IfStatementSOSExecutor extends AbstractSOSExecutor{

    private IfStatementApplicableVisitor ifStatementApplicableVisitor = new IfStatementApplicableVisitor();

    @Override
    public boolean isApplicable(HybridState hybridState) {
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    return ifStatementApplicableVisitor.checkStatementApplicability(softwareState);
                }
            }
        }
        for (PhysicalState physicalState : hybridState.getPhysicalStates().values()) {
            if (physicalState.hasStatement()) {
                // CHECKME: isIdle is same as isSuspended?
                if (!physicalState.isIdle()) {
                    return ifStatementApplicableVisitor.checkStatementApplicability(physicalState);
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
            List<HybridState> newHybridStates = hybridState.ifStatement(actorState);
            result.addAll(newHybridStates);
        }
        return result;
    }
    @Nonnull
    private List<SoftwareState> applicableSoftwareStates(HybridState hybridState) {
        List<SoftwareState> applicableStates = new ArrayList<>();
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    if(ifStatementApplicableVisitor.checkStatementApplicability(softwareState)) {
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
                    if(ifStatementApplicableVisitor.checkStatementApplicability(physicalState)) {
                        applicableStates.add(physicalState);
                    }
                }
            }
        }
        return applicableStates;
    }
}
