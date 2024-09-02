package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;
import sos.NonTimeProgressSOSExecutor;
import utils.CompilerUtil;
import utils.ReachabilityAnalysisGraph;
import visitors.BlockStatementExecutorVisitor;
import visitors.ExpressionEvaluatorVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

import static stateSpace.HybridState.extractVariableNames;

public class SpaceStateGenerator {

    public SpaceStateGenerator() {

    }

    @FunctionalInterface
    public interface JoszefCaller {
        double[] call(String[] ODEs, double[] intervals, double[] reachParams);
    }

    public void analyzeReachability(JoszefCaller joszefCaller) {
        double currentEvent = 0.0;
        NonTimeProgressSOSExecutor nonTimeProgressSOSExecutor = new NonTimeProgressSOSExecutor();
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        Queue<HybridState> queue = new LinkedList<>();
        HybridState initialState = makeInitialState();
        ReachabilityAnalysisGraph reachabilityAnalysisGraph = new ReachabilityAnalysisGraph(initialState);
        queue.addAll(nonTimeProgressSOSExecutor.generateNextStates(initialState));

        while (!queue.isEmpty()) { // should add time upper bound
            HybridState state = queue.poll();
            ReachabilityAnalysisGraph.TreeNode rootNode = reachabilityAnalysisGraph.findNodeInGraph(state);

            List<Set<String>> globalStateModes = state.getGlobalStateModes();

            String[] ODEs = RebecInstantiationMapping.getInstance().getCurrentFlows(globalStateModes);

            double[] intervals = state.getIntervals(ODEs);
//            double[] intervals = new double[]{0.0, 0.0, 20.0, 20.0};

            double timeInterval = 0.01;
            double[] nextEvents = state.getEvents(currentEvent, timeInterval);
            double[] nextEventInterval = {currentEvent, Arrays.stream(nextEvents).min().orElseThrow()};
            // min from nearest lower bound resume bound and step_size

            // step_size is fixed
            double[] reachParams = new double[]{50.0, 0.99, 0.01, 7.0, timeInterval};

            Cloner cloner = new Cloner();
            HybridState updatedPhysicalHybridState = cloner.deepClone(state);
            Map<String, HybridState> updatedPhysicalHybridStates = new HashMap<>();
            updatedPhysicalHybridStates.put(updatedPhysicalHybridState.updateHash(), updatedPhysicalHybridState);

            if (ODEs.length > 0) {
                double[] result = joszefCaller.call(ODEs, intervals, reachParams);
                //update i

                int index = 0;
                for (String ODE : ODEs) {
                    String[] components = extractVariableNames(ODE);
                    String physicalClassName = components[0], odeVariableName = components[1];
                    double odeVariableLowerBound = result[index * 2];
                    double odeVariableUpperBound = result[(index * 2) + 1];

                    PhysicalState physicalState = (PhysicalState) updatedPhysicalHybridState.getActorState(physicalClassName);
                    physicalState.updateVariable(new IntervalRealVariable(odeVariableName, odeVariableLowerBound, odeVariableUpperBound));
                    index++;
                }
            }

            for (Map.Entry<String, PhysicalState> physicalStateEntry : updatedPhysicalHybridState.getPhysicalStates().entrySet()) {
                Map<String, HybridState> shallowCopyCurrentStates = new HashMap<>(updatedPhysicalHybridStates);
                for (Map.Entry<String, HybridState> hybridStateEntry : shallowCopyCurrentStates.entrySet()) {

                    PhysicalState physicalState = hybridStateEntry.getValue().getPhysicalStates().get(physicalStateEntry.getKey());

                    ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(physicalState.getVariablesValuation());
                    String physicalDeclarationName = RebecInstantiationMapping.getInstance().getRebecReactiveClassType(physicalState.getActorName());
                    DiscreteBoolVariable guardSatisfiedResult = (DiscreteBoolVariable) evaluatorVisitor.visit(
                            (BinaryExpression) Objects.requireNonNull(CompilerUtil.getGuardCondition(physicalDeclarationName, physicalState.getMode())));
                    DiscreteBoolVariable invariantSatisfiedResult = (DiscreteBoolVariable) evaluatorVisitor.visit(
                            (BinaryExpression) Objects.requireNonNull(CompilerUtil.getInvariantCondition(physicalDeclarationName, physicalState.getMode())));
                    PhysicalClassDeclaration physicalClassDeclaration = CompilerUtil.getPhysicalClassDeclaration(physicalDeclarationName);

                    if (invariantSatisfiedResult.getDefinite()) {
                        if (invariantSatisfiedResult.getValue()) {
                            // CHECKME: Should Do sth else?
                        } else {
                            if (guardSatisfiedResult.getDefinite()) {
                                if (guardSatisfiedResult.getValue()) {
                                    List<Statement> guardStatements =
                                            Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName, physicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
                                    physicalState.addStatements(guardStatements);
                                } else {
                                    throw new RuntimeException("Time lock happened");
                                }
                            } else {
//                                HybridState hybridStateUnsatisfiedGuard = cloner.deepClone(hybridStateEntry.getValue());
                                List<Statement> guardStatements =
                                        Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName, physicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
                                physicalState.addStatements(guardStatements);
//                                updatedPhysicalHybridStates.put(hybridStateUnsatisfiedGuard.updateHash(), hybridStateUnsatisfiedGuard);
                            }
                        }
                    } else {
                        //Invariant = True
                        //Invariant = False
                        if (guardSatisfiedResult.getDefinite()) {
                            if (guardSatisfiedResult.getValue()) {
                                List<Statement> guardStatements =
                                        Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName, physicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
                                physicalState.addStatements(guardStatements);
                            } else {
                                throw new RuntimeException("Time lock happened");
                            }
                        } else {
//                                HybridState hybridStateUnsatisfiedGuard = cloner.deepClone(hybridStateEntry.getValue());
                            List<Statement> guardStatements =
                                    Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName, physicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
                            physicalState.addStatements(guardStatements);
//                                updatedPhysicalHybridStates.put(hybridStateUnsatisfiedGuard.updateHash(), hybridStateUnsatisfiedGuard);
                        }

                    }
                }
            }
            // CHECKME: when we should call NonTimeProgressExecutor
//            NonTimeProgressSOSExecutor nonTimeProgressSOSExecutor = new NonTimeProgressSOSExecutor();
            List<HybridState> generatedHybridStates = nonTimeProgressSOSExecutor.generateNextStates(updatedPhysicalHybridState);

            queue.addAll(generatedHybridStates);
            for (HybridState hybridState : generatedHybridStates)
                reachabilityAnalysisGraph.addNode(rootNode, hybridState);

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
            } else {
//                softwareStates.add(createSoftwareState(reactiveClassDeclaration, mainRebecDefinition));
                SoftwareState softwareState = createSoftwareState(reactiveClassDeclaration, mainRebecDefinition);
                softwareStates.put(softwareState.getActorName(), softwareState);
            }
        }
        return new HybridState(new ContinuousVariable("globalTime"), softwareStates, physicalStates, new CANNetworkState());
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
            String type = ((OrdinaryPrimitiveType) stateVar.getType()).getName();
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
            String type = ((OrdinaryPrimitiveType) stateVar.getType()).getName();
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
