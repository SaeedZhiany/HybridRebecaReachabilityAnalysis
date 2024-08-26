package utils;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import stateSpace.CANNetworkState;
import stateSpace.HybridState;
import stateSpace.PhysicalState;
import stateSpace.SoftwareState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReachabilityAnalysisGraphTest {

    @Test
    @Tag("integration")  // Or another tag relevant to your test
    public void testReachabilityAnalysisGraph() {
        HybridState rootState = createSampleHybridState(
                "RootState",
                new BigDecimal(0),
                new BigDecimal(10),
                new BigDecimal(5),
                2,
                3,
                "*"
        );
        HybridState childState1 = createSampleHybridState(
                "ChildState1",
                new BigDecimal(1),
                new BigDecimal(11),
                new BigDecimal(6),
                4,
                5,
                "+"
        );
        HybridState childState2 = createSampleHybridState(
                "ChildState2",
                new BigDecimal(2),
                new BigDecimal(12),
                new BigDecimal(7),
                6,
                2,
                "-"
        );
        HybridState grandChildState = createSampleHybridState(
                "GrandChildState",
                new BigDecimal(3),
                new BigDecimal(13),
                new BigDecimal(8),
                3,
                3,
                "*"
        );

        ReachabilityAnalysisGraph reachabilityAnalysisGraph = new ReachabilityAnalysisGraph(rootState);

        ReachabilityAnalysisGraph.TreeNode rootNode = reachabilityAnalysisGraph.getRoot();
        reachabilityAnalysisGraph.addNode(rootNode, "child1", childState1);
        reachabilityAnalysisGraph.addNode(rootNode, "child2", childState2);
        ReachabilityAnalysisGraph.TreeNode childNode1 = rootNode.getChildren().get(0);
        reachabilityAnalysisGraph.addNode(childNode1, "grandchild1", grandChildState);

        String jsonOutput = reachabilityAnalysisGraph.toJson();
        assertNotNull(jsonOutput);
    }

    private static HybridState createSampleHybridState(
            String stateName,
            BigDecimal globalTimeLowerBound,
            BigDecimal globalTimeUpperBound,
            BigDecimal variableInitialValue,
            int literalA,
            int literalB,
            String operator
    ) {
        // Create a ContinuousVariable representing global time with the given bounds
        ContinuousVariable globalTime = new ContinuousVariable("globalTime", globalTimeLowerBound, globalTimeUpperBound);

        // Create a SoftwareState with the given variable initial value
        HashMap<String, Variable> variableValuation = new HashMap<>();
        variableValuation.put("variableName", new DiscreteDecimalVariable("variableName", variableInitialValue));

        // Create a list of statements for the sigma (executing statements) list using the given literals and operator
        List<Statement> sigma = new ArrayList<>();
        BinaryExpression rightExp = createBinaryExpression(operator, createLiteral(literalA, "int"), createLiteral(literalB, "int"));
        BinaryExpression assignStatement = createBinaryExpression("=", createTermPrimary("variableName"), rightExp);
        sigma.add(assignStatement);

        // Create SoftwareState and add it to the map
        SoftwareState softwareState = new SoftwareState(stateName, variableValuation, new HashSet<>(), sigma, 0, new ContinuousVariable("resumeTime"));
        HashMap<String, SoftwareState> softwareStateHashMap = new HashMap<>();
        softwareStateHashMap.put(softwareState.getActorName(), softwareState);

        // Create an empty PhysicalState map (not used in this example)
        HashMap<String, PhysicalState> physicalStateHashMap = new HashMap<>();

        // Create an empty CANNetworkState
        CANNetworkState canNetworkState = new CANNetworkState();

        // Create and return the HybridState
        return new HybridState(globalTime, softwareStateHashMap, physicalStateHashMap, canNetworkState);
    }

    static TermPrimary createTermPrimary(String name) {
        TermPrimary termPrimary = new TermPrimary();
        termPrimary.setName(name);
        return termPrimary;
    }

    static BinaryExpression createBinaryExpression(String operator, Expression a, Expression b) {
        BinaryExpression binaryExpression = new BinaryExpression();
        binaryExpression.setOperator(operator);
        binaryExpression.setLeft(a);
        binaryExpression.setRight(b);
        return binaryExpression;
    }

    static Literal createLiteral(Object a, String type) {
        Literal literalA = new Literal();
        literalA.setLiteralValue(String.valueOf(a));
        OrdinaryPrimitiveType typeA = new OrdinaryPrimitiveType();
        typeA.setName(type);
        literalA.setType(typeA);
        return literalA;
    }
}
