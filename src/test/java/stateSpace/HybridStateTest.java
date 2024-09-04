package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.timedrebeca.objectmodel.TimedRebecaParentSuffixPrimary;
import utils.CompilerUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HybridStateTest {
    private HybridState hybridState;
//    @Mock
    private CompilerUtil compilerUtilMock;
//    @Mock
    private RebecInstantiationMapping rebecInstantiationMappingMock;
//    @Mock
    private  TimedRebecaParentSuffixPrimary parentSuffixPrimaryMock;

    ContinuousVariable createContinuousVariable(Double lowerBound, Double upperBound) {
        return new ContinuousVariable("continuousVariable", lowerBound, upperBound);
    }

    TermPrimary createTermPrimary(String name) {
        TermPrimary termPrimary = new TermPrimary();
        termPrimary.setName(name);
        return termPrimary;
    }

    BinaryExpression creatBinaryExpression(String operator, Expression a, Expression b) {
        BinaryExpression binaryExpression = new BinaryExpression();
        binaryExpression.setOperator(operator);
        binaryExpression.setLeft(a);
        binaryExpression.setRight(b);
        return binaryExpression;
    }

    Literal createLiteral(Object a, String type) {
        Literal literalA = new Literal();
        literalA.setLiteralValue(String.valueOf(a));
        OrdinaryPrimitiveType typeA = new OrdinaryPrimitiveType();
        typeA.setName(type);
        literalA.setType(typeA);
        return literalA;
    }

    @BeforeEach
    void setUp() {
        compilerUtilMock = mock(CompilerUtil.class);
        rebecInstantiationMappingMock = mock(RebecInstantiationMapping.class);
        parentSuffixPrimaryMock = mock(TimedRebecaParentSuffixPrimary.class);
    }

    @Test
    @Tag("test takeMessage with non-deterministic behaviour in resumeTime")
    void testTakeMessageWithNonDeterministicResumeTime() {
        int messageArrivalLowerBound = 1;
        int messageArrivalUpperBound = 5;

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
        SoftwareState softwareState = new SoftwareState("init", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime", Double.valueOf(messageArrivalLowerBound), Double.valueOf(messageArrivalUpperBound+1)));
        softwareState.addMessage(message);
        List<ActorState> expected = new ArrayList<>();
        try (MockedStatic<CompilerUtil> mockedCompiler = mockStatic(CompilerUtil.class)) {
            List<Statement> messageBody = new ArrayList<>();
            Statement mockedStatement = mock(Statement.class);
            messageBody.add(mockedStatement);
            mockedCompiler.when(() -> CompilerUtil.getMessageBody(anyString(), anyString())).thenReturn(messageBody);

            ActorState expectedSoftwareState = new SoftwareState(
                    "init",
                    messageParams,
                    new HashSet<>(),
                    messageBody,
                    0,
                    new ContinuousVariable("resumeTime", globalTime.getLowerBound(), globalTime.getUpperBound())
            );
            expected.add(expectedSoftwareState);
            HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
            softwareStateHashMap.put(softwareState.getActorName(), softwareState);
            HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());
            List<HybridState> newHybridStates = hybridState.takeMessage(softwareState);

            assertEquals(2, newHybridStates.size());
            ActorState actual = newHybridStates.get(0).getActorState(softwareState.getActorName());
            assertEquals(expected.get(0).getActorName(), actual.getActorName());
            assertEquals(expected.get(0).getVariableValuation(), actual.getVariableValuation());
            assertTrue(actual.getMessageBag().isEmpty());
            assertEquals(expected.get(0).getLocalTime(), actual.getLocalTime());
            assertInstanceOf(SoftwareState.class, actual);
            assertEquals(((SoftwareState) expected.get(0)).getResumeTime(), ((SoftwareState) actual).getResumeTime());

            SoftwareState suspendedSoftwareState = (SoftwareState) newHybridStates.get(1).getActorState(softwareState.getActorName());
            assertEquals(1, suspendedSoftwareState.getMessageBag().size());
            assertTrue(suspendedSoftwareState.getMessageBag().contains(message));
        }
    }

    @Test
    @Tag("test sendStatement with one parameter")
    void testSendStatementWithOneParameter() {
        try (MockedStatic<RebecInstantiationMapping> rebecInstantiationMappingMockedStatic = mockStatic(RebecInstantiationMapping.class);
             MockedStatic<CompilerUtil> compilerUtilMockedStatic = mockStatic(CompilerUtil.class)) {
            rebecInstantiationMappingMockedStatic.when(RebecInstantiationMapping::getInstance).thenReturn(rebecInstantiationMappingMock);
            DotPrimary statement = new DotPrimary();
            TermPrimary right = new TermPrimary();
            right.setName("messageServerName");
            TermPrimary left = new TermPrimary();
            left.setName("knownRebecName");
            statement.setRight(right);
            statement.setLeft(left);
            List<Statement> sigma = new ArrayList<>();
            sigma.add(statement);

            PhysicalState physicalState1 = new PhysicalState("physicalState1", "none", new HashMap<>(), new HashSet<>(), sigma, 0);
            PhysicalState physicalState2 = new PhysicalState("physicalState2", "none", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
            HashMap<String, PhysicalState> physicalStateHashMap = new HashMap<>();
            physicalStateHashMap.put(physicalState1.getActorName(), physicalState1);
            physicalStateHashMap.put(physicalState2.getActorName(), physicalState2);
            int messageArrivalLowerBound = 1;
            int messageArrivalUpperBound = 2;
            int globalStartTime = 1;
            int globalEndTime = 2;
            ContinuousVariable continuousVariable = createContinuousVariable(Double.valueOf(globalStartTime), Double.valueOf(globalEndTime));
            hybridState = new HybridState(continuousVariable, new HashMap<>(), physicalStateHashMap, new CANNetworkState());
            when(rebecInstantiationMappingMock.getKnownRebecBinding(physicalState1.getActorName(), left.getName())).thenReturn(physicalState2.getActorName());

            List<FormalParameterDeclaration> formalParameterDeclarations = new ArrayList<>();
            formalParameterDeclarations.add(new FormalParameterDeclaration());
            formalParameterDeclarations.get(0).setName("formalParameterName1");
            when(rebecInstantiationMappingMock.getRebecReactiveClassType(physicalState2.getActorName())).thenReturn("ReactiveClassType");

            compilerUtilMockedStatic.when(() -> CompilerUtil.getServerParameters("ReactiveClassType", "messageServerName")).thenReturn(formalParameterDeclarations);
            Literal literal = new Literal();
            literal.setLiteralValue("2");
            OrdinaryPrimitiveType ordinaryPrimitiveType = new OrdinaryPrimitiveType();
            ordinaryPrimitiveType.setName("int");
            literal.setType(ordinaryPrimitiveType);
            ArrayList<Expression> parentSuffixPrimaryArguments = new ArrayList<>();
            parentSuffixPrimaryArguments.add(literal);
            when(parentSuffixPrimaryMock.getArguments()).thenReturn(parentSuffixPrimaryArguments);
            when(parentSuffixPrimaryMock.getStartAfterExpression()).thenReturn(createLiteral(messageArrivalLowerBound, "int"));
            when(parentSuffixPrimaryMock.getEndAfterExpression()).thenReturn(createLiteral(messageArrivalUpperBound, "int"));

            right.setParentSuffixPrimary(parentSuffixPrimaryMock);

            HybridState newHybridState = hybridState.sendStatement(physicalState1).get(0);
            assertEquals(0, physicalState2.getMessageBag().size());
            assertEquals(1, newHybridState.getActorState(physicalState2.getActorName()).getMessageBag().size());
            assertEquals(1, physicalState1.getSigma().size());
            assertEquals(0, newHybridState.getActorState(physicalState1.getActorName()).getSigma().size());

            assertEquals(globalStartTime + messageArrivalLowerBound,
                    Arrays.stream(newHybridState.getActorState(physicalState2.getActorName()).getMessageBag().toArray())
                            .map(message -> ((Message) message).getArrivalTime().getLowerBound().doubleValue())
                            .findFirst().get());

            assertEquals(globalEndTime + messageArrivalUpperBound,
                    Arrays.stream(newHybridState.getActorState(physicalState2.getActorName()).getMessageBag().toArray())
                            .map(message -> ((Message) message).getArrivalTime().getUpperBound().doubleValue())
                            .findFirst().get());
        }
    }

    @Test
    @Tag("test sendStatement with self sender")
    void testSendStatementWithSelfSender() {
        try (MockedStatic<RebecInstantiationMapping> rebecInstantiationMappingMockedStatic = mockStatic(RebecInstantiationMapping.class);
             MockedStatic<CompilerUtil> compilerUtilMockedStatic = mockStatic(CompilerUtil.class)) {
            rebecInstantiationMappingMockedStatic.when(RebecInstantiationMapping::getInstance).thenReturn(rebecInstantiationMappingMock);
            DotPrimary statement = new DotPrimary();
            TermPrimary right = new TermPrimary();
            right.setName("messageServerName");
            TermPrimary left = new TermPrimary();
            left.setName("self");
            statement.setRight(right);
            statement.setLeft(left);
            List<Statement> sigma = new ArrayList<>();
            sigma.add(statement);

            PhysicalState physicalState = new PhysicalState("physicalState", "none", new HashMap<>(), new HashSet<>(), sigma, 0);
            HashMap<String, PhysicalState> physicalStateHashMap = new HashMap<>();
            physicalStateHashMap.put(physicalState.getActorName(), physicalState);
            int messageArrivalLowerBound = 1;
            int messageArrivalUpperBound = 2;
            int globalStartTime = 1;
            int globalEndTime = 2;
            ContinuousVariable continuousVariable = createContinuousVariable(Double.valueOf(globalStartTime), Double.valueOf(globalEndTime));
            hybridState = new HybridState(continuousVariable, new HashMap<>(), physicalStateHashMap, new CANNetworkState());
            when(rebecInstantiationMappingMock.getKnownRebecBinding(physicalState.getActorName(), left.getName())).thenReturn(null);

            List<FormalParameterDeclaration> formalParameterDeclarations = new ArrayList<>();
            formalParameterDeclarations.add(new FormalParameterDeclaration());
            formalParameterDeclarations.get(0).setName("formalParameterName1");
            when(rebecInstantiationMappingMock.getRebecReactiveClassType(physicalState.getActorName())).thenReturn("ReactiveClassType");

            compilerUtilMockedStatic.when(() -> CompilerUtil.getServerParameters("ReactiveClassType", "messageServerName")).thenReturn(formalParameterDeclarations);
            Literal literal = new Literal();
            literal.setLiteralValue("2");
            OrdinaryPrimitiveType ordinaryPrimitiveType = new OrdinaryPrimitiveType();
            ordinaryPrimitiveType.setName("int");
            literal.setType(ordinaryPrimitiveType);
            ArrayList<Expression> parentSuffixPrimaryArguments = new ArrayList<>();
            parentSuffixPrimaryArguments.add(literal);
            when(parentSuffixPrimaryMock.getArguments()).thenReturn(parentSuffixPrimaryArguments);
            when(parentSuffixPrimaryMock.getStartAfterExpression()).thenReturn(createLiteral(messageArrivalLowerBound, "int"));
            when(parentSuffixPrimaryMock.getEndAfterExpression()).thenReturn(createLiteral(messageArrivalUpperBound, "int"));

            right.setParentSuffixPrimary(parentSuffixPrimaryMock);

            HybridState newHybridState = hybridState.sendStatement(physicalState).get(0);
            assertEquals(1, physicalState.getSigma().size());
            assertEquals(0, newHybridState.getActorState(physicalState.getActorName()).getSigma().size());

            assertEquals(globalStartTime + messageArrivalLowerBound,
                    Arrays.stream(newHybridState.getActorState(physicalState.getActorName()).getMessageBag().toArray())
                            .map(message -> ((Message) message).getArrivalTime().getLowerBound().doubleValue())
                            .findFirst().get());

            assertEquals(globalEndTime + messageArrivalUpperBound,
                    Arrays.stream(newHybridState.getActorState(physicalState.getActorName()).getMessageBag().toArray())
                            .map(message -> ((Message) message).getArrivalTime().getUpperBound().doubleValue())
                            .findFirst().get());
        }
    }

    @Test
    @Tag("test sendStatement with non-deterministic behaviour in resume time")
    void testSendStatementWithNonDeterministicResumeTime() {
        try (MockedStatic<RebecInstantiationMapping> rebecInstantiationMappingMockedStatic = mockStatic(RebecInstantiationMapping.class);
             MockedStatic<CompilerUtil> compilerUtilMockedStatic = mockStatic(CompilerUtil.class)) {
            rebecInstantiationMappingMockedStatic.when(RebecInstantiationMapping::getInstance).thenReturn(rebecInstantiationMappingMock);
            DotPrimary statement = new DotPrimary();
            TermPrimary right = new TermPrimary();
            right.setName("messageServerName");
            TermPrimary left = new TermPrimary();
            left.setName("knownRebecName");
            statement.setRight(right);
            statement.setLeft(left);
            List<Statement> sigma = new ArrayList<>();
            sigma.add(statement);

            int messageArrivalLowerBound = 1;
            int messageArrivalUpperBound = 2;
            int globalStartTime = 1;
            int globalEndTime = 2;

            SoftwareState softwareState1 = new SoftwareState("softwareState1", new HashMap<>(), new HashSet<>(), sigma, 0, createContinuousVariable(Double.valueOf(globalStartTime), Double.valueOf(globalEndTime + 1)));
            SoftwareState softwareState2 = new SoftwareState("softwareState2", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime"));
            HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
            softwareStateHashMap.put(softwareState1.getActorName(), softwareState1);
            softwareStateHashMap.put(softwareState2.getActorName(), softwareState2);

            ContinuousVariable continuousVariable = createContinuousVariable(Double.valueOf(globalStartTime), Double.valueOf(globalEndTime));
            hybridState = new HybridState(continuousVariable, softwareStateHashMap, new HashMap<>(), new CANNetworkState());
            when(rebecInstantiationMappingMock.getKnownRebecBinding(softwareState1.getActorName(), left.getName())).thenReturn(softwareState2.getActorName());

            List<FormalParameterDeclaration> formalParameterDeclarations = new ArrayList<>();
            formalParameterDeclarations.add(new FormalParameterDeclaration());
            formalParameterDeclarations.get(0).setName("formalParameterName1");
            when(rebecInstantiationMappingMock.getRebecReactiveClassType(softwareState2.getActorName())).thenReturn("ReactiveClassType");

            compilerUtilMockedStatic.when(() -> CompilerUtil.getServerParameters("ReactiveClassType", "messageServerName")).thenReturn(formalParameterDeclarations);
            Literal literal = new Literal();
            literal.setLiteralValue("2");
            OrdinaryPrimitiveType ordinaryPrimitiveType = new OrdinaryPrimitiveType();
            ordinaryPrimitiveType.setName("int");
            literal.setType(ordinaryPrimitiveType);
            ArrayList<Expression> parentSuffixPrimaryArguments = new ArrayList<>();
            parentSuffixPrimaryArguments.add(literal);
            when(parentSuffixPrimaryMock.getArguments()).thenReturn(parentSuffixPrimaryArguments);
            when(parentSuffixPrimaryMock.getStartAfterExpression()).thenReturn(createLiteral(messageArrivalLowerBound, "int"));
            when(parentSuffixPrimaryMock.getEndAfterExpression()).thenReturn(createLiteral(messageArrivalUpperBound, "int"));

            right.setParentSuffixPrimary(parentSuffixPrimaryMock);

            List<HybridState> newHybridStates = hybridState.sendStatement(softwareState1);
            HybridState newHybridState = newHybridStates.get(0);
            assertEquals(0, softwareState2.getMessageBag().size());
            assertEquals(1, newHybridState.getActorState(softwareState2.getActorName()).getMessageBag().size());
            assertEquals(1, softwareState1.getSigma().size());
            assertEquals(0, newHybridState.getActorState(softwareState1.getActorName()).getSigma().size());

            assertEquals(globalStartTime + messageArrivalLowerBound,
                    Arrays.stream(newHybridState.getActorState(softwareState2.getActorName()).getMessageBag().toArray())
                            .map(message -> ((Message) message).getArrivalTime().getLowerBound().doubleValue())
                            .findFirst().get());

            assertEquals(globalEndTime + messageArrivalUpperBound,
                    Arrays.stream(newHybridState.getActorState(softwareState2.getActorName()).getMessageBag().toArray())
                            .map(message -> ((Message) message).getArrivalTime().getUpperBound().doubleValue())
                            .findFirst().get());

            assertEquals(2, newHybridStates.size());
            assertEquals(1, newHybridStates.get(1).getActorState(softwareState1.getActorName()).getSigma().size());
            assertEquals(0, newHybridStates.get(1).getActorState(softwareState2.getActorName()).getMessageBag().size());
            assertEquals(Double.valueOf(globalEndTime), ((SoftwareState) newHybridStates.get(1).getActorState(softwareState1.getActorName())).getResumeTime().getLowerBound());
            assertEquals(Double.valueOf(globalEndTime + 1), ((SoftwareState) newHybridStates.get(1).getActorState(softwareState1.getActorName())).getResumeTime().getUpperBound());
        }
    }

    @Test
    @Tag("test assignStatement")
    void testAssignStatement() {
        BinaryExpression rightExp = creatBinaryExpression("*", createLiteral(2, "int"), createLiteral(3, "int"));

        String variableName = "variableName";
        BinaryExpression assignStatement = creatBinaryExpression("=", createTermPrimary(variableName), rightExp);
        List<Statement> sigma = new ArrayList<>();
        sigma.add(assignStatement);

        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime"));
        softwareState.addStatements(sigma);
        softwareState.addVariable(new DiscreteDecimalVariable(variableName, new BigDecimal(0)));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        hybridState = new HybridState(new ContinuousVariable("globalTime"), softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        HybridState newHybridState = hybridState.assignStatement(softwareState).get(0);
        assertEquals(new BigDecimal(0), ((DiscreteDecimalVariable) softwareState.getVariableValuation().get(variableName)).getValue());
        assertTrue(newHybridState.getActorState(softwareState.getActorName()).getVariableValuation().get(variableName) instanceof DiscreteDecimalVariable);
        assertEquals(new BigDecimal(6), ((DiscreteDecimalVariable) newHybridState.getActorState(softwareState.getActorName()).getVariableValuation().get(variableName)).getValue());
    }

    @Test
    @Tag("test assignStatement with non-deterministic resume time")
    void testAssignStatementWithNonDeterministicResumeTime() {
        BinaryExpression rightExp = creatBinaryExpression("*", createLiteral(2, "int"), createLiteral(3, "int"));

        String variableName = "variableName";
        BinaryExpression assignStatement = creatBinaryExpression("=", createTermPrimary(variableName), rightExp);
        List<Statement> sigma = new ArrayList<>();
        sigma.add(assignStatement);

        int globalLowerBound = 1;
        int globalUpperBound = 2;

        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime", Double.valueOf(globalLowerBound), Double.valueOf(globalUpperBound+1)));
        softwareState.addStatements(sigma);
        softwareState.addVariable(new DiscreteDecimalVariable(variableName, new BigDecimal(0)));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        hybridState = new HybridState(createContinuousVariable(Double.valueOf(globalLowerBound), Double.valueOf(globalUpperBound)), softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        List<HybridState> newHybridStates = hybridState.assignStatement(softwareState);
        HybridState newHybridState = newHybridStates.get(0);
        assertEquals(new BigDecimal(0), ((DiscreteDecimalVariable) softwareState.getVariableValuation().get(variableName)).getValue());
        assertTrue(newHybridState.getActorState(softwareState.getActorName()).getVariableValuation().get(variableName) instanceof DiscreteDecimalVariable);
        assertEquals(new BigDecimal(6), ((DiscreteDecimalVariable) newHybridState.getActorState(softwareState.getActorName()).getVariableValuation().get(variableName)).getValue());

        assertEquals(2, newHybridStates.size());
        assertEquals(1, newHybridStates.get(1).getActorState(softwareState.getActorName()).getSigma().size());
        assertTrue(newHybridStates.get(1).getActorState(softwareState.getActorName()).getVariableValuation().get(variableName) instanceof DiscreteDecimalVariable);
        assertEquals(new BigDecimal(0), ((DiscreteDecimalVariable) newHybridStates.get(1).getActorState(softwareState.getActorName()).getVariableValuation().get(variableName)).getValue());
    }

    @Test
    @Tag("test delay statement")
    void testDelayStatement() {
        TermPrimary termPrimary = new TermPrimary();
        termPrimary.setName("delay");
        List<Statement> sigma = new ArrayList<>();
        // set arguments
        parentSuffixPrimaryMock = mock(TimedRebecaParentSuffixPrimary.class);
        ArrayList<Expression> arguments = new ArrayList<>();
        arguments.add(createLiteral(2, "int"));
        arguments.add(createLiteral(3, "int"));
        when(parentSuffixPrimaryMock.getArguments()).thenReturn(arguments);
        termPrimary.setParentSuffixPrimary(parentSuffixPrimaryMock);
        sigma.add(termPrimary);

        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(1), Double.valueOf(2));
        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        HybridState newHybridState = hybridState.delayStatement(softwareState).get(0);
        Double softwareStateLowerBound = ((SoftwareState) newHybridState.getActorState(softwareState.getActorName())).getResumeTime().getLowerBound();
        Double softwareStateUpperBound = ((SoftwareState) newHybridState.getActorState(softwareState.getActorName())).getResumeTime().getUpperBound();
        assertEquals(softwareStateLowerBound, Double.valueOf(3));
        assertEquals(softwareStateUpperBound, Double.valueOf(5));
    }

    @Test
    @Tag("test delayStatement with non-deterministic resume time")
    void testDelayStatementWithNonDeterministicResumeTime() {
        TermPrimary termPrimary = new TermPrimary();
        termPrimary.setName("delay");
        List<Statement> sigma = new ArrayList<>();
        // set arguments
        parentSuffixPrimaryMock = mock(TimedRebecaParentSuffixPrimary.class);
        ArrayList<Expression> arguments = new ArrayList<>();
        arguments.add(createLiteral(2, "int"));
        arguments.add(createLiteral(3, "int"));
        when(parentSuffixPrimaryMock.getArguments()).thenReturn(arguments);
        termPrimary.setParentSuffixPrimary(parentSuffixPrimaryMock);
        sigma.add(termPrimary);

        int globalLowerBound = 1;
        int globalUpperBound = 2;

        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime", Double.valueOf(globalLowerBound), Double.valueOf(globalUpperBound+1)));
        softwareState.addStatements(sigma);
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(createContinuousVariable(Double.valueOf(globalLowerBound), Double.valueOf(globalUpperBound)), softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        List<HybridState> newHybridStates = hybridState.delayStatement(softwareState);
        HybridState newHybridState = newHybridStates.get(0);
        Double softwareStateLowerBound = ((SoftwareState) newHybridState.getActorState(softwareState.getActorName())).getResumeTime().getLowerBound();
        Double softwareStateUpperBound = ((SoftwareState) newHybridState.getActorState(softwareState.getActorName())).getResumeTime().getUpperBound();
        assertEquals(softwareStateLowerBound, Double.valueOf(3));
        assertEquals(softwareStateUpperBound, Double.valueOf(5));

        assertEquals(2, newHybridStates.size());
        assertEquals(1, newHybridStates.get(1).getActorState(softwareState.getActorName()).getSigma().size());
    }

    @Test
    @Tag("test if statement condition (true) is definite without nondeterminism in resumeTime")
    void testIfStatementConditionTrueDefinite() {
        BinaryExpression conditionExpr = creatBinaryExpression(">", createLiteral(3, "int"), createLiteral(2, "int"));
        ConditionalStatement conditionalStatement = new ConditionalStatement();
        Statement ifStatement = new Statement();
        Statement elseStatement = new Statement();
        conditionalStatement.setCondition(conditionExpr);
        conditionalStatement.setStatement(ifStatement);
        conditionalStatement.setElseStatement(elseStatement);
        List<Statement> sigma = new ArrayList<>();
        sigma.add(conditionalStatement);

        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(0), Double.valueOf(2));
        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        List<HybridState> newHybridStates = hybridState.ifStatement(softwareState);
        assertEquals(1, newHybridStates.size());
        assertEquals(1, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().size());
        assertEquals(ifStatement, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().get(0));
    }

    @Test
    @Tag("test if statement condition (false) is definite without nondeterminism in resumeTime")
    void testIfStatementConditionFalseDefinite() {
        BinaryExpression conditionExpr = creatBinaryExpression(">", createLiteral(1, "int"), createLiteral(2, "int"));
        ConditionalStatement conditionalStatement = new ConditionalStatement();
        Statement ifStatement = new Statement();
        Statement elseStatement = new Statement();
        conditionalStatement.setCondition(conditionExpr);
        conditionalStatement.setStatement(ifStatement);
        conditionalStatement.setElseStatement(elseStatement);
        List<Statement> sigma = new ArrayList<>();
        sigma.add(conditionalStatement);

        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(0), Double.valueOf(2));
        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        List<HybridState> newHybridStates = hybridState.ifStatement(softwareState);
        assertEquals(1, newHybridStates.size());
        assertEquals(1, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().size());
        assertEquals(elseStatement, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().get(0));
    }

    @Test
    @Tag("test if statement condition is indefinite without nondeterminism in resumeTime")
    void testIfStatementConditionInDefinite() {
        TermPrimary interval1TermPrimary = createTermPrimary("interval1");
        TermPrimary interval2TermPrimary = createTermPrimary("interval2");
        IntervalRealVariable interval1RealVariable = new IntervalRealVariable(interval1TermPrimary.getName(), 2.0, 4.0);
        IntervalRealVariable interval2RealVariable = new IntervalRealVariable(interval2TermPrimary.getName(), 1.0, 3.0);
        HashMap<String, Variable> variableValuation = new HashMap<>();
        variableValuation.put(interval1RealVariable.getName(), interval1RealVariable);
        variableValuation.put(interval2RealVariable.getName(), interval2RealVariable);

        BinaryExpression conditionExpr = creatBinaryExpression(">", interval1TermPrimary, interval2TermPrimary);
        ConditionalStatement conditionalStatement = new ConditionalStatement();
        Statement ifStatement = new Statement();
        Statement elseStatement = new Statement();
        conditionalStatement.setCondition(conditionExpr);
        conditionalStatement.setStatement(ifStatement);
        conditionalStatement.setElseStatement(elseStatement);
        List<Statement> sigma = new ArrayList<>();
        sigma.add(conditionalStatement);

        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(0), Double.valueOf(2));
        SoftwareState softwareState = new SoftwareState("softwareState", variableValuation, new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        List<HybridState> newHybridStates = hybridState.ifStatement(softwareState);
        assertEquals(2, newHybridStates.size());
        assertEquals(1, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().size());
        assertEquals(ifStatement, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().get(0));

        assertEquals(1, newHybridStates.get(1).getActorState(softwareState.getActorName()).getSigma().size());
        assertEquals(elseStatement, newHybridStates.get(1).getActorState(softwareState.getActorName()).getSigma().get(0));
    }

    @Test
    @Tag("test ifStatement with non-deterministic resume time")
    void testIfStatementWithNonDeterministicResumeTime() {
        BinaryExpression conditionExpr = creatBinaryExpression(">", createLiteral(3, "int"), createLiteral(2, "int"));
        ConditionalStatement conditionalStatement = new ConditionalStatement();
        Statement ifStatement = new Statement();
        Statement elseStatement = new Statement();
        conditionalStatement.setCondition(conditionExpr);
        conditionalStatement.setStatement(ifStatement);
        conditionalStatement.setElseStatement(elseStatement);
        List<Statement> sigma = new ArrayList<>();
        sigma.add(conditionalStatement);

        int globalLowerBound = 1;
        int globalUpperBound = 2;

        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime", Double.valueOf(globalLowerBound), Double.valueOf(globalUpperBound+1)));
        softwareState.addStatements(sigma);
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(createContinuousVariable(Double.valueOf(globalLowerBound), Double.valueOf(globalUpperBound)), softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        List<HybridState> newHybridStates = hybridState.ifStatement(softwareState);
        HybridState newHybridState = newHybridStates.get(0);
        assertEquals(1, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().size());
        assertEquals(ifStatement, newHybridStates.get(0).getActorState(softwareState.getActorName()).getSigma().get(0));

        assertEquals(2, newHybridStates.size());
        assertEquals(1, newHybridStates.get(1).getActorState(softwareState.getActorName()).getSigma().size());
        assertTrue(newHybridStates.get(1).getActorState(softwareState.getActorName()).getSigma().get(0) instanceof ConditionalStatement);
    }

    @Test
    @Tag("test setMode statement")
    void testSetModeStatement() {
        TermPrimary termPrimary = new TermPrimary();
        termPrimary.setName("setMode");
        List<Statement> sigma = new ArrayList<>();
        // set arguments
        parentSuffixPrimaryMock = mock(TimedRebecaParentSuffixPrimary.class);
        ArrayList<Expression> arguments = new ArrayList<>();
        arguments.add(createTermPrimary("mode"));
        when(parentSuffixPrimaryMock.getArguments()).thenReturn(arguments);
        termPrimary.setParentSuffixPrimary(parentSuffixPrimaryMock);
        sigma.add(termPrimary);

        ContinuousVariable globalTime = createContinuousVariable(Double.valueOf(0), Double.valueOf(2));
        PhysicalState physicalState = new PhysicalState("physicalState", "none", new HashMap<>(), new HashSet<>(), sigma, 0);
        HashMap<String, PhysicalState> physicalStateHashMap = new HashMap<>();
        physicalStateHashMap.put(physicalState.getActorName(), physicalState);
        HybridState hybridState = new HybridState(globalTime, new HashMap<>(), physicalStateHashMap, new CANNetworkState());

        HybridState newHybridState = hybridState.setModeStatement(physicalState).get(0);
        assertEquals("mode", ((PhysicalState) newHybridState.getActorState(physicalState.getActorName())).getMode());
    }
    
    @Tag("verify getGlobalStateModes output")
    void testGetGlobalStateModes() {
        // Prepare the test data
        PhysicalState physicalState1 = new PhysicalState("actor1", "On", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        PhysicalState physicalState2 = new PhysicalState("actor2", "Off", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        PhysicalState physicalState3 = new PhysicalState("actor3", "none", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);

        HashMap<String, PhysicalState> physicalStates = new HashMap<>();
        physicalStates.put(physicalState1.getActorName(), physicalState1);
        physicalStates.put(physicalState2.getActorName(), physicalState2);
        physicalStates.put(physicalState3.getActorName(), physicalState3);

        List<Statement> sigma = new ArrayList<>();
        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(0), new BigDecimal(2));
        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());
        hybridState.setPhysicalStates(physicalStates);

        // Invoke the method under test
        List<Set<String>> result = hybridState.getGlobalStateModes();

        // Define the expected output
        List<Set<String>> expected = new ArrayList<>();
        Set<String> expectedSet1 = new HashSet<>(Arrays.asList("actor1", "On"));
        Set<String> expectedSet2 = new HashSet<>(Arrays.asList("actor2", "Off"));
        expected.add(expectedSet1);
        expected.add(expectedSet2);

        // Verify the result
        assertEquals(expected.size(), result.size(), "The number of global state modes is incorrect.");
        assertTrue(result.containsAll(expected), "The global state modes do not match the expected values.");
    }

    @Test
    public void testGetIntervalsMultipleOdesDifferentStates() {
        String[] odes = {"temperature > 100", "pressure < 2"};
        HashMap<String, PhysicalState> physicalStates = new HashMap<>();

        // Physical state for temperature
        PhysicalState tempState = new PhysicalState("sensor1", "active", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        IntervalRealVariable temperature = new IntervalRealVariable("temperature", 80.0, 120.0);
        tempState.getVariablesValuation().put("temperature", temperature);
        physicalStates.put("sensor1", tempState);

        // Physical state for pressure
        PhysicalState pressureState = new PhysicalState("sensor2", "active", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        IntervalRealVariable pressure = new IntervalRealVariable("pressure", 0.0, 5.0);
        pressureState.getVariablesValuation().put("pressure", pressure);
        physicalStates.put("sensor2", pressureState);

        double[] actualIntervals = new HybridState(null, null, physicalStates, null).getIntervals(odes);
        assertEquals(4, actualIntervals.length);

        // Order doesn't matter, so verify both temperature and pressure intervals are present
        assertTrue(actualIntervals[0] == 80.0 || actualIntervals[0] == 0.0);
        assertTrue(actualIntervals[1] == 120.0 || actualIntervals[1] == 0.0);
        assertTrue(actualIntervals[2] == 80.0 || actualIntervals[2] == 5.0);
        assertTrue(actualIntervals[3] == 120.0 || actualIntervals[3] == 5.0);
    }

    @Test
    void testGetIntervalsBasicValidCase() {
        HashMap<String, PhysicalState> physicalStates = new HashMap<>();

        // Initialize physical states and their variables
        PhysicalState physicalState1 = new PhysicalState("Physics1", "none", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
        PhysicalState physicalState2 = new PhysicalState("Physics2", "none", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);

        IntervalRealVariable intervalVariable1 = new IntervalRealVariable("Var1", 1.0, 5.0);
        IntervalRealVariable intervalVariable2 = new IntervalRealVariable("Var2", 2.0, 6.0);

        HashMap<String, Variable> variables1 = new HashMap<>();
        variables1.put("Var1", intervalVariable1);
        physicalState1.setVariablesValuation(variables1);

        HashMap<String, Variable> variables2 = new HashMap<>();
        variables2.put("Var2", intervalVariable2);
        physicalState2.setVariablesValuation(variables2);

        physicalStates.put("Physics1", physicalState1);
        physicalStates.put("Physics2", physicalState2);

        hybridState = new HybridState(new ContinuousVariable("globalTime"), new HashMap<>(), physicalStates, new CANNetworkState());
        String[] ODEs = {"Physics1_Var1=1", "Physics2_Var2=1"};
        double[] intervals = hybridState.getIntervals(ODEs);

        assertArrayEquals(new double[]{1.0, 5.0, 2.0, 6.0}, intervals, "Intervals do not match the expected values.");
    }
//
//    @Nested
//    class GetEventsTests {
//        private HybridState hybridStateForEvents;
//        private HashMap<String, SoftwareState> softwareStatesMock;
//
//        @BeforeEach
//        void setUp() {
//            // Set up the HybridState object
//            hybridStateForEvents = new HybridState();
//
//            // Mock the softwareStates map
//            softwareStatesMock = mock(HashMap.class);
//            hybridStateForEvents.setSoftwareStates(softwareStatesMock);
//        }
//
//        @Test
//        void testGetEvents_withEmptySoftwareStates() {
//            // Given an empty softwareStates map
//            when(softwareStatesMock.values()).thenReturn(new ArrayList<>());
//
//            // When calling getEvents with currentEvent = 0.0 and timeInterval = 1.0
//            double[] result = hybridStateForEvents.getEvents(0.0, 1.0);
//
//            // Then the result should only contain the currentEvent + timeInterval
//            assertArrayEquals(new double[]{1.0}, result);
//        }
//
//        @Test
//        void testGetEvents_withSoftwareStateResumeTimes() {
//            // Given a softwareState with specific resume times and message arrival times
//            SoftwareState softwareState = mock(SoftwareState.class);
//
//            // Mock the resume time
//            ContinuousVariable resumeTime = mock(ContinuousVariable.class);
//            when(resumeTime.getLowerBound()).thenReturn(new BigDecimal("2.0"));
//            when(resumeTime.getUpperBound()).thenReturn(new BigDecimal("4.0"));
//            when(softwareState.getResumeTime()).thenReturn(resumeTime);
//
//            // Mock a message with arrival time
//            Message message1 = mock(Message.class);
//            ContinuousVariable arrivalTime1 = mock(ContinuousVariable.class);
//            when(arrivalTime1.getLowerBound()).thenReturn(new BigDecimal("3.0"));
//            when(arrivalTime1.getUpperBound()).thenReturn(new BigDecimal("5.0"));
//            when(message1.getArrivalTime()).thenReturn(arrivalTime1);
//
//            // Mock the message bag to return the mocked message as a Set
//            when(softwareState.getMessageBag()).thenReturn(new HashSet<>(Arrays.asList(message1)));
//
//            // Mock the software states map
//            HashMap<String, SoftwareState> softwareStatesMock = new HashMap<>();
//            softwareStatesMock.put("actor1", softwareState);
//            when(hybridStateForEvents.getSoftwareStates()).thenReturn(softwareStatesMock);
//
//            // When calling getEvents with currentEvent = 0.0 and timeInterval = 1.0
//            double[] result = hybridStateForEvents.getEvents(0.0, 1.0);
//
//            // Then the result should contain the resume times and arrival times, plus the currentEvent + timeInterval
//            double[] expected = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
//            assertArrayEquals(expected, result);
//        }
//
//
//        @Test
//        void testGetEvents_withMultipleSoftwareStates() {
//            // Given multiple softwareStates with different resume times and message arrival times
//            SoftwareState state1 = mock(SoftwareState.class);
//            SoftwareState state2 = mock(SoftwareState.class);
//
//            ContinuousVariable resumeTime1 = mock(ContinuousVariable.class);
//            when(resumeTime1.getLowerBound()).thenReturn(new BigDecimal(2.0));
//            when(resumeTime1.getUpperBound()).thenReturn(new BigDecimal(3.0));
//
//            ContinuousVariable resumeTime2 = mock(ContinuousVariable.class);
//            when(resumeTime2.getLowerBound()).thenReturn(new BigDecimal(4.0));
//            when(resumeTime2.getUpperBound()).thenReturn(new BigDecimal(6.0));
//
//            when(state1.getResumeTime()).thenReturn(resumeTime1);
//            when(state2.getResumeTime()).thenReturn(resumeTime2);
//
//            // Change from ArrayList to HashSet
//            when(state1.getMessageBag()).thenReturn(new HashSet<>());
//            when(state2.getMessageBag()).thenReturn(new HashSet<>());
//
//            when(softwareStatesMock.values()).thenReturn(Arrays.asList(state1, state2));
//
//            // When calling getEvents with currentEvent = 1.0 and timeInterval = 2.0
//            double[] result = hybridStateForEvents.getEvents(1.0, 2.0);
//
//            // Then the result should contain all resume times, plus the currentEvent + timeInterval
//            double[] expected = new double[]{3.0, 2.0, 3.0, 4.0, 6.0};
//            assertArrayEquals(expected, result);
//        }
//
//        @Test
//        void testGetEvents_withSortedOutput() {
//            // Given a state where events are out of order initially
//            SoftwareState state1 = mock(SoftwareState.class);
//
//            ContinuousVariable resumeTime1 = mock(ContinuousVariable.class);
//            when(resumeTime1.getLowerBound()).thenReturn(new BigDecimal(5.0));
//            when(resumeTime1.getUpperBound()).thenReturn(new BigDecimal(2.0));  // Deliberate reverse order
//
//            when(state1.getResumeTime()).thenReturn(resumeTime1);
//
//            Message message1 = mock(Message.class);
//            ContinuousVariable arrivalTime1 = mock(ContinuousVariable.class);
//            when(arrivalTime1.getLowerBound()).thenReturn(new BigDecimal(7.0));
//            when(arrivalTime1.getUpperBound()).thenReturn(new BigDecimal(3.0));  // Deliberate reverse order
//            when(message1.getArrivalTime()).thenReturn(arrivalTime1);
//
//            // Change from List to HashSet
//            when(state1.getMessageBag()).thenReturn(new HashSet<>(Arrays.asList(message1)));
//
//            when(softwareStatesMock.values()).thenReturn(Arrays.asList(state1));
//
//            // When calling getEvents with currentEvent = 1.0 and timeInterval = 2.0
//            double[] result = hybridStateForEvents.getEvents(1.0, 2.0);
//
//            // Then the result should be sorted in ascending order
//            double[] expected = new double[]{3.0, 2.0, 3.0, 5.0, 7.0};
//            Arrays.sort(expected);  // Sorting the expected result
//            assertArrayEquals(expected, result);
//        }

    }
}