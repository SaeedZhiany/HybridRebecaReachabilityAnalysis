package converters.modelConverters;

import converters.templates.FlowstarDataModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

public class DataModelWrapper extends DefaultObjectWrapper {

    public DataModelWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if (obj instanceof FlowstarDataModel) {
            return new FlowstarModelAdapter(((FlowstarDataModel) obj), this);
        }
        return super.handleUnknownType(obj);
    }
}
