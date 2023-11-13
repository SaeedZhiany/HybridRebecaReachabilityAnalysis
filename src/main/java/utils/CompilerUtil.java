package utils;

import dataStructure.ConnectionType;
import org.rebecalang.compiler.modelcompiler.RebecaCompiler;
import org.rebecalang.compiler.modelcompiler.SymbolTable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.HybridRebecaCode;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.ModeDeclaration;
import org.rebecalang.compiler.modelcompiler.hybridrebeca.objectmodel.PhysicalClassDeclaration;
import org.rebecalang.compiler.utils.CompilerFeature;
import org.rebecalang.compiler.utils.ExceptionContainer;
import org.rebecalang.compiler.utils.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static dataStructure.ConnectionType.WIRE;

public class CompilerUtil {

    @Nonnull
    private static Pair<RebecaModel, SymbolTable> pair;

    static {
        pair = new Pair<>();
    }

    private CompilerUtil() {
    }

    public static void compile(@Nonnull String fileName) throws Exception {
        RebecaCompiler compiler = new RebecaCompiler();
        pair = compiler.compileRebecaFile(
                Paths.get(Constants.DIRECTORY_SAMPLES, fileName).toFile(),
                new HashSet<>(Arrays.asList(CompilerFeature.CORE_2_3, CompilerFeature.HYBRID_REBECA))
        );

        final ExceptionContainer exceptionContainer = compiler.getExceptionContainer();
        if (!exceptionContainer.exceptionsIsEmpty()) {
            System.err.println(exceptionContainer.getExceptions());
            pair = new Pair<>();
            throw new Exception("Compiler Exception.");
        }

        if (!exceptionContainer.warningsIsEmpty()) {
            System.out.println(Constants.Green + exceptionContainer.getWarnings() + Constants.DefaultColor);
        }
    }

    @Nonnull
    public static RebecaModel getRebecaModel() {
        return pair.getFirst();
    }

    @Nonnull
    public static HybridRebecaCode getHybridRebecaCode() {
        return (HybridRebecaCode) getRebecaModel().getRebecaCode();
    }

    @Nonnull
    public static SymbolTable getSymbolTable() {
        return pair.getSecond();
    }

    public static ConnectionType getConnectionType(@Nonnull String srcRebecName, @Nonnull String dstRebecName) {
        final HybridRebecaCode hybridRebecaCode = getHybridRebecaCode();

        for (MainRebecDefinition mainRebecDefinition : hybridRebecaCode.getMainDeclaration().getMainRebecDefinition()) {
            if (mainRebecDefinition.getName().equals(srcRebecName)) {
                for (Expression binding : mainRebecDefinition.getBindings()) {
                    if (((TermPrimary) binding).getName().equals(dstRebecName)) {
                        for (Annotation annotation : binding.getAnnotations()) {
                            ConnectionType connectionType = ConnectionType.byValue(annotation.getIdentifier());
                            if (connectionType != null) {
                                return connectionType;
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }

        // if anything is not specified, we will assume that the connection is WIRE between the two rebecs.
        return WIRE;
    }

    @Nullable
    public static Expression getInvariantCondition(@Nonnull String actorName, @Nullable String modeName) {
        final ModeDeclaration modeDeclaration = getModeDeclaration(actorName, modeName);

        return modeDeclaration != null ? modeDeclaration.getInvariantDeclaration().getCondition() : null;
    }

    @Nullable
    public static Expression getGuardCondition(@Nonnull String actorName, @Nullable String modeName) {
        final ModeDeclaration modeDeclaration = getModeDeclaration(actorName, modeName);

        return modeDeclaration != null ? modeDeclaration.getGuardDeclaration().getCondition() : null;
    }

    @Nullable
    public static PhysicalClassDeclaration getPhysicalClassDeclaration(@Nonnull String actorName) {
        for (PhysicalClassDeclaration physicalClassDeclaration : getHybridRebecaCode().getPhysicalClassDeclaration()) {
            if (physicalClassDeclaration.getName().equals(actorName)) {
                return physicalClassDeclaration;
            }
        }
        return null;
    }

    @Nullable
    public static ModeDeclaration getModeDeclaration(@Nonnull String actorName, @Nullable String modeName) {
        if (modeName != null) {
            final PhysicalClassDeclaration physicalClassDeclaration = getPhysicalClassDeclaration(actorName);
            if (physicalClassDeclaration != null) {
                for (ModeDeclaration modeDeclaration : physicalClassDeclaration.getModeDeclarations()) {
                    if (modeDeclaration.getName().equals(modeName)) {
                        return modeDeclaration;
                    }
                }
            }
        }
        return null;
    }

    @Nonnull
    public static List<String> getContinuousVariables(@Nonnull String actorName) {
        final PhysicalClassDeclaration physicalClassDeclaration = getPhysicalClassDeclaration(actorName);
        final List<String> continuesVariables = new ArrayList<>();
        if (physicalClassDeclaration != null) {
            for (FieldDeclaration stateVar : physicalClassDeclaration.getStatevars()) {
                List<Annotation> annotations = stateVar.getAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation.getIdentifier().equals("Real")) {
                            for (VariableDeclarator variableDeclarator : stateVar.getVariableDeclarators()) {
                                continuesVariables.add(variableDeclarator.getVariableName());
                            }
                            break;
                        }
                    }
                }
            }
        }
        return continuesVariables;
    }

    // FIXME: this doesn't work but we need something like this
    @Nonnull
    public static List<Statement> getMessageBody(@Nonnull String actorName, @Nonnull String messageName) {
        final PhysicalClassDeclaration physicalClassDeclaration = getPhysicalClassDeclaration(actorName);
        final List<Statement> messageBody = new ArrayList<>();
        if (physicalClassDeclaration != null) {
            for (MethodDeclaration methodDeclaration : physicalClassDeclaration.getMethodDeclarations()) {
                if (methodDeclaration.getName().equals(messageName)) {
                    messageBody.addAll(methodDeclaration.getStatements());
                    break;
                }
            }
        }
        return messageBody;
    }

}
