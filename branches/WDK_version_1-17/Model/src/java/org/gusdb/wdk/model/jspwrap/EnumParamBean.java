package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.AbstractEnumParam;
import org.gusdb.wdk.model.EnumParam;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A wrapper on a {@link EnumParam} that provides simplified access for
 * consumption by a view
 */
public class EnumParamBean extends ParamBean {

    public EnumParamBean(AbstractEnumParam param) {
        super(param);
    }

    public Boolean getMultiPick() {
        return ((AbstractEnumParam) param).getMultiPick();
    }

    public String[] getVocabInternal() throws WdkModelException {
        return ((AbstractEnumParam) param).getVocabInternal();
    }

    public String[] getVocab() throws WdkModelException {
        return ((AbstractEnumParam) param).getVocab();
    }

    public Map<String, String> getVocabMap() throws WdkModelException {
        return ((AbstractEnumParam) param).getVocabMap();
    }

    public String[] getDisplays() throws WdkModelException {
        return ((AbstractEnumParam) param).getDisplays();
    }

    public Map<String, String> getDisplayMap() throws WdkModelException {
        return ((AbstractEnumParam) param).getDisplayMap();
    }
    
    public String getDisplayType() {
        return ((AbstractEnumParam) param).getDisplayType();
    }
}
