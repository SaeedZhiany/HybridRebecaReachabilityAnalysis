package stateSpace;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class HybridState {

    @Nonnull
    private HashMap<String, SoftwareState> softwareStates;
    @Nonnull
    private HashMap<String, PhysicalState> physicalStates;
    @Nonnull
    private CANNetworkState CANNetworkState;

    public HybridState() {
        this(new HashMap<>(), new HashMap<>(), new CANNetworkState());
    }

    public HybridState(HybridState hybridState) {
        this(hybridState.softwareStates, hybridState.physicalStates, hybridState.CANNetworkState);
    }

    private HybridState(
            @Nonnull HashMap<String, SoftwareState> softwareStates,
            @Nonnull HashMap<String, PhysicalState> physicalStates,
            @Nonnull stateSpace.CANNetworkState CANNetworkState
    ) {
        this.softwareStates = softwareStates;
        this.physicalStates = physicalStates;
        this.CANNetworkState = CANNetworkState;
    }

    private void replaceActorState(SoftwareState softwareState) {
        softwareStates.replace(softwareState.actorName, softwareState);
    }

    private void replaceActorState(PhysicalState physicalState) {
        physicalStates.replace(physicalState.actorName, physicalState);
    }
}
