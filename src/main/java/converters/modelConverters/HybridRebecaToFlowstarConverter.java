package converters.modelConverters;

import converters.templates.FlowstarDataModel;
import converters.translator.*;
import dataStructure.DiscreteDecimalVariable;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import utils.CompilerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;

import static utils.Constants.DIRECTORY_OUTPUTS;
import static utils.Constants.DIRECTORY_TEMPLATES;

public class HybridRebecaToFlowstarConverter {

    private static Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        try {
            cfg.setDirectoryForTemplateLoading(new File(DIRECTORY_TEMPLATES));
        } catch (IOException e) {
            e.printStackTrace();
        }
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setObjectWrapper(new DataModelWrapper(cfg.getIncompatibleImprovements()));

        ExpressionTranslatorContainer.registerTranslator(BinaryExpression.class, new BinaryExpressionTranslator());
        ExpressionTranslatorContainer.registerTranslator(PlusSubExpression.class, new PlusSubExpressionTranslator());
        ExpressionTranslatorContainer.registerTranslator(TermPrimary.class, new TermPrimaryExpressionTranslator());
        ExpressionTranslatorContainer.registerTranslator(TernaryExpression.class, new TermPrimaryExpressionTranslator());
        ExpressionTranslatorContainer.registerTranslator(UnaryExpression.class, new UnaryExpressionTranslator());
        ExpressionTranslatorContainer.registerTranslator(Literal.class, new LiteralTranslator());
    }

    private HybridRebecaToFlowstarConverter() {
        // nothing to do
    }

    public static String generateFlowstarModelOutputFile(FlowstarDataModel dataModel)
            throws IOException, TemplateException {
        Template template = cfg.getTemplate("HRebecaToFlowstar.ftl");
        final String outputPath = Paths.get(DIRECTORY_OUTPUTS, dataModel.getOutputFileName() + ".model").toString();
        try (Writer fileWriter = new FileWriter(new File(outputPath))) {
            template.process(dataModel, fileWriter);
        }
        return outputPath;
    }
//
//    /**
//     * @param state physical state that must be converts to flowstar model
//     * @return output file path
//     * @throws ExpressionTranslationException
//     * @throws IOException
//     * @throws TemplateException
//     */
//    public static String generateFlowstarModelOutputFile(PhysicalState state)
//            throws ExpressionTranslationException, IOException, TemplateException {
//
//        final ModeDeclaration modeDeclaration = CompilerUtil.getModeDeclaration(state.getActorName(), state.getMode());
//
//        final List<FlowstarDataModel.AssignmentExp> modeVariableAssignments = new ArrayList<>();
//        final List<FlowstarDataModel.AssignmentExp> jumpVariableAssignments = new ArrayList<>();
//        if (modeDeclaration != null) {
//            for (Statement statement : modeDeclaration.getInvariantDeclaration().getBlock().getStatements()) {
//                BinaryExpression binaryExpression = ((BinaryExpression) statement);
//                modeVariableAssignments.add(new FlowstarDataModel.AssignmentExp(
//                        ((TermPrimary) binaryExpression.getLeft()).getName(),
//                        ExpressionTranslatorContainer.translate(binaryExpression.getRight(), state.getDiscreteVariablesValuation())
//                ));
//            }
//
//            for (Statement statement : modeDeclaration.getGuardDeclaration().getBlock().getStatements()) {
//                if (statement instanceof BinaryExpression) {
//                    BinaryExpression binaryExpression = ((BinaryExpression) statement);
//                    jumpVariableAssignments.add(new FlowstarDataModel.AssignmentExp(
//                            ((TermPrimary) binaryExpression.getLeft()).getName(),
//                            ExpressionTranslatorContainer.translate(binaryExpression.getRight(), state.getDiscreteVariablesValuation())
//                    ));
//                }
//            }
//        }
//        final FlowstarDataModel.ModeDefinition modeDefinition = new FlowstarDataModel.ModeDefinition(
//                getEvaluatedInvariantCondition(state.getActorName(), state.getMode(), state.getDiscreteVariablesValuation()),
//                modeVariableAssignments
//        );
//        final FlowstarDataModel.JumpDefinition jumpDefinition = new FlowstarDataModel.JumpDefinition(
//                getEvaluatedGuardCondition(state.getActorName(), state.getMode(), state.getDiscreteVariablesValuation()),
//                jumpVariableAssignments
//        );
//        final FlowstarDataModel flowstarDataModel = new FlowstarDataModel(
//                new ArrayList<>(state.getContinuousVariableValuation().values()),
//                new FlowstarDataModel.SettingBlock( // TODO revise setting block
//                        100,
//                        0.5f,
//                        8,
//                        new String[]{"s", "d"},
//                        new Integer[]{1, 9},
//                        6,
//                        7,
//                        1
//                ),
//                modeDefinition,
//                jumpDefinition,
//                state.getActorName() + state.getMode()
//        );
//        return generateFlowstarModelOutputFile(flowstarDataModel);
//    }

    @Nullable
    public static String getEvaluatedInvariantCondition(
            @Nonnull String actorName,
            @Nullable String modeName,
            @Nonnull HashMap<String, DiscreteDecimalVariable> discreteVariables
    ) throws ExpressionTranslationException {
        final Expression invariantCondition = CompilerUtil.getInvariantCondition(actorName, modeName);

        return invariantCondition != null
                ? ExpressionTranslatorContainer.translate(invariantCondition, discreteVariables)
                : null;
    }

    @Nullable
    public static String getEvaluatedGuardCondition(
            @Nonnull String actorName,
            @Nullable String modeName,
            @Nonnull HashMap<String, DiscreteDecimalVariable> discreteVariables
    ) throws ExpressionTranslationException {
        final Expression guardCondition = CompilerUtil.getGuardCondition(actorName, modeName);

        return guardCondition != null
                ? ExpressionTranslatorContainer.translate(guardCondition, discreteVariables)
                : null;
    }
}
