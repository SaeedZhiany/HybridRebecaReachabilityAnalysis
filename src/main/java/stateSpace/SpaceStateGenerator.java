package stateSpace;

import com.fasterxml.jackson.core.JsonProcessingException;
import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;
import utils.CompilerUtil;
import visitors.BlockStatementExecutorVisitor;
import visitors.ExpressionEvaluatorVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class SpaceStateGenerator {

    public SpaceStateGenerator() {

    }

    @FunctionalInterface
    public interface JoszefCaller {
        double[] call(String[] ODEs, double [] intervals, double [] reachParams);
    }

    public void analyzeReachability(JoszefCaller joszefCaller) {
        double currentEvent = 0.0;
        Queue<HybridState> queue = new LinkedList<>();
        Set<HybridState> visitedStates = new HashSet<>();
        HybridState initialState = makeInitialState();
        queue.add(initialState);
        visitedStates.add(initialState);

        while (!queue.isEmpty()) {
            HybridState state = queue.poll();

            List<Set<String>> globalStateModes = state.getGlobalStateModes();

            String[] ODEs = RebecInstantiationMapping.getInstance().getCurrentFlows(globalStateModes);

            double[] intervals = state.getIntervals(ODEs);
//            double[] intervals = new double[]{0.0, 0.0, 20.0, 20.0};

            double timeInterval = 0.01;
            double[] nextEvents = state.getEvents(currentEvent, timeInterval);
            double[] nextEventInterval = {currentEvent, Arrays.stream(nextEvents).min().orElseThrow()};

            // step_size is fixed
            double[] reachParams = new double[]{50.0, 0.99, 0.01, 7.0, timeInterval};

            if (ODEs.length > 0) {
//                 double[] result = joszefCaller.call(ODEs, intervals, reachParams);
                //update i
            }
        }


        // Queue<HybridState> states = new Queue(makeInitialState);
        // while queue is not empty {
        // state = queue.pop() -> hybridState
        // odes = getCurrentFlows
        // intervals = state->physicalstate->getvariablevaluation
        // timeInterval = 0.1
        // reachparams -> time = interval -> step_size = interval
        // reault = computeFlowPipe(odes, intervals, timeInterval) -> calljuze
        // update state with result
        // copy state
        // loop for each updated physical
        //    update local state physical
        //    if guard holds execute statement
        //
        // }

    }

    private HybridState makeInitialState() {
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        HashMap<String, SoftwareState> softwareStates = new HashMap<>();
        HashMap<String, PhysicalState> physicalStates = new HashMap<>();

        List<MainRebecDefinition> mainRebecDefinitions = CompilerUtil.getHybridRebecaCode().getMainDeclaration().getMainRebecDefinition();
        for (MainRebecDefinition mainRebecDefinition : mainRebecDefinitions) {
            OrdinaryPrimitiveType type = (OrdinaryPrimitiveType) mainRebecDefinition.getType();
            ReactiveClassDeclaration reactiveClassDeclaration = CompilerUtil.getReactiveClassDeclaration(type.getName());
            if (reactiveClassDeclaration == null) {
                PhysicalClassDeclaration physicalClassDeclaration = CompilerUtil.getPhysicalClassDeclaration(type.getName());
                if (physicalClassDeclaration == null) {
                    throw new RuntimeException("Main class not found");
                }
//                physicalStates.add(createPhysicalState(physicalClassDeclaration, mainRebecDefinition));
                PhysicalState physicalState = createPhysicalState(physicalClassDeclaration, mainRebecDefinition);
                physicalStates.put(physicalState.getActorName(), physicalState);
            }
            else {
//                softwareStates.add(createSoftwareState(reactiveClassDeclaration, mainRebecDefinition));
                SoftwareState softwareState = createSoftwareState(reactiveClassDeclaration, mainRebecDefinition);
                softwareStates.put(softwareState.getActorName(), softwareState);
            }
        }
        return new HybridState(new ContinuousVariable("globalTime"),  softwareStates,  physicalStates, new CANNetworkState());
    }

    private SoftwareState createSoftwareState(ReactiveClassDeclaration reactiveClassDeclaration, MainRebecDefinition mainRebecDefinition) {
        ConstructorDeclaration constructorDeclaration = getConstructor(reactiveClassDeclaration.getConstructors(), mainRebecDefinition.getArguments());
        if (constructorDeclaration == null) {
            throw new RuntimeException("Constructor not found");
        }
        Map<String, Variable> variableValuationInitial = new HashMap<>();
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        for (FormalParameterDeclaration formalParameterDeclaration : constructorDeclaration.getFormalParameters()) {
            Variable variable = expressionEvaluatorVisitor.visit(mainRebecDefinition.getArguments().get(constructorDeclaration.getFormalParameters().indexOf(formalParameterDeclaration)));
            variableValuationInitial.put(formalParameterDeclaration.getName(), variable);
        }

        List<FieldDeclaration> stateVars = reactiveClassDeclaration.getStatevars();
        for (FieldDeclaration stateVar : stateVars) {
            String type = ((OrdinaryPrimitiveType)stateVar.getType()).getName();
            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
                switch (type) {
                    case "int":
                    case "byte":
                    case "short": {
                        variableValuationInitial.put(variableDeclarator.getVariableName(),
                                new DiscreteDecimalVariable(variableDeclarator.getVariableName(), new BigDecimal(0)));
                        break;
                    }
                    case "float":
                    case "double": {
                        variableValuationInitial.put(variableDeclarator.getVariableName(),
                                new IntervalRealVariable(variableDeclarator.getVariableName(), 0.0));
                        break;
                    }
                    case "boolean": {
                        variableValuationInitial.put(variableDeclarator.getVariableName(),
                                new DiscreteBoolVariable(variableDeclarator.getVariableName(), false));
                        break;
                    }
                }
            }

        }
        BlockStatementExecutorVisitor blockStatementExecutorVisitor = new BlockStatementExecutorVisitor(variableValuationInitial);
        blockStatementExecutorVisitor.visit(constructorDeclaration.getBlock());
        HashMap<String, Variable> variableValuation = new HashMap<>();
        for (FieldDeclaration stateVar : stateVars) {
            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
                variableValuation.put(variableDeclarator.getVariableName(), variableValuationInitial.get(variableDeclarator.getVariableName()));
            }
        }
        return new SoftwareState(mainRebecDefinition.getName(), variableValuation, new HashSet<>(), new ArrayList<>(), 0, new ContinuousVariable("resumeTime"));


    }

    private PhysicalState createPhysicalState(PhysicalClassDeclaration physicalClassDeclaration, MainRebecDefinition mainRebecDefinition) {
        ConstructorDeclaration constructorDeclaration = getConstructor(physicalClassDeclaration.getConstructors(), mainRebecDefinition.getArguments());
        if (constructorDeclaration == null) {
            throw new RuntimeException("Constructor not found");
        }
        Map<String, Variable> variableValuationInitial = new HashMap<>();
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(new HashMap<>());
        for (FormalParameterDeclaration formalParameterDeclaration : constructorDeclaration.getFormalParameters()) {
            Variable variable = expressionEvaluatorVisitor.visit(mainRebecDefinition.getArguments().get(constructorDeclaration.getFormalParameters().indexOf(formalParameterDeclaration)));
            variableValuationInitial.put(formalParameterDeclaration.getName(), variable);
        }

        List<FieldDeclaration> stateVars = physicalClassDeclaration.getStatevars();
        for (FieldDeclaration stateVar : stateVars) {
            String type = ((OrdinaryPrimitiveType)stateVar.getType()).getName();
            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
                switch (type) {
                    case "int":
                    case "byte":
                    case "short": {
                        variableValuationInitial.put(variableDeclarator.getVariableName(),
                                new DiscreteDecimalVariable(variableDeclarator.getVariableName(), new BigDecimal(0)));
                        break;
                    }
                    case "float":
                    case "double": {
                        variableValuationInitial.put(variableDeclarator.getVariableName(),
                                new IntervalRealVariable(variableDeclarator.getVariableName(), 0.0));
                        break;
                    }
                    case "boolean": {
                        variableValuationInitial.put(variableDeclarator.getVariableName(),
                                new DiscreteBoolVariable(variableDeclarator.getVariableName(), false));
                        break;
                    }
                }
            }

        }
        BlockStatementExecutorVisitor blockStatementExecutorVisitor = new BlockStatementExecutorVisitor(variableValuationInitial, "none");
        blockStatementExecutorVisitor.visit(constructorDeclaration.getBlock());
        HashMap<String, Variable> variableValuation = new HashMap<>();
        for (FieldDeclaration stateVar : stateVars) {
            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
                variableValuation.put(variableDeclarator.getVariableName(), variableValuationInitial.get(variableDeclarator.getVariableName()));
            }
        }
        return new PhysicalState(mainRebecDefinition.getName(), blockStatementExecutorVisitor.getMode(), variableValuation, new HashSet<>(), new ArrayList<>(), 0);
    }

    private ConstructorDeclaration getConstructor(List<ConstructorDeclaration> constructorDeclarations, List<Expression> declarationArgs) {
        for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
            List<FormalParameterDeclaration> parameterDeclarations = constructorDeclaration.getFormalParameters();
            if (parameterDeclarations.size() != declarationArgs.size()) {
                continue;
            }
            boolean isFound = true;
            for (FormalParameterDeclaration parameterDeclaration : parameterDeclarations) {
                OrdinaryPrimitiveType formalParameterType = (OrdinaryPrimitiveType) parameterDeclaration.getType();
                OrdinaryPrimitiveType actualParameterType = ((OrdinaryPrimitiveType) declarationArgs.get(parameterDeclarations.indexOf(parameterDeclaration)).getType());
                if (!formalParameterType.getName().equals(actualParameterType.getName())) {
                    isFound = false;
                    break;
                }
            }
            if (isFound) {
                return constructorDeclaration;
            }
        }
        return null;
    }
}
