package stateSpace;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.Statement;
import utils.CompilerUtil;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class PhysicalStateTest {

    private PhysicalState physicalState;

    @BeforeEach
    void setUp() {
        physicalState = new PhysicalState("init", "mode1", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
    }

    ContinuousVariable createContinuousVariable(BigDecimal lowerBound, BigDecimal upperBound) {
        return new ContinuousVariable("continuousVariable", lowerBound, upperBound);
    }

    @Test
    @Tag("messageCanBeTaken with 0 message in messageBag return false")
    void TestMessageCanBeTakenWithZeroMessage() {
        assertFalse(this.physicalState.messageCanBeTaken(createContinuousVariable(new BigDecimal(0), new BigDecimal(10))));
    }

    @Test
    @Tag("messageCanBeTaken with 1 message in messageBag return true")
    void TestMessageCanBeTakenWithOneMessageReturnTrue() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message);
        assertTrue(this.physicalState.messageCanBeTaken(createContinuousVariable(
                new BigDecimal(messageArrivalLowerBound + 1),
                new BigDecimal(messageArrivalUpperBound
                ))));
    }

    @Test
    @Tag("messageCanBeTaken with 1 message in messageBag return false")
    void TestMessageCanBeTakenWithOneMessageReturnFalse() {
        int messageArrivalLowerBound = 1;
        int messageArrivalUpperBound = 5;

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message);
        assertFalse(this.physicalState.messageCanBeTaken(createContinuousVariable(
                new BigDecimal(messageArrivalLowerBound - 1),
                new BigDecimal(messageArrivalUpperBound
                ))));
    }

    @Test
    @Tag("messageCanBeTaken with more than 1 message and 1 message can be taken in messageBag return true")
    void TestMessageCanBeTakenWithMoreThanOneMessageReturnTrue() {
        int message1ArrivalLowerBound = 1;
        int message1ArrivalUpperBound = 5;

        int message2ArrivalLowerBound = message1ArrivalLowerBound + 2;
        int message2ArrivalUpperBound = message1ArrivalUpperBound;

        int globalTimeLowerBound = message1ArrivalLowerBound + 1;
        int globalTimeUpperBound = message1ArrivalUpperBound;

        Message message1 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message1ArrivalLowerBound), new BigDecimal(message1ArrivalUpperBound)));
        Message message2 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message2ArrivalLowerBound), new BigDecimal(message2ArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message1);
        this.physicalState.getMessageBag().add(message2);
        assertTrue(this.physicalState.messageCanBeTaken(createContinuousVariable(
                new BigDecimal(globalTimeLowerBound),
                new BigDecimal(globalTimeUpperBound)
        )));
    }

    @Test
    @Tag("messageCanBeTaken with more than 1 message return false")
    void TestMessageCanBeTakenWithMoreThanOneMessageReturnFalse() {
        int message1ArrivalLowerBound = 1;
        int message1ArrivalUpperBound = 5;

        int message2ArrivalLowerBound = message1ArrivalLowerBound + 2;
        int message2ArrivalUpperBound = message1ArrivalUpperBound;

        int globalTimeLowerBound = message1ArrivalLowerBound - 1;
        int globalTimeUpperBound = message1ArrivalUpperBound;

        Message message1 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message1ArrivalLowerBound), new BigDecimal(message1ArrivalUpperBound)));
        Message message2 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message2ArrivalLowerBound), new BigDecimal(message2ArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message1);
        this.physicalState.getMessageBag().add(message2);
        assertFalse(this.physicalState.messageCanBeTaken(createContinuousVariable(
                new BigDecimal(globalTimeLowerBound),
                new BigDecimal(globalTimeUpperBound)
        )));
    }

    @Test
    @Tag("messageCanBeTaken with setMode message return false")
    void TestMessageCanBeTakenWithSetModeMessageReturnFalse() {
        int messageArrivalLowerBound = 1;
        int messageArrivalUpperBound = 5;

        Message message = new Message(
                "sender",
                "receiver",
                "setMode",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message);
        assertFalse(this.physicalState.messageCanBeTaken(createContinuousVariable(
                new BigDecimal(messageArrivalLowerBound + 1),
                new BigDecimal(messageArrivalUpperBound
                ))));
    }

    @Test
    @Tag("getMessagesToBeTaken with 0 message in messageBag return empty list")
    void TestGetMessagesToBeTakenWithZeroMessage() {
        assertTrue(this.physicalState.getMessagesToBeTaken(createContinuousVariable(new BigDecimal(0), new BigDecimal(10))).isEmpty());
    }

    @Test
    @Tag("getMessagesToBeTaken with 1 message in messageBag return list with 1 message")
    void TestGetMessagesToBeTakenWithOneMessage() {
        int messageArrivalLowerBound = 0;
        int messageArrivalUpperBound = 5;

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message);
        List<Message> expected = new ArrayList<>();
        expected.add(message);
        assertEquals(expected, this.physicalState.getMessagesToBeTaken(createContinuousVariable(
                new BigDecimal(messageArrivalLowerBound + 1),
                new BigDecimal(messageArrivalUpperBound
                ))));
    }


    @Test
    @Tag("getMessagesToBeTaken with more than 1 message and 1 message can be taken in messageBag return list with 1 message")
    void TestGetMessagesToBeTakenWithMoreThanOneMessage() {
        int message1ArrivalLowerBound = 1;
        int message1ArrivalUpperBound = 5;

        int message2ArrivalLowerBound = message1ArrivalLowerBound + 2;
        int message2ArrivalUpperBound = message1ArrivalUpperBound;

        int globalTimeLowerBound = message1ArrivalLowerBound + 1;
        int globalTimeUpperBound = message1ArrivalUpperBound;

        Message message1 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message1ArrivalLowerBound), new BigDecimal(message1ArrivalUpperBound)));
        Message message2 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message2ArrivalLowerBound), new BigDecimal(message2ArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message1);
        this.physicalState.getMessageBag().add(message2);
        List<Message> expected = new ArrayList<>();
        expected.add(message1);
        assertEquals(expected, this.physicalState.getMessagesToBeTaken(createContinuousVariable(
                new BigDecimal(globalTimeLowerBound),
                new BigDecimal(globalTimeUpperBound)
        )));
    }

    @Test
    @Tag("getMessagesToBeTaken with more than 1 message return message list")
    void TestGetMessagesToBeTakenWithMoreThanOneMessageReturnMessageList() {
        int message1ArrivalLowerBound = 1;
        int message1ArrivalUpperBound = 5;

        int message2ArrivalLowerBound = message1ArrivalLowerBound;
        int message2ArrivalUpperBound = message1ArrivalUpperBound;

        int globalTimeLowerBound = message1ArrivalLowerBound + 1;
        int globalTimeUpperBound = message1ArrivalUpperBound;

        Message message1 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message1ArrivalLowerBound), new BigDecimal(message1ArrivalUpperBound)));
        Message message2 = new Message(
                "sender",
                "receiver",
                "content",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(message2ArrivalLowerBound), new BigDecimal(message2ArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message1);
        this.physicalState.getMessageBag().add(message2);
        List<Message> messageList = new ArrayList<>();
        messageList.add(message1);
        messageList.add(message2);
        Set<Message> expected = new HashSet<Message>(messageList);
        assertEquals(expected, new HashSet<>(this.physicalState.getMessagesToBeTaken(createContinuousVariable(
                new BigDecimal(globalTimeLowerBound),
                new BigDecimal(globalTimeUpperBound)
        ))));
    }

    @Test
    @Tag("getMessagesToBeTaken with setMode message return empty list")
    void TestGetMessagesToBeTakenWithSetModeMessageReturnEmptyList() {
        int messageArrivalLowerBound = 1;
        int messageArrivalUpperBound = 5;

        Message message = new Message(
                "sender",
                "receiver",
                "setMode",
                new HashMap<>(),
                createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound)));
        this.physicalState.getMessageBag().add(message);
        List<Message> expected = new ArrayList<>();
        assertEquals(expected, this.physicalState.getMessagesToBeTaken(createContinuousVariable(
                new BigDecimal(messageArrivalLowerBound + 1),
                new BigDecimal(messageArrivalUpperBound
                ))));
    }

    @Test
    @Tag("takeMessage with 0 message in messageBag return empty list")
    void TestTakeMessagesWithZeroMessage() {
        assertTrue(this.physicalState.takeMessage(createContinuousVariable(new BigDecimal(0), new BigDecimal(10))).isEmpty());
    }

    @Test
    @Tag("takeMessage with 1 message in messageBag return list with 1 message with t3 <= t1 && t4 <= t2")
    void TestTakeMessageWithOneMessageInsideGlobalTimeRange() {
        int messageArrivalLowerBound = 1;
        int messageArrivalUpperBound = 5;

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
        this.physicalState.addMessage(message);
        List<ActorState> expected = new ArrayList<>();
        try (MockedStatic<CompilerUtil> mockedCompiler = mockStatic(CompilerUtil.class)) {
            List<Statement> messageBody = new ArrayList<>();
            Statement mockedStatement = mock(Statement.class);
            messageBody.add(mockedStatement);
            mockedCompiler.when(() -> CompilerUtil.getMessageBody(anyString(), anyString())).thenReturn(messageBody);

            ActorState expectedPhysicalState = new PhysicalState(
                    "init",
                    "mode1",
                    messageParams,
                    new HashSet<>(),
                    messageBody,
                    0
            );
            expected.add(expectedPhysicalState);

            List<ActorState> actual = this.physicalState.takeMessage(globalTime);
            assertEquals(expected.size(), actual.size());
            assertEquals(expected.get(0).getActorName(), actual.get(0).getActorName());
            assertEquals(expected.get(0).getVariableValuation(), actual.get(0).getVariableValuation());
            assertTrue(actual.get(0).getMessageBag().isEmpty());
            assertEquals(expected.get(0).getSigma(), actual.get(0).getSigma());
            assertEquals(expected.get(0).getLocalTime(), actual.get(0).getLocalTime());
            assertInstanceOf(PhysicalState.class, actual.get(0));
        }
    }

    @Test
    @Tag("takeMessage with 1 message in messageBag return list with 1 message with t3 <= t1 && t4 > t2")
    void TestTakeMessageWithOneMessageOutOfGlobalTimeRange() {
        int messageArrivalLowerBound = 1; // t3
        int messageArrivalUpperBound = 5; // t4

        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(
                new BigDecimal(messageArrivalLowerBound), // t1
                new BigDecimal(messageArrivalUpperBound - 1) // t2
        );

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "content",
                messageParams,
                messageArrivalTime);
        this.physicalState.addMessage(message);
        List<ActorState> expected = new ArrayList<>();
        try (MockedStatic<CompilerUtil> mockedCompiler = mockStatic(CompilerUtil.class)) {
            List<Statement> messageBody = new ArrayList<>();
            Statement mockedStatement = mock(Statement.class);
            messageBody.add(mockedStatement);
            mockedCompiler.when(() -> CompilerUtil.getMessageBody(anyString(), anyString())).thenReturn(messageBody);

            ActorState expectedPhysicalState1 = new PhysicalState(
                    "init",
                    "mode1",
                    messageParams,
                    new HashSet<>(),
                    messageBody,
                    0
            );

            ActorState expectedPhysicalState2 = new PhysicalState(
                    "init",
                    "mode1",
                    new HashMap<>(),
                    new HashSet<>(),
                    new ArrayList<>(),
                    0
            );
            expected.add(expectedPhysicalState1);
            expected.add(expectedPhysicalState2);


            List<ActorState> actual = this.physicalState.takeMessage(globalTime);
            assertEquals(expected.size(), actual.size());
            assertEquals(expected.get(0).getActorName(), actual.get(0).getActorName());
            assertEquals(expected.get(0).getVariableValuation(), actual.get(0).getVariableValuation());
            assertEquals(expected.get(0).getSigma(), actual.get(0).getSigma());
            assertTrue(actual.get(0).getMessageBag().isEmpty());
            assertEquals(expected.get(0).getLocalTime(), actual.get(0).getLocalTime());
            assertInstanceOf(PhysicalState.class, actual.get(0));



            assertEquals(expected.get(1).getActorName(), actual.get(1).getActorName());
            assertEquals(expected.get(1).getVariableValuation(), actual.get(1).getVariableValuation());
            assertEquals(expected.get(1).getSigma(), actual.get(1).getSigma());
            assertEquals(expected.get(1).getLocalTime(), actual.get(1).getLocalTime());
            assertInstanceOf(PhysicalState.class, actual.get(1));

            // Check message bag of second state
            assertEquals(1, actual.get(1).getMessageBag().size());
            List<Message> remainMessages = new ArrayList<>(actual.get(1).getMessageBag());
            assertEquals(new ContinuousVariable("arrivalTime", globalTime.getUpperBound(), messageArrivalTime.getUpperBound()), remainMessages.get(0).getArrivalTime());

        }
    }

    @Test
    @Tag("takeMessage with 1 message in messageBag return list with 1 message with t3 <= t1 && t4 <= t2 but message is setMode")
    void TestTakeMessageWithOneMessageInsideGlobalTimeRangeButSetMode() {
        int messageArrivalLowerBound = 1;
        int messageArrivalUpperBound = 5;

        ContinuousVariable messageArrivalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(messageArrivalLowerBound), new BigDecimal(messageArrivalUpperBound));

        HashMap<String, Variable> messageParams = new HashMap<>();
        messageParams.put("key", new DiscreteDecimalVariable("value", new BigDecimal(1)));

        Message message = new Message(
                "sender",
                "receiver",
                "setMode",
                messageParams,
                messageArrivalTime);
        this.physicalState.addMessage(message);
        List<ActorState> expected = new ArrayList<>();
        try (MockedStatic<CompilerUtil> mockedCompiler = mockStatic(CompilerUtil.class)) {
            List<Statement> messageBody = new ArrayList<>();
            Statement mockedStatement = mock(Statement.class);
            messageBody.add(mockedStatement);
            mockedCompiler.when(() -> CompilerUtil.getMessageBody(anyString(), anyString())).thenReturn(messageBody);
            List<ActorState> actual = this.physicalState.takeMessage(globalTime);
            assertTrue(actual.isEmpty());
        }
    }
}