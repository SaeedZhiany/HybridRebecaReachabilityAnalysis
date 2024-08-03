package stateSpace;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridTermPrimary;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.ModeDeclaration;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;

import utils.CompilerUtil;

import java.util.*;

import static utils.CompilerUtil.getHybridRebecaCode;

public class RebecInstantiationMapping {
    // Map <rebecName, <knownRebecsDeclration,
    private Map<String, Map<String, String>> knownRebecsMap;
    // Map <rebecName, rebecType>
    private Map<String, String> rebecsDeclarationMap;

    private Map<Set<String>, String[]> modeToODEs;

    private static RebecInstantiationMapping rebecInstantiationMapping;

    private RebecInstantiationMapping() {
        rebecsDeclarationMap= new HashMap<>();
        knownRebecsMap = new HashMap<>();
        List<ReactiveClassDeclaration> reactiveClassDeclarationList = getHybridRebecaCode().getReactiveClassDeclaration();
        MainDeclaration mainDeclaration = getHybridRebecaCode().getMainDeclaration();
        for (MainRebecDefinition mainRebecDefinition : mainDeclaration.getMainRebecDefinition()) {
            String rebecName = mainRebecDefinition.getName(); //hws2
            String rebecType = ((OrdinaryPrimitiveType) mainRebecDefinition.getType()).getName(); //HeaterWithSensor
            rebecsDeclarationMap.put(rebecName, rebecType);
            Map<String, String> knownRebecInstances = new HashMap<>();
            ReactiveClassDeclaration reactiveClassDeclaration = CompilerUtil.getReactiveClassDeclaration(rebecType);
            if (reactiveClassDeclaration == null) {
                reactiveClassDeclaration = CompilerUtil.getPhysicalClassDeclaration(rebecType);
            }
            if (reactiveClassDeclaration == null) {
                throw new RuntimeException("Reactive class " + rebecType + " not found.");
            }
            List<FieldDeclaration> variableDeclarators = reactiveClassDeclaration.getKnownRebecs();
            for (int i = 0 ;i < mainRebecDefinition.getBindings().size() ; i++) {
                knownRebecInstances.put(variableDeclarators.get(i).getVariableDeclarators().get(0).getVariableName(),
                        ((TermPrimary) mainRebecDefinition.getBindings().get(i)).getName());
            }
            knownRebecsMap.put(rebecName, knownRebecInstances);
        }

        final HybridRebecaCode hybridRebecaCode = getHybridRebecaCode();
        modeToODEs = getModeODEs(hybridRebecaCode);

//        System.out.println("\n-----------------\nmodeToODEs");
//        for (Set<String> key : modeToODEs.keySet()) {
//            String[] value = modeToODEs.get(key);
//
//            System.out.println("Key: " + key);
//            System.out.println("Value: ");
//            for (String ode : value) {
//                System.out.println(ode);
//            }
//            System.out.println("----");
//        }
    }


    private static Map<Set<String>, String[]> getModeODEs(HybridRebecaCode hybridRebecaCode) {
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
        Map<Set<String>, String[]> modeToODEs = new HashMap<>();
        for (Map.Entry<MainRebecDefinition, PhysicalClassDeclaration> entry : allPhysicalNodes.entrySet()) {
            MainRebecDefinition mainRebecDefinition = entry.getKey();
            PhysicalClassDeclaration declaration = entry.getValue();
            for (ModeDeclaration mode : declaration.getModeDeclarations()) {
                List<Statement> statements = mode.getInvariantDeclaration().getBlock().getStatements();
                String[] ODEs = new String[statements.size()];
                String nameOfPhisicalVarible = "";
                for (int i = 0; i < statements.size(); i++) {
                    Statement statement = statements.get(i);

                    // get left -> write visitor for HybridTermPrimary
                    String ODE = ((TermPrimary) ((BinaryExpression) statement).getLeft()).getName();

                    // add name of physical class
                    nameOfPhisicalVarible = mainRebecDefinition.getName();
                    ODE += "_" + nameOfPhisicalVarible;

                    int derivativeOrder = 0;
                    try {
                        derivativeOrder = ((HybridTermPrimary) ((BinaryExpression) statement).getLeft()).getDerivativeOrder();
                    } catch (Exception e) {
                        derivativeOrder = 0;
                    }
                    ODE += repeatCharacter('\'', derivativeOrder);
                    ODE += ((BinaryExpression) statement).getOperator();

                    //                     try to use visitor
//                    SymbolTable s = CompilerUtil.getSymbolTable();
//                    Map<String, Variable> symbolTable = getSymbolTableAsMap(declaration);
//                    ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(symbolTable);
//                    Variable actual = expressionEvaluatorVisitor.visit(((BinaryExpression) statement).getRight());

                    // get right -> symbol table needed
                    if (((BinaryExpression) statement).getRight() instanceof Literal) {
                        ODE += ((Literal) (((BinaryExpression) statement).getRight())).getLiteralValue();
                    } else if (((BinaryExpression) statement).getRight() instanceof UnaryExpression) {
                        ODE += ((UnaryExpression) ((BinaryExpression) statement).getRight()).getOperator();
                        ODE += ((Literal) ((UnaryExpression) ((BinaryExpression) statement).getRight()).getExpression()).getLiteralValue();
                    }

                    ODEs[i] = ODE;
                }

                Set<String> keySet = new HashSet<>();
                Collections.addAll(keySet, new String[]{nameOfPhisicalVarible, mode.getName()});

                if (modeToODEs.containsKey(keySet)) {
                    String[] existingODEs = modeToODEs.get(keySet);
                    String[] combinedODEs = new String[existingODEs.length + ODEs.length];
                    System.arraycopy(existingODEs, 0, combinedODEs, 0, existingODEs.length);
                    System.arraycopy(ODEs, 0, combinedODEs, existingODEs.length, ODEs.length);
                    modeToODEs.put(keySet, combinedODEs);
                } else {
                    modeToODEs.put(keySet, ODEs);
                }

            }
        }

        return modeToODEs;
    }

    public String[] getCurrentFlows(List<Set<String>> modes) {
        String[] result = new String[]{};
        for(Set<String> mode : modes) {
            String[] flows = modeToODEs.get(mode);
            result = appendArrays(result, flows);
        }

        return result;
    }

    public String[] appendArrays(String[] a, String[] b) {
        List<String> combinedList = new ArrayList<>(Arrays.asList(a));
        combinedList.addAll(Arrays.asList(b));
        return combinedList.toArray(new String[0]);
    }

    public static String repeatCharacter(char ch, int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    public static RebecInstantiationMapping getInstance() {
        if (rebecInstantiationMapping == null) {
            rebecInstantiationMapping = new RebecInstantiationMapping();
        }
        return rebecInstantiationMapping;
    }

    public String getKnownRebecBinding(String rebecName, String knownRebecName) {
        return knownRebecsMap.get(rebecName).get(knownRebecName);
    }

    public String getRebecReactiveClassType(String actorName) {
        return rebecsDeclarationMap.get(actorName);
    }
}
