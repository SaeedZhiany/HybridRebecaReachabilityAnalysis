package stateSpace;

import dataStructure.ContinuousVariable;
import dataStructure.DiscreteDecimalVariable;
import dataStructure.IntervalRealVariable;
import org.junit.jupiter.api.Test;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import utils.CompilerUtil;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpaceStateGeneratorTest {

    @Test
    public void createSoftwareTest() throws Exception {
        CompilerUtil.compile("src/test/resources", "softwareCreation.txt");
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        SoftwareState softwareState = spaceStateGenerator.createSoftwareState(hybridRebecaCode.getReactiveClassDeclaration().get(0), hybridRebecaCode.getMainDeclaration().getMainRebecDefinition().get(0));
        assertEquals(softwareState.getResumeTime(), new ContinuousVariable("resumeTime", 0.0, 0.0));
        assertEquals(softwareState.variablesValuation.get("timer"), new IntervalRealVariable("timer", 1.5, 1.5));
        assertEquals(softwareState.variablesValuation.get("inValue"), new DiscreteDecimalVariable("inValue", new BigDecimal(5)));
        assertEquals(softwareState.variablesValuation.get("defaultF"), new IntervalRealVariable("defaultF", 0.0, 0.0));
        assertEquals(softwareState.variablesValuation.get("tempr"), new IntervalRealVariable("tempr", 20.0, 20.0));
        assertEquals(softwareState.variablesValuation.get("defaultI"), new DiscreteDecimalVariable("defaultI", new BigDecimal(0)));
    }

    @Test
    public void createPhysicalTest() throws Exception {
        CompilerUtil.compile("src/test/resources", "physicalCreation.txt");
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        PhysicalState physicalState = spaceStateGenerator.createPhysicalState(hybridRebecaCode.getPhysicalClassDeclaration().get(0), hybridRebecaCode.getMainDeclaration().getMainRebecDefinition().get(0));
        assertEquals(physicalState.variablesValuation.get("timer"), new IntervalRealVariable("timer", 1.5, 1.5));
        assertEquals(physicalState.variablesValuation.get("inValue"), new DiscreteDecimalVariable("inValue", new BigDecimal(5)));
        assertEquals(physicalState.variablesValuation.get("defaultF"), new IntervalRealVariable("defaultF", 0.0, 0.0));
        assertEquals(physicalState.variablesValuation.get("temp"), new IntervalRealVariable("temp", 20.0, 20.0));
        assertEquals(physicalState.variablesValuation.get("defaultI"), new DiscreteDecimalVariable("defaultI", new BigDecimal(0)));
        assertEquals(physicalState.getMode(), "Off");
    }

    @Test
    public void createHybridTest() throws Exception {
        CompilerUtil.compile("src/test/resources", "hybridStateCreation.txt");
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
        HybridState hybridState = spaceStateGenerator.makeInitialState();
        assertEquals(hybridState.getPhysicalStates().get("pp").getMode(), "Off");
        assertEquals(hybridState.getPhysicalStates().get("pp").getVariableValuation().get("timer"), new IntervalRealVariable("timer", 2.5, 2.5));
        assertEquals(hybridState.getPhysicalStates().get("pp").getVariableValuation().get("inValue"), new DiscreteDecimalVariable("inValue", new BigDecimal(6)));
        assertEquals(hybridState.getPhysicalStates().get("pp").getVariableValuation().get("defaultF"), new IntervalRealVariable("defaultF", 0.0, 0.0));
        assertEquals(hybridState.getPhysicalStates().get("pp").getVariableValuation().get("temp"), new IntervalRealVariable("temp", 20.0, 20.0));
        assertEquals(hybridState.getPhysicalStates().get("pp").getVariableValuation().get("defaultI"), new DiscreteDecimalVariable("defaultI", new BigDecimal(0)));
        assertEquals(hybridState.getSoftwareStates().get("ss").getResumeTime(), new ContinuousVariable("resumeTime", 0.0, 0.0));
        assertEquals(hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("timer"), new IntervalRealVariable("timer", 0.5, 0.5));
        assertEquals(hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("inValue"), new DiscreteDecimalVariable("inValue", new BigDecimal(7)));
        assertEquals(hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("defaultF"), new IntervalRealVariable("defaultF", 0.0, 0.0));
        assertEquals(hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("tempr"), new IntervalRealVariable("tempr", 20.0, 20.0));
        assertEquals(hybridState.getSoftwareStates().get("ss").getVariablesValuation().get("defaultI"), new DiscreteDecimalVariable("defaultI", new BigDecimal(0)));
    }
}
