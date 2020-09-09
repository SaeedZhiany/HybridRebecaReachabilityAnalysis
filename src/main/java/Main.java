import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import utils.CompilerUtil;
import utils.FlowstarExecutor;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {
//            Utils.getUserPassword();
            CompilerUtil.compile("heater.txt");
            final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
            FlowstarExecutor.analyzeFlowstarModel("", Arrays.asList("x","v"));

            /*PhysicalState physicalState = new PhysicalState(
                    "HeaterWithSensor",
                    "On",
                    new HashMap<String, dataStructure.Variable>(){{
                        put("tempr", new Variable("tempr"));
                        put("timer", new Variable("timer"));
                        put("myVar", new Variable("myVar", new BigDecimal(5)));
                    }},
                    new PriorityQueue<>(1),
                    new Sigma(""),
                    0
            );
            HybridRebecaToFlowstarConverter.generateFlowstarModelOutputFile(physicalState);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
