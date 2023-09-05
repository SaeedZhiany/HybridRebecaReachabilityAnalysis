package stateSpace;

import javax.annotation.Nonnull;
import java.lang.StringBuilder;
import java.util.HashMap;
import utils.StringSHA256;
import dataStructure.ContinuousVariable;

public class HybridState {

    // CHECKME: should global time be non-null?
    @Nonnull
    private ContinuousVariable globalTime;
    @Nonnull
    private HashMap<String, SoftwareState> softwareStates;
    @Nonnull
    private HashMap<String, PhysicalState> physicalStates;
    @Nonnull
    private CANNetworkState CANNetworkState;
    @Nonnull
    private String hashString;

    public HybridState() {
        // FIXME: is this the correct way to initialize globalTime?
        ContinuousVariable globalTime = new ContinuousVariable("globalTime");
        this(globalTime, new HashMap<>(), new HashMap<>(), new CANNetworkState());
    }

    public HybridState(HybridState hybridState) {
        // CHECKME: aren't this attributes private?
        this(hybridState.globalTime, hybridState.softwareStates, hybridState.physicalStates, hybridState.CANNetworkState);
    }

    private HybridState(
            @Nonnull ContinuousVariable globalTime,
            @Nonnull HashMap<String, SoftwareState> softwareStates,
            @Nonnull HashMap<String, PhysicalState> physicalStates,
            @Nonnull stateSpace.CANNetworkState CANNetworkState
    ) {
        this.globalTime = globalTime;
        this.softwareStates = softwareStates;
        this.physicalStates = physicalStates;
        this.CANNetworkState = CANNetworkState;
        this.hashString = updateHash();
    }

    public boolean equals(HybridState state) {
        String thisHashString = this.getHash();
        String stateHashString = state.getHash();
        if (thisHashString != stateHashString) {
            return false;
        }
        // TODO: make sure that the 2 states are actually equal
        return true;
    }

    private void replaceActorState(SoftwareState softwareState) {
        softwareStates.replace(softwareState.actorName, softwareState);
    }

    private void replaceActorState(PhysicalState physicalState) {
        physicalStates.replace(physicalState.actorName, physicalState);
    }

    @Override
    public String toString() {
        // CHECKME: the order of the states is not guaranteed, is it a problem?
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(globalTime.toString());

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

}
