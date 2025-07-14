package converters.modelConverters;

import converters.templates.FlowstarDataModel;
import dataStructure.ContinuousVariable;
import freemarker.template.*;

public class FlowstarModelAdapter extends WrappingTemplateModel implements AdapterTemplateModel, TemplateHashModel {

    private final FlowstarDataModel flowstarDataModel;

    public FlowstarModelAdapter(FlowstarDataModel flowstarDataModel, ObjectWrapper objectWrapper) {
        super(objectWrapper);
        this.flowstarDataModel = flowstarDataModel;
    }

    @Override
    public Object getAdaptedObject(Class<?> hint) {
        return flowstarDataModel;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        switch (key) {
            case "vars":
                return new VarsAdapter(flowstarDataModel.getVars(), getObjectWrapper());
            case "settingBlock":
                return wrap(flowstarDataModel.getSettingBlock());
            case "modeDefinition":
                return new ModeDefinitionAdapter(flowstarDataModel.getModeDefinition(), getObjectWrapper());
            case "jumpDefinition":
                return new JumpDefinitionAdapter(flowstarDataModel.getJumpDefinition(), getObjectWrapper());
            case "outputFileName":
                return wrap(flowstarDataModel.getOutputFileName());
            default:
                throw new TemplateModelException();
        }
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }

    public static class VarsAdapter extends WrappingTemplateModel implements AdapterTemplateModel, TemplateSequenceModel {

        private ContinuousVariable[] vars;

        public VarsAdapter(ContinuousVariable[] vars, ObjectWrapper objectWrapper) {
            super(objectWrapper);
            this.vars = vars;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return vars;
        }

        @Override
        public TemplateModel get(int index) throws TemplateModelException {
            if (vars.length > 0) {
                return wrap(vars[index]);
            } else {
                throw new TemplateModelException();
            }
        }

        @Override
        public int size() throws TemplateModelException {
            return vars.length;
        }
    }

    public static class ModeDefinitionAdapter extends WrappingTemplateModel implements AdapterTemplateModel, TemplateHashModel {

        private FlowstarDataModel.ModeDefinition modeDefinition;

        public ModeDefinitionAdapter(FlowstarDataModel.ModeDefinition modeDefinition, ObjectWrapper objectWrapper) {
            super(objectWrapper);
            this.modeDefinition = modeDefinition;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return modeDefinition;
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            switch (key) {
                case "invariantCondition":
                    return wrap(modeDefinition.getInvariantCondition());
                case "ODEAssignments":
                    return new VariableAssignmentAdapter(modeDefinition.getODEAssignments(), getObjectWrapper());
                default:
                    throw new TemplateModelException();
            }
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return false;
        }
    }

    public static class JumpDefinitionAdapter extends WrappingTemplateModel implements AdapterTemplateModel, TemplateHashModel {

        private FlowstarDataModel.JumpDefinition jumpDefinition;

        public JumpDefinitionAdapter(FlowstarDataModel.JumpDefinition jumpDefinition, ObjectWrapper objectWrapper) {
            super(objectWrapper);
            this.jumpDefinition = jumpDefinition;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return jumpDefinition;
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            switch (key) {
                case "guardCondition":
                    return wrap(jumpDefinition.getGuardCondition());
                case "resetAssignments":
                    return new VariableAssignmentAdapter(jumpDefinition.getResetAssignments(), getObjectWrapper());
                default:
                    throw new TemplateModelException();
            }
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return false;
        }
    }

    public static class VariableAssignmentAdapter
            extends WrappingTemplateModel implements AdapterTemplateModel, TemplateSequenceModel {

        private FlowstarDataModel.AssignmentExp[] assignmentExps;

        public VariableAssignmentAdapter(
                FlowstarDataModel.AssignmentExp[] assignmentExps,
                ObjectWrapper objectWrapper
        ) {
            super(objectWrapper);
            this.assignmentExps = assignmentExps;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return assignmentExps;
        }

        @Override
        public TemplateModel get(int index) throws TemplateModelException {
            if (assignmentExps.length > 0) {
                return wrap(assignmentExps[index]);
            } else {
                throw new TemplateModelException();
            }
        }

        @Override
        public int size() throws TemplateModelException {
            return assignmentExps.length;
        }
    }
}
