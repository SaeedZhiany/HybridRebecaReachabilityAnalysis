package sos;

import stateSpace.ActorState;
import stateSpace.HybridState;
import stateSpace.PhysicalState;
import stateSpace.SoftwareState;
import visitors.SendStatementApplicableVisitor;
import visitors.SetModeApplicableVisitor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SetModeSOSExecutor extends AbstractSOSExecutor {
    private SetModeApplicableVisitor setModeApplicableVisitor = new SetModeApplicableVisitor();

    @Override
    public boolean isApplicable(HybridState hybridState) {
        for (PhysicalState physicalState : hybridState.getPhysicalStates().values()) {
            if (physicalState.hasStatement()) {
                // CHECKME: isIdle is same as isSuspended?
                if (!physicalState.isIdle()) {
                    return setModeApplicableVisitor.checkStatementApplicability(physicalState);
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    protected List<HybridState> execute(HybridState hybridState) {
        List<HybridState> result = new ArrayList<>();
        for (PhysicalState physicalState : applicablePhysicalStates(hybridState)) {
            List<HybridState> newHybridStates = hybridState.setModeStatement(physicalState);
            result.addAll(newHybridStates);
        }
        return result;
    }

    @Nonnull
    private List<PhysicalState> applicablePhysicalStates(HybridState hybridState) {
        List<PhysicalState> applicableStates = new ArrayList<>();
        for (PhysicalState physicalState : hybridState.getPhysicalStates().values()) {
            if (physicalState.hasStatement()) {
                // CHECKME: isIdle is same as isSuspended?
                if (!physicalState.isIdle()) {
                    if(setModeApplicableVisitor.checkStatementApplicability(physicalState)) {
                        applicableStates.add(physicalState);
                    }
                }
            }
        }
        return applicableStates;
    }
}
