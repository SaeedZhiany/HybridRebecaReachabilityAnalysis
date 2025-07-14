package stateSpace;

import dataStructure.StringVariable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.ModeDeclaration;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;

import utils.CompilerUtil;
import visitors.ExpressionExtractorVisitor;

import java.util.*;

import static utils.CompilerUtil.getHybridRebecaCode;

public class RebecInstantiationMapping {
    private Map<String, Map<String, String>> knownRebecsMap;
    private Map<String, String> rebecsDeclarationMap;
    private Map<Set<String>, Map<String, Expression>> modeToODEs;
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
    }


    private static Map<Set<String>, Map<String, Expression>> getModeODEs(HybridRebecaCode hybridRebecaCode) {
        List<MainRebecDefinition> allRebecNodes = hybridRebecaCode.getMainDeclaration().getMainRebecDefinition();
        List<PhysicalClassDeclaration> allPhysicalClassDeclaration = hybridRebecaCode.getPhysicalClassDeclaration();
        ExpressionExtractorVisitor expressionExtractorVisitor = new ExpressionExtractorVisitor();

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

        Map<Set<String>, Map<String, Expression>> modeToODEs = new HashMap<>();

        for (Map.Entry<MainRebecDefinition, PhysicalClassDeclaration> entry : allPhysicalNodes.entrySet()) {
            MainRebecDefinition mainRebecDefinition = entry.getKey();
            PhysicalClassDeclaration declaration = entry.getValue();
            for (ModeDeclaration mode : declaration.getModeDeclarations()) {
                List<Statement> statements = mode.getInvariantDeclaration().getBlock().getStatements();
                Map<String, Expression> ODEs = new HashMap<>();
                String nameOfPhisicalVarible = "";
                for (int i = 0; i < statements.size(); i++) {
                    Statement statement = statements.get(i);
                    nameOfPhisicalVarible = mainRebecDefinition.getName();
                    expressionExtractorVisitor.setNameOfPhisicalVarible(nameOfPhisicalVarible);
                    String keyName = expressionExtractorVisitor.getNameOfPhisicalVarible() +
                            ((TermPrimary) ((BinaryExpression) statement).getLeft()).getName() + "'";
                    Expression ODEExpression = ((BinaryExpression) statement).getRight();
                    ODEs.put(keyName, ODEExpression);
                }

                Set<String> keySet = new HashSet<>();
                Collections.addAll(keySet, nameOfPhisicalVarible, mode.getName());

                if (modeToODEs.containsKey(keySet)) {
                    modeToODEs.get(keySet).putAll(ODEs);
                } else {
                    modeToODEs.put(keySet, ODEs);
                }

            }
        }
        return modeToODEs;
    }

    public Map<String, Expression> getActorODEs(String actorName, String mode) {
        HashSet<String> actorNameMode = new HashSet<>();
        actorNameMode.add(actorName);
        actorNameMode.add(mode);
        return modeToODEs.get(actorNameMode);
    }

    public String[] appendArrays(String[] a, String[] b) {
        List<String> combinedList = new ArrayList<>(Arrays.asList(a));
        combinedList.addAll(Arrays.asList(b));
        return combinedList.toArray(new String[0]);
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
