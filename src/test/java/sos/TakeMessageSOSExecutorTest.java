package sos;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import stateSpace.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TakeMessageSOSExecutorTest {
    private TakeMessageSOSExecutor takeMessageSOSExecutor;

    ContinuousVariable createContinuousVariable(Double lowerBound, Double upperBound) {
        return new ContinuousVariable("continuousVariable", lowerBound, upperBound);
    }

    @BeforeEach
    void setUp() {
        this.takeMessageSOSExecutor = new TakeMessageSOSExecutor();
    }

    @Test
    @Tag("test isApplicable with 0 message in 1 SoftwareState's messageBag return false")
    void TestisApplicableWithZeroMessage() {
        HashMap<String, SoftwareState> softwareStateMap = new HashMap<>();
        SoftwareState softwareState = new SoftwareState("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime"));
        softwareStateMap.put("init", softwareState);
        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(0), Double.valueOf(10));
        HybridState hybridState = new HybridState(globalTime, softwareStateMap, new HashMap<>(), new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 1 message in 1 SoftwareState's messageBag return true")
    void TestisApplicableWithOneMessageInsideGlobalTimeRange() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;
        SoftwareState softwareState = new SoftwareState("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime"));
        ContinuousVariable messageArrivalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                messageParams,
                messageArrivalTime);
        softwareState.addMessage(message);
        HashMap<String, SoftwareState> softwareStateMap = new HashMap<>();
        softwareStateMap.put("init", softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateMap, new HashMap<>(), new CANNetworkState());
        assertTrue(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 0 message in 1 PhysicalState's messageBag and not in idle mode return false")
    void TestisApplicableWithZeroMessageInPhysicalStateNotIdle() {
        HashMap<String, PhysicalState> physicalStateMap = new HashMap<>();
        PhysicalState physicalState = new PhysicalState("init", "mode", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        physicalStateMap.put("init", physicalState);
        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(0), Double.valueOf(10));
        HybridState hybridState = new HybridState(globalTime, new HashMap<>(), physicalStateMap, new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 1 message in 1 PhysicalState's messageBag and not in idle mode return true")
    void TestisApplicableWithOneMessageInPhysicalStateNotIdle() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;
        PhysicalState physicalState = new PhysicalState("init", "mode", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        ContinuousVariable messageArrivalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                messageParams,
                messageArrivalTime);
        physicalState.addMessage(message);
        HashMap<String, PhysicalState> physicalStateMap = new HashMap<>();
        physicalStateMap.put("init", physicalState);
        HybridState hybridState = new HybridState(globalTime, new HashMap<>(), physicalStateMap, new CANNetworkState());
        assertTrue(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 1 message in 1 PhysicalState's messageBag and in idle mode return false")
    void TestisApplicableWithOneMessageInPhysicalStateIdle() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;
        PhysicalState physicalState = new PhysicalState("init", "none", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        ContinuousVariable messageArrivalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                messageParams,
                messageArrivalTime);
        physicalState.addMessage(message);
        HashMap<String, PhysicalState> physicalStateMap = new HashMap<>();
        physicalStateMap.put("init", physicalState);
        HybridState hybridState = new HybridState(globalTime, new HashMap<>(), physicalStateMap, new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 1 message in 1 suspended SoftwareState's messageBag return false")
    void TestisApplicableWithOneMessageInSuspendedSoftwareState() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;
        SoftwareState softwareState = new SoftwareState("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, createContinuousVariable(Double.valueOf(0), Double.valueOf(6)));
        ContinuousVariable messageArrivalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound));

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                messageParams,
                messageArrivalTime);
        softwareState.addMessage(message);
        softwareState.setResumeTime(new ContinuousVariable("resumeTime", Double.valueOf(5), Double.valueOf(6)));
        HashMap<String, SoftwareState> softwareStateMap = new HashMap<>();
        softwareStateMap.put("init", softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateMap, new HashMap<>(), new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }
}