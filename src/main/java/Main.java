import dataStructure.ContinuousVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;

import stateSpace.SoftwareState;
import stateSpace.SpaceStateGenerator;
import utils.CompilerUtil;
import utils.FlowstarExecutor;
import visitors.ExpressionEvaluatorVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        Main mainInstance = new Main();
        mainInstance.run(args);
    }

    public void run(String[] args) {
        try {
            // Uncomment if needed
            // Utils.getUserPassword();
            CompilerUtil.compile("heater.txt");
            final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
            FlowstarExecutor.analyzeFlowstarModel("", Arrays.asList("x", "v"));
            SpaceStateGenerator spaceStateGenerator = new SpaceStateGenerator();
            spaceStateGenerator.analyzeReachability(Main::callJoszef);

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

    static {
        System.loadLibrary("hypro"); // Load native library hello.dll (Windows) or libhello.so (Unixes)
        //  at runtime
        // This library contains a native method called sayHello()
    }

    public static void callJoszef(String[] ODEs, double [] intervals, double [] reachParams) {
//      String[] ODEs = {"x'=-0.5-y-1.5*x*x-0.5*x*x*x", "y'=3*x-y"};
//      String[] ODEs = {"timer'=1", "tempr'=0.1"};
//        double [] intervals = new double [] {0.8, 1.2, 0.8, 1.2};
//
//        // reachability parameters
//        // Max_iter_number(int), Stop_ration, timeStepSize, Taylor_model_Order(int), Time
//        double [] reachParams = new double [] {50.0, 0.99, 0.01, 7.0, 5};

        double[] resultArray = new HelloJNI().sayHello(ODEs, intervals, reachParams);  // Create an instance and invoke the native method

        System.out.println("Results from Java");

        for (int i = 0; i<resultArray.length; i++)
            System.out.println(resultArray[i]);

    }

    @FunctionalInterface
    public interface ODECallback {
        void call(String[] ODEs);
    }


}

