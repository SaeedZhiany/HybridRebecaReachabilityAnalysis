package sos;

import stateSpace.ActorState;
import stateSpace.HybridState;
import stateSpace.PhysicalState;
import stateSpace.SoftwareState;
import visitors.DelayApplicableVisitor;
import visitors.SendStatementApplicableVisitor;
import visitors.Visitor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DelaySOSExecuter extends AbstractSOSExecutor{

    private DelayApplicableVisitor delayApplicableVisitor = new DelayApplicableVisitor();

    @Override
    public boolean isApplicable(HybridState hybridState) {
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    return delayApplicableVisitor.checkStatementApplicability(softwareState);
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
            List<HybridState> newHybridState = hybridState.delayStatement(softwareState);
            result.addAll(newHybridState);
        }
        return result;
    }
    @Nonnull
    private List<SoftwareState> applicableSoftwareStates(HybridState hybridState) {
        List<SoftwareState> applicableStates = new ArrayList<>();
        for (SoftwareState softwareState : hybridState.getSoftwareStates().values()) {
            if (softwareState.hasStatement()) {
                if (!hybridState.isSuspended(softwareState.getResumeTime())) {
                    if(delayApplicableVisitor.checkStatementApplicability(softwareState)) {
                        applicableStates.add(softwareState);
                    }
                }
            }
        }
        return applicableStates;
    }
}
