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

    ContinuousVariable createContinuousVariable(BigDecimal lowerBound, BigDecimal upperBound) {
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
            ContinuousVariable continuousVariable = createContinuousVariable(
                    new BigDecimal(globalStartTime),
                    new BigDecimal(globalEndTime));
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

        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(1), new BigDecimal(2));
        SoftwareState softwareState = new SoftwareState("softwareState", new HashMap<>(), new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);
        HybridState hybridState = new HybridState(globalTime, softwareStateHashMap, new HashMap<>(), new CANNetworkState());

        HybridState newHybridState = hybridState.delayStatement(softwareState).get(0);
        BigDecimal softwareStateLowerBound = ((SoftwareState) newHybridState.getActorState(softwareState.getActorName())).getResumeTime().getLowerBound();
        BigDecimal softwareStateUpperBound = ((SoftwareState) newHybridState.getActorState(softwareState.getActorName())).getResumeTime().getUpperBound();
        assertEquals(softwareStateLowerBound, new BigDecimal(3));
        assertEquals(softwareStateUpperBound, new BigDecimal(5));

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

        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(0), new BigDecimal(2));
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

        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(0), new BigDecimal(2));
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

        ContinuousVariable globalTime = createContinuousVariable(new BigDecimal(0), new BigDecimal(2));
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
}