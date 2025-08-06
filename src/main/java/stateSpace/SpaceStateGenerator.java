package stateSpace;

import dataStructure.*;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;
import sos.*;
import utils.CompilerUtil;
import utils.ReachabilityAnalysisGraph;
import visitors.BlockStatementExecutorVisitor;
import visitors.ExpressionEvaluatorVisitor;

import java.math.BigDecimal;
import java.util.*;

import static configs.MyClonerInstance.startTime;
import static stateSpace.HybridState.extractVariableNames;

public class SpaceStateGenerator {

    public SpaceStateGenerator() {

    }

    @FunctionalInterface
    public interface JoszefCaller {
        double[] call(String[] ODEs, double[] intervals, double[] reachParams);
    }

    public void analyzeReachability(JoszefCaller joszefCaller) {
        // must be tupple
        double endSimulation = 3;

        NonTimeProgressSOSExecutor nonTimeProgressSOSExecutor = new NonTimeProgressSOSExecutor();
        final HybridRebecaCode hybridRebecaCode = CompilerUtil.getHybridRebecaCode();
        HybridState initialState = makeInitialState();
        ReachabilityAnalysisGraph reachabilityAnalysisGraph = new ReachabilityAnalysisGraph(initialState);
        Queue<HybridState> queue = new LinkedList<>();
        List<HybridState> initialStatesAfterConstruction = executeConstructors(initialState);

        ReachabilityAnalysisGraph.TreeNode initialNode = reachabilityAnalysisGraph.findNodeInGraph(initialState);
        for (HybridState state : initialStatesAfterConstruction) {
            reachabilityAnalysisGraph.addNode(initialNode, state, "Constructor");
            queue.add(state);
        }

        Boolean isFirstRound = true;
        long stateCounter =0;
        while (!queue.isEmpty() && isReachedEndYet(queue, endSimulation)) { // should add time upper bound
            System.out.println("Queue size: " + queue.size());
            double currentEvent = 0.0;
            HybridState state = queue.poll();
            stateCounter ++;
            currentEvent = state.getGlobalTime().getLowerBound();
            state.updateHash(); // MAYBE REMOVAL RISKY

            ReachabilityAnalysisGraph.TreeNode rootNode = reachabilityAnalysisGraph.findNodeInGraph(state);
            if (isFirstRound)
                rootNode = reachabilityAnalysisGraph.findNodeInGraph(initialState);

            double timeStep = 0.3;
            double stepSize = 0.3;
            double[] nextEvents = state.getEvents(currentEvent, timeStep);
            ArrayList<Double> nextEventsList = new ArrayList<>(Arrays.stream(nextEvents).boxed().toList());
            if (nextEvents.length == 0 || currentEvent + timeStep < nextEvents[0]) {
                nextEventsList.add(currentEvent + timeStep);
            }
            nextEventsList.add(Collections.min(nextEventsList) - currentEvent + state.getGlobalTime().getUpperBound());
            Collections.sort(nextEventsList);
            double previousEvent = nextEventsList.get(0);
            if (isFirstRound) {
                previousEvent = 0;
            }
            for (Double nextEvent : nextEventsList) {
                if (Math.abs(previousEvent - nextEvent) > 0.0001) {
                    currentEvent = nextEvent;
                    break; // Exit the loop once the condition is met
                }
            }


            if (previousEvent >= endSimulation)
                continue;

            HybridState updatedPhysicalHybridState = new HybridState(state);
            if (isFirstRound) {
                updatedPhysicalHybridState.updateGlobalTime(0, currentEvent);
            } else {
                updatedPhysicalHybridState.updateGlobalTime(previousEvent, currentEvent);
            }
            Map<String, HybridState> updatedPhysicalHybridStates = new HashMap<>();
            updatedPhysicalHybridStates.put(updatedPhysicalHybridState.updateHash(), updatedPhysicalHybridState);

            for (Map.Entry<String, PhysicalState> physicalState : updatedPhysicalHybridState.getPhysicalStates().entrySet()) {
                if (isFirstRound) {
                    calculateActorODEs(joszefCaller, updatedPhysicalHybridState, physicalState, endSimulation, stepSize);
                }
                physicalState.getValue().computeODEBoundsForTimeRange(updatedPhysicalHybridState.getGlobalTime(), stepSize, endSimulation);
            }

            try {
                updatePhysicalStates(updatedPhysicalHybridState.getPhysicalStates(), updatedPhysicalHybridStates);
            } catch (Exception ex) {
                System.out.println("Deadlock happened");
                reachabilityAnalysisGraph.addNode(rootNode, updatedPhysicalHybridState, "PhysicalUpdate");
                continue;
            }
            if (updatedPhysicalHybridStates.size() > 1)
                System.out.println("sds");
            for (Map.Entry<String, HybridState> hybridStateEntry : updatedPhysicalHybridStates.entrySet()) {
                hybridStateEntry.getValue().updateHash(); // MAYBE REMOVAL
                reachabilityAnalysisGraph.addNode(rootNode, hybridStateEntry.getValue(), "PhysicalUpdate");
                List<HybridState> generatedHybridStates = nonTimeProgressSOSExecutor.generateNextStates(hybridStateEntry.getValue(), false);
                queue.addAll(generatedHybridStates);
                ReachabilityAnalysisGraph.TreeNode rootTempNode = reachabilityAnalysisGraph.findNodeInGraph(hybridStateEntry.getValue());
                for (HybridState hybridState : generatedHybridStates) {
                    if (!hybridStateEntry.getValue().getHash().equals(hybridState.getHash())) {
                        HashMap<String, PhysicalState> newPhysicalStates = hybridState.getPhysicalStates();
                        HashMap<String, PhysicalState> oldPhysicalStates = hybridStateEntry.getValue().getPhysicalStates();
                        for (Map.Entry<String, PhysicalState> physicalState : newPhysicalStates.entrySet()) {
                            if (!physicalState.getValue().getMode().equals(oldPhysicalStates.get(physicalState.getKey()).getMode()) ||
                                    physicalState.getValue().isGuardExecuted()) {
                                physicalState.getValue().setGuardExecuted(false);
                                calculateActorODEs(joszefCaller, hybridState, physicalState, endSimulation, stepSize);
                            }
                        }
                        reachabilityAnalysisGraph.addNode(rootTempNode, hybridState, " NonTimeProgressExecute");
                    }
                }
            }
            isFirstRound = false;
        }
        long endTime = System.nanoTime();
        System.out.println("Execution time: " + ((endTime - startTime)/ 1_000_000) + " ms");
        System.out.println("Total Stated: " + stateCounter);
        String graph = reachabilityAnalysisGraph.toDot();
    }

    protected List<HybridState> executeConstructors(HybridState initialState) {
        NonTimeProgressSOSExecutor nonTimeProgressSOSExecutor = new NonTimeProgressSOSExecutor();
        return  nonTimeProgressSOSExecutor.generateNextStates(initialState, false);
    }

    private static String getStringOfVariableSimple(Variable variable) {
        if (variable instanceof DiscreteDecimalVariable decimalVariable)
            return String.valueOf(decimalVariable.getValue());
        if (variable instanceof DiscreteBoolVariable boolVariable)
            return boolVariable.getValue() ? "1" : "0";
        if (variable instanceof IntervalRealVariable realVariable)
            return String.valueOf(realVariable.getLowerBound());
        if (variable instanceof ContinuousVariable continuousVariable)
            return String.valueOf(continuousVariable.getLowerBound());
        if (variable instanceof StringVariable stringVariable)
            return stringVariable.getValue();
        return "";
    }

    private static void calculateActorODEs(JoszefCaller joszefCaller, HybridState hybridState,
                                           Map.Entry<String, PhysicalState> physicalState, double endSimulation, double stepSize) {

        if (physicalState.getValue().getMode() ==null || physicalState.getValue().getMode().equals("none"))
            return;

        double[] actorReachParams = new double[]{10.0, 0.99, stepSize, 7.0, endSimulation - hybridState.getGlobalTime().getLowerBound()};
        Map<String, Expression> actorODEs = RebecInstantiationMapping.getInstance().getActorODEs(physicalState.getKey(), physicalState.getValue().getMode());
        ExpressionEvaluatorVisitor expressionEvaluatorVisitor = new ExpressionEvaluatorVisitor(physicalState.getValue().getVariablesValuation());

        for (Map.Entry<String, Expression> actorODE : actorODEs.entrySet()) {
            String realODE = actorODE.getKey() + "=" + getStringOfVariableSimple(expressionEvaluatorVisitor.visit(actorODE.getValue()));
            double[] actorIntervals = hybridState.getIntervals(new String[]{realODE});
            double[] actorResult = joszefCaller.call(new String[]{realODE}, actorIntervals, actorReachParams);
            String[] components = extractVariableNames(realODE);
            String odeVariableName = components[1];
            List<Double> actorResultList = new ArrayList<>(Arrays.stream(actorResult).boxed().toList());

            physicalState.getValue().addODEResult(odeVariableName, actorResultList);
        }
    }

    private Boolean isReachedEndYet(Queue<HybridState> queue, double endSimulation) {
        for (HybridState hybridState : queue) {
            if (hybridState.getGlobalTime().getLowerBound() < endSimulation) // CHECKME: Don't need to remove states which endSimulation < UpperBound as it is BFS
                return true;
        }
        return false;
    }

    private static void updatePhysicalStates(HashMap<String, PhysicalState> physicalStates, Map<String, HybridState> updatedPhysicalHybridStates) {
        for (Map.Entry<String, PhysicalState> physicalStateEntry : physicalStates.entrySet()) {
            Map<String, HybridState> shallowCopyCurrentStates = new HashMap<>(updatedPhysicalHybridStates);
            for (Map.Entry<String, HybridState> hybridStateEntry : shallowCopyCurrentStates.entrySet()) {

                PhysicalState physicalState = hybridStateEntry.getValue().getPhysicalStates().get(physicalStateEntry.getKey());

                ExpressionEvaluatorVisitor evaluatorVisitor = new ExpressionEvaluatorVisitor(physicalState.getVariablesValuation());
                String physicalDeclarationName = RebecInstantiationMapping.getInstance().getRebecReactiveClassType(physicalState.getActorName());
                DiscreteBoolVariable guardSatisfiedResult = (DiscreteBoolVariable) evaluatorVisitor.visit(
                        (BinaryExpression) Objects.requireNonNull(CompilerUtil.getGuardCondition(physicalDeclarationName, physicalState.getMode())));
                DiscreteBoolVariable invariantSatisfiedResult = (DiscreteBoolVariable) evaluatorVisitor.visit(
                        (BinaryExpression) Objects.requireNonNull(CompilerUtil.getInvariantCondition(physicalDeclarationName, physicalState.getMode())));

                if (invariantSatisfiedResult.getDefinite()) {
                    if (invariantSatisfiedResult.getValue()) {
                        // CHECKME
                        checkGuardIfInvariantIsTrue(updatedPhysicalHybridStates, physicalStateEntry, hybridStateEntry,
                                guardSatisfiedResult, physicalDeclarationName);
                    } else {
                        if (updatedPhysicalHybridStates.size() <= 1) {
                            throw new RuntimeException("Deadlock happened");
                        } else {
                            updatedPhysicalHybridStates.remove(hybridStateEntry.getKey());
                        }
                    }
                } else {
                    checkGuardIfInvariantIsTrue(updatedPhysicalHybridStates, physicalStateEntry, hybridStateEntry,
                            guardSatisfiedResult, physicalDeclarationName);
                }
//            }
            }
        }
    }

    private static void checkGuardIfInvariantIsFalse(DiscreteBoolVariable guardSatisfiedResult,
                                                     String physicalDeclarationName, PhysicalState physicalState) {
        if (guardSatisfiedResult.getDefinite()) {
            if (guardSatisfiedResult.getValue()) {
                List<Statement> guardStatements =
                        Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName
                                , physicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
                physicalState.addStatements(guardStatements);
            } else {
                throw new RuntimeException("Time lock happened");
            }
        } else {
            List<Statement> guardStatements =
                    Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName,
                            physicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
            physicalState.addStatements(guardStatements);
        }
    }

    private static void checkGuardIfInvariantIsTrue(Map<String, HybridState> updatedPhysicalHybridStates,
                                                    Map.Entry<String, PhysicalState> physicalStateEntry,
                                                    Map.Entry<String, HybridState> hybridStateEntry,
                                                    DiscreteBoolVariable guardSatisfiedResult,
                                                    String physicalDeclarationName) {
        if ((guardSatisfiedResult.getDefinite() && guardSatisfiedResult.getValue()) || !guardSatisfiedResult.getDefinite()) {
            HybridState newHybridState = new HybridState(hybridStateEntry.getValue());
            PhysicalState newPhysicalState = newHybridState.getPhysicalStates().get(physicalStateEntry.getKey());
            newPhysicalState.setGuardExecuted(true);
            newPhysicalState.setLastTimeModeChangedLowerBound(newHybridState.getGlobalTime().getLowerBound());
            List<Statement> guardStatements =
                    Objects.requireNonNull(CompilerUtil.getModeDeclaration(physicalDeclarationName,
                            newPhysicalState.getMode())).getGuardDeclaration().getBlock().getStatements();
            newPhysicalState.addStatements(guardStatements);
            updatedPhysicalHybridStates.put(newHybridState.updateHash(), newHybridState);
        }
    }

    protected HybridState makeInitialState() {
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
                PhysicalState physicalState = createPhysicalState(physicalClassDeclaration, mainRebecDefinition);
                physicalStates.put(physicalState.getActorName(), physicalState);
            } else {
                SoftwareState softwareState = createSoftwareState(reactiveClassDeclaration, mainRebecDefinition);
                softwareStates.put(softwareState.getActorName(), softwareState);
            }
        }
        return new HybridState(new ContinuousVariable("globalTime"), softwareStates, physicalStates, new CANNetworkState());
    }

    private boolean checkVariableSameTypes(String type1, String type2) {
        switch (type1) {
            case "int":
            case "byte":
            case "short": {
                return type2.equals("int") || type2.equals("byte") || type2.equals("short");
            }
            case "float":
            case "double": {
                return type2.equals("float") || type2.equals("double");

            }
            case "boolean": {
                return type2.equals("boolean");
            }
        }
        return false;
    }

    protected SoftwareState createSoftwareState(ReactiveClassDeclaration reactiveClassDeclaration, MainRebecDefinition mainRebecDefinition) {
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
        HashMap<String, Variable> variableValuation = new HashMap<>(variableValuationInitial);
//        BlockStatementExecutorVisitor blockStatementExecutorVisitor = new BlockStatementExecutorVisitor(variableValuationInitial);
//        blockStatementExecutorVisitor.visit(constructorDeclaration.getBlock());
//        HashMap<String, Variable> variableValuation = new HashMap<>();
//        for (FieldDeclaration stateVar : stateVars) {
//            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
//                variableValuation.put(variableDeclarator.getVariableName(), variableValuationInitial.get(variableDeclarator.getVariableName()));
//            }
//        }
        // TODO:
        return new SoftwareState(mainRebecDefinition.getName(), variableValuation, new HashSet<>(),
                constructorDeclaration.getBlock().getStatements(), 0, new ContinuousVariable("resumeTime"));
    }

    protected PhysicalState createPhysicalState(PhysicalClassDeclaration physicalClassDeclaration, MainRebecDefinition mainRebecDefinition) {
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
        HashMap<String, Variable> variableValuation = new HashMap<>(variableValuationInitial);

//        BlockStatementExecutorVisitor blockStatementExecutorVisitor = new BlockStatementExecutorVisitor(variableValuationInitial, "none");
//        blockStatementExecutorVisitor.visit(constructorDeclaration.getBlock());
//        HashMap<String, Variable> variableValuation = new HashMap<>();
//        for (FieldDeclaration stateVar : stateVars) {
//            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
//                variableValuation.put(variableDeclarator.getVariableName(), variableValuationInitial.get(variableDeclarator.getVariableName()));
//            }
//        }
//        variableValuation = variableValuationInitial;
        return new PhysicalState(mainRebecDefinition.getName(), "init", variableValuation, new HashSet<>(),
                constructorDeclaration.getBlock().getStatements(), 0);
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
                if (!checkVariableSameTypes(formalParameterType.getName(), actualParameterType.getName())) {
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
