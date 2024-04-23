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

    ContinuousVariable createContinuousVariable(BigDecimal lowerBound, BigDecimal upperBound) {
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
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(0), new BigDecimal(10));
        HybridState hybridState = new HybridState(globalTime, softwareStateMap, new HashMap<>(), new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 1 message in 1 SoftwareState's messageBag return true")
    void TestisApplicableWithOneMessageInsideGlobalTimeRange() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;
        SoftwareState softwareState = new SoftwareState("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime"));
        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));

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
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(0), new BigDecimal(10));
        HybridState hybridState = new HybridState(globalTime, new HashMap<>(), physicalStateMap, new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }

    @Test
    @Tag("test isApplicable with 1 message in 1 PhysicalState's messageBag and not in idle mode return true")
    void TestisApplicableWithOneMessageInPhysicalStateNotIdle() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;
        PhysicalState physicalState = new PhysicalState("init", "mode", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));

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
        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));

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
        SoftwareState softwareState = new SoftwareState("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, createContinuousVariable(new BigDecimal(0), new BigDecimal(6)));
        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                messageParams,
                messageArrivalTime);
        softwareState.addMessage(message);
        softwareState.setResumeTime(new ContinuousVariable("resumeTime", new BigDecimal(0), new BigDecimal(6)));
        HashMap<String, SoftwareState> softwareStateMap = new HashMap<>();
        softwareStateMap.put("init", softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateMap, new HashMap<>(), new CANNetworkState());
        assertFalse(this.takeMessageSOSExecutor.isApplicable(hybridState));
    }



//
//    @Test
//    @Tag("takeMessage with 1 message in messageBag return list with 1 message with t3 <= t1 && t4 <= t2")
//    void TestTakeMessageWithOneMessageInsideGlobalTimeRange() {
//        int messageArrivalLowerBound = 1;
//        int messageArrivalUpperBound = 5;
//
//        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
//        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
//
//        HashMap<String, Variable> messageParams = new HashMap<>();
//        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));
//
//        Message message = new Message(
//                "sender",
//                "receiver",
//                "content",
//                messageParams,
//                messageArrivalTime);
//        this.softwareState.addMessage(message);
//        List<ActorState> expected = new ArrayList<>();
//        try (MockedStatic<CompilerUtil> mockedCompiler = mockStatic(CompilerUtil.class)) {
//            List<Statement> messageBody = new ArrayList<>();
//            Statement mockedStatement = mock(Statement.class);
//            messageBody.add(mockedStatement);
//            mockedCompiler.when(() -> CompilerUtil.getMessageBody(anyString(), anyString())).thenReturn(messageBody);
//
//            ActorState expectedSoftwareState = new SoftwareState(
//                    "init",
//                    messageParams,
//                    new HashSet<>(),
//                    messageBody,
//                    0,
//                    new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound())
//            );
//            expected.add(expectedSoftwareState);
//
//            List<ActorState> actual = this.softwareState.takeMessage(globalTime);
//            assertEquals(expected.size(), actual.size());
//            assertEquals(expected.get(0).getActorName(), actual.get(0).getActorName());
//            assertEquals(expected.get(0).getVariableValuation(), actual.get(0).getVariableValuation());
//            assertTrue(actual.get(0).getMessageBag().isEmpty());
//            assertEquals(expected.get(0).getSigma(), actual.get(0).getSigma());
//            assertEquals(expected.get(0).getLocalTime(), actual.get(0).getLocalTime());
//            assertInstanceOf(SoftwareState.class, actual.get(0));
//            assertEquals(((SoftwareState) expected.get(0)).getResumeTime(), ((SoftwareState) actual.get(0)).getResumeTime());
//        }
//
//    }
//
//    @Test
//    @Tag("takeMessage with 1 message in messageBag return list with 1 message with t3 <= t1 && t4 > t2")
//    void TestTakeMessageWithOneMessageOutOfGlobalTimeRange() {
//        int messageArrivalLowerBound = 1; // t3
//        int messageArrivalUpperBound = 5; // t4
//
//        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
//        ContinuousVariable globalTime = createContinuousVariable(
//                new BigDecimal(messageArrivalLowerBound), // t1
//                new BigDecimal(messageArrivalUpperBound - 1) // t2
//        );
//
//        HashMap<String, Variable> messageParams = new HashMap<>();
//        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));
//
//        Message message = new Message(
//                "sender",
//                "receiver",
//                "content",
//                messageParams,
//                messageArrivalTime);
//        this.softwareState.addMessage(message);
//        List<ActorState> expected = new ArrayList<>();
//        try (MockedStatic<CompilerUtil> mockedCompiler = mockStatic(CompilerUtil.class)) {
//            List<Statement> messageBody = new ArrayList<>();
//            Statement mockedStatement = mock(Statement.class);
//            messageBody.add(mockedStatement);
//            mockedCompiler.when(() -> CompilerUtil.getMessageBody(anyString(), anyString())).thenReturn(messageBody);
//
//            ActorState expectedSoftwareState1 = new SoftwareState(
//                    "init",
//                    messageParams,
//                    new HashSet<>(),
//                    messageBody,
//                    0,
//                    new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound())
//            );
//
//            ActorState expectedSoftwareState2 = new SoftwareState(
//                    "init",
//                    new HashMap<>(),
//                    new HashSet<>(),// Should be one message with arrival time = [t2, t4]
//                    new ArrayList<>(),
//                    0,
//                    new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound())
//            );
//            expected.add(expectedSoftwareState1);
//            expected.add(expectedSoftwareState2);
//
//
//            List<ActorState> actual = this.softwareState.takeMessage(globalTime);
//            assertEquals(expected.size(), actual.size());
//            assertEquals(expected.get(0).getActorName(), actual.get(0).getActorName());
//            assertEquals(expected.get(0).getVariableValuation(), actual.get(0).getVariableValuation());
//            assertEquals(expected.get(0).getSigma(), actual.get(0).getSigma());
//            assertTrue(actual.get(0).getMessageBag().isEmpty());
//            assertEquals(expected.get(0).getLocalTime(), actual.get(0).getLocalTime());
//            assertInstanceOf(SoftwareState.class, actual.get(0));
//            assertEquals(((SoftwareState) expected.get(0)).getResumeTime(), ((SoftwareState) actual.get(0)).getResumeTime());
//
//
//
//            assertEquals(expected.get(1).getActorName(), actual.get(1).getActorName());
//            assertEquals(expected.get(1).getVariableValuation(), actual.get(1).getVariableValuation());
//            assertEquals(expected.get(1).getSigma(), actual.get(1).getSigma());
//            assertEquals(expected.get(1).getLocalTime(), actual.get(1).getLocalTime());
//            assertInstanceOf(SoftwareState.class, actual.get(1));
//            assertEquals(((SoftwareState) expected.get(1)).getResumeTime(), ((SoftwareState) actual.get(1)).getResumeTime());
//
//            // Check message bag of second state
//            assertEquals(1, actual.get(1).getMessageBag().size());
//            List<Message> remainMessages = new ArrayList<>(actual.get(1).getMessageBag());
//            assertEquals(new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), messageArrivalTime.getUpperBound()), remainMessages.get(0).getArrivalTime());
//
//        }
//    }
}