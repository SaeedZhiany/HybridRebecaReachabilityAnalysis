import org.checkerframework.checker.units.qual.s;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridTermPrimary;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.ModeDeclaration;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;
import stateSpace.GlobalState;
import utils.CompilerUtil;
import utils.FlowstarExecutor;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            CompilerUtil.compile("heater.txt");
            final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();

            Thread myThread = new Thread(() -> {
                try {
                    FlowstarExecutor.analyzeFlowstarModel("", Arrays.asList("x","v"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            myThread.start();

            GlobalState globalState = new GlobalState("On");
            String[] ODEs = getODEs(globalState, hybridRebecaCode);
            System.out.println(ODEs[0]);
            System.out.println(ODEs[1]);
//            Thread.sleep(3000);
            callJoszef(ODEs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] getODEs(GlobalState globalState, HybridRebecaCode hybridRebecaCode) {
        List<MainRebecDefinition> allRebecNodes = hybridRebecaCode.getMainDeclaration().getMainRebecDefinition();
        List<PhysicalClassDeclaration> allPhysicalClassDeclaration = hybridRebecaCode.getPhysicalClassDeclaration();

        // get physical nodes from main
        HashMap<MainRebecDefinition, PhysicalClassDeclaration> allPhysicalNodes = new HashMap<>();
        for (MainRebecDefinition node : allRebecNodes) {
            PhysicalClassDeclaration temp = null;
            for (PhysicalClassDeclaration classDeclaration : allPhysicalClassDeclaration) {
                if (classDeclaration.getName().equals(((OrdinaryPrimitiveType) node.getType()).getName())) {
                    allPhysicalNodes.put(node, classDeclaration);
                    break;
                }
            }
        }

        // get ODEs
        HashMap<String, String[]> modeToODEs = new HashMap<>();
        for (Map.Entry<MainRebecDefinition, PhysicalClassDeclaration> entry : allPhysicalNodes.entrySet()) {
            MainRebecDefinition var = entry.getKey();
            PhysicalClassDeclaration declaration = entry.getValue();
            for (ModeDeclaration mode : declaration.getModeDeclarations()) {
                List<Statement> statements = mode.getInvariantDeclaration().getBlock().getStatements();
                String[] ODEs = new String[statements.size()];
                for (int i = 0; i < statements.size(); i++) {
                    Statement statement = statements.get(i);
                    String ODE = ((TermPrimary) ((BinaryExpression) statement).getLeft()).getName();

                    // add name of physical class
                    ODE += "_" + var.getName();

                    int derivativeOrder = 0;
                    try {
                        derivativeOrder = ((HybridTermPrimary) ((BinaryExpression) statement).getLeft()).getDerivativeOrder();
                    } catch (Exception e) {
                        derivativeOrder = 0;
                    }
                    ODE += repeatCharacter('\'', derivativeOrder);
                    ODE += ((BinaryExpression) statement).getOperator();

                    // get right
                    if (((BinaryExpression) statement).getRight() instanceof Literal) {
                        ODE += ((Literal) (((BinaryExpression) statement).getRight())).getLiteralValue();
                    } else if (((BinaryExpression) statement).getRight() instanceof UnaryExpression) {
                        ODE += ((UnaryExpression) ((BinaryExpression) statement).getRight()).getOperator();
                        ODE += ((Literal) ((UnaryExpression) ((BinaryExpression) statement).getRight()).getExpression()).getLiteralValue();
                    }

                    //                System.out.println(ODE);

                    ODEs[i] = ODE;
                }

                modeToODEs.put(mode.getName(), ODEs);
            }
        }

        String globalStateMode = globalState.getMode();
        return modeToODEs.get(globalStateMode);
    }

    static {
        System.loadLibrary("hypro"); // Load native library hello.dll (Windows) or libhello.so (Unixes)
        //  at runtime
        // This library contains a native method called sayHello()
    }

    public static void callJoszef(String[] ODEs) {
//      String[] ODEs = {"x'=-0.5-y-1.5*x*x-0.5*x*x*x", "y'=3*x-y"};
//      String[] ODEs = {"timer'=1", "tempr'=0.1"};
        double [] intervals = new double [] {0.8, 1.2, 0.8, 1.2};

        // reachability parameters
        // Max_iter_number(int), Stop_ration, timeStepSize, Taylor_model_Order(int), Time
        double [] reachParams = new double [] {50.0, 0.99, 0.01, 7.0, 5};

        double[] resultArray = new HelloJNI().sayHello(ODEs, intervals, reachParams);  // Create an instance and invoke the native method

        System.out.println("Results from Java");

        for (int i = 0; i<resultArray.length; i++)
            System.out.println(resultArray[i]);

    }

    public static String repeatCharacter(char ch, int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

}
