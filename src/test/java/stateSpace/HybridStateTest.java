package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import dataStructure.Variable;
import org.junit.jupiter.api.BeforeEach;
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
}