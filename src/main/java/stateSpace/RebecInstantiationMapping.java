package stateSpace;

import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import utils.CompilerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.CompilerUtil.getHybridRebecaCode;

public class RebecInstantiationMapping {
    // Map <rebecName, <knownRebecsDeclration,
    private Map<String, Map<String, String>> knownRebecsMap;
    // Map <rebecName, rebecType>
    private Map<String, String> rebecsDeclarationMap;

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
