package stateSpace;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import org.junit.jupiter.api.Test;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import utils.CompilerUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public class SpaceStateGeneratorTest {

    @Test
    public void TestCreateSoftwareState() throws Exception {
        CompilerUtil.compile("src/test/resources", "softwareCreation.txt");
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        SoftwareState softwareState = spaceStateGenerator.createSoftwareState(hybridRebecaCode.getReactiveClassDeclaration().get(0), hybridRebecaCode.getMainDeclaration().getMainRebecDefinition().get(0));
        assertEquals(new ContinuousVariable("resumeTime", 0.0, 0.0), softwareState.getResumeTime());
        assertEquals(new IntervalRealVariable("timer", 1.5, 1.5), softwareState.variablesValuation.get("timer"));
        assertEquals(new DiscreteDecimalVariable("inValue", new BigDecimal(5)), softwareState.variablesValuation.get("inValue"));
        assertEquals(new IntervalRealVariable("defaultF", 0.0, 0.0), softwareState.variablesValuation.get("defaultF"));
        assertEquals(new IntervalRealVariable("tempr", 20.0, 20.0), softwareState.variablesValuation.get("tempr"));
        assertEquals(new DiscreteDecimalVariable("defaultI", new BigDecimal(0)), softwareState.variablesValuation.get("defaultI"));
    }

    @Test
    public void TestCreatePhysicalState() throws Exception {
        CompilerUtil.compile("src/test/resources", "physicalCreation.txt");
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        PhysicalState physicalState = spaceStateGenerator.createPhysicalState(hybridRebecaCode.getPhysicalClassDeclaration().get(0), hybridRebecaCode.getMainDeclaration().getMainRebecDefinition().get(0));
        assertEquals(new IntervalRealVariable("timer", 1.5, 1.5), physicalState.variablesValuation.get("timer"));
        assertEquals(new DiscreteDecimalVariable("inValue", new BigDecimal(5)), physicalState.variablesValuation.get("inValue"));
        assertEquals(new IntervalRealVariable("defaultF", 0.0, 0.0), physicalState.variablesValuation.get("defaultF"));
        assertEquals(new IntervalRealVariable("temp", 20.0, 20.0), physicalState.variablesValuation.get("temp"));
        assertEquals(new DiscreteDecimalVariable("defaultI", new BigDecimal(0)), physicalState.variablesValuation.get("defaultI"));
        assertEquals("Off", physicalState.getMode());
    }

    @Test
    public void TestCreateHybridState() throws Exception {
        CompilerUtil.compile("src/test/resources", "hybridStateCreation.txt");
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        HybridState hybridState = spaceStateGenerator.makeInitialState();
        assertEquals("Off", hybridState.getPhysicalStates().get("pp").getMode());
        assertEquals(new IntervalRealVariable("timer", 2.5, 2.5), hybridState.getPhysicalStates().get("pp").getVariableValuation().get("timer"));
        assertEquals(new DiscreteDecimalVariable("inValue", new BigDecimal(6)), hybridState.getPhysicalStates().get("pp").getVariableValuation().get("inValue"));
        assertEquals(new IntervalRealVariable("defaultF", 0.0, 0.0), hybridState.getPhysicalStates().get("pp").getVariableValuation().get("defaultF"));
        assertEquals(new IntervalRealVariable("temp", 20.0, 20.0), hybridState.getPhysicalStates().get("pp").getVariableValuation().get("temp"));
        assertEquals(new DiscreteDecimalVariable("defaultI", new BigDecimal(0)), hybridState.getPhysicalStates().get("pp").getVariableValuation().get("defaultI"));
        assertEquals(new ContinuousVariable("resumeTime", 0.0, 0.0), hybridState.getSoftwareStates().get("ss").getResumeTime());
        assertEquals(new IntervalRealVariable("timer", 0.5, 0.5), hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("timer"));
        assertEquals(new DiscreteDecimalVariable("inValue", new BigDecimal(7)), hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("inValue"));
        assertEquals(new IntervalRealVariable("defaultF", 0.0, 0.0), hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("defaultF"));
        assertEquals(new IntervalRealVariable("tempr", 20.0, 20.0), hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("tempr"));
        assertEquals(new DiscreteDecimalVariable("defaultI", new BigDecimal(0)), hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("defaultI"));
    }

    @Test
    void TestIsReachedEndYetTrueCase() {
        Queue<HybridState> queue = new LinkedList<>();
        double endSimulation = 2;

        ContinuousVariable hybridStateTime1 = new ContinuousVariable("globalTime", 2.5, 3.5);
        HybridState hybridState1 = new HybridState(hybridStateTime1, new HashMap<>(), new HashMap<>(), new CANNetworkState());
        queue.add(hybridState1);

        ContinuousVariable hybridStateTime2 = new ContinuousVariable("globalTime", 1.5, 2.5);
        HybridState hybridState2 = new HybridState(hybridStateTime2, new HashMap<>(), new HashMap<>(), new CANNetworkState());
        queue.add(hybridState2);

        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        Boolean isReachable = spaceStateGenerator.isReachedEndYet(queue, endSimulation);
        assertTrue(isReachable);
    }

    @Test
    void TestIsReachedEndYetFalseCase() {
        Queue<HybridState> queue = new LinkedList<>();
        double endSimulation = 2;

        ContinuousVariable hybridStateTime1 = new ContinuousVariable("globalTime", 2.5, 3.5);
        HybridState hybridState1 = new HybridState(hybridStateTime1, new HashMap<>(), new HashMap<>(), new CANNetworkState());
        queue.add(hybridState1);

        ContinuousVariable hybridStateTime2 = new ContinuousVariable("globalTime", 3.5, 4.5);
        HybridState hybridState2 = new HybridState(hybridStateTime2, new HashMap<>(), new HashMap<>(), new CANNetworkState());
        queue.add(hybridState2);

        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        Boolean isReachable = spaceStateGenerator.isReachedEndYet(queue, endSimulation);
        assertFalse(isReachable);
    }
}
