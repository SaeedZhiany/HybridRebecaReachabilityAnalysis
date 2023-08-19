package stateSpace;

import javax.annotation.Nonnull;
import java.util.HashMap;
import utils.StringSHA256;

public class HybridState {

    // CHECKME: should we add global time to this class?

    @Nonnull
    private HashMap<String, SoftwareState> softwareStates;
    @Nonnull
    private HashMap<String, PhysicalState> physicalStates;
    @Nonnull
    private CANNetworkState CANNetworkState;
    @Nonnull
    private String hashString;

    public HybridState() {
        this(new HashMap<>(), new HashMap<>(), new CANNetworkState());
    }

    public HybridState(HybridState hybridState) {
        this(hybridState.softwareStates, hybridState.physicalStates, hybridState.CANNetworkState);
    }

    public boolean equals(HybridState state) {
        String thisHashString = this.getHash();
        String stateHashString = state.getHash();
        if (thisHashString != stateHashString) {
            return false;
        }
        // TODO: make sure that the 2 states are actually equal
        // CHECKME: should we compare global time?
        return true;
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

    @Override
    public String toString() {
        // CHECKME: the order of the states is not guaranteed, is it a problem?
        // CHECKME: should we add global time to this string?
        StringBuilder stringBuilder = new StringBuilder();

        for (SoftwareState softwareState : softwareStates.values()) {
            stringBuilder.append(softwareState.toString());
            stringBuilder.append(";");
        }
        for (PhysicalState physicalState : physicalStates.values()) {
            stringBuilder.append(physicalState.toString());
            stringBuilder.append(";");
        }
        // stringBuilder.append(CANNetworkState.toString());
        return stringBuilder.toString();
    }

    // CHECKME: when should we call this method?
    private String updateHash() {
        return StringSHA256.hashString(this.toString());
    }

    private String getHash() {
        return this.hashString;
    }

    private void replaceActorState(PhysicalState physicalState) {
        physicalStates.replace(physicalState.actorName, physicalState);
    }
}
