package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.EnumParam;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A wrapper on a {@link EnumParam} that provides simplified access for
 * consumption by a view
 */
public class EnumParamBean extends ParamBean {

    public EnumParamBean(EnumParam param) {
        super(param);
    }

    public Boolean getMultiPick() {
        return ((EnumParam) param).getMultiPick();
    }

    public String[] getVocabInternal() throws WdkModelException {
        return ((EnumParam) param).getVocabInternal();
    }

    public String[] getVocab() throws WdkModelException {
        return ((EnumParam) param).getVocab();
    }

    public Map<String, String> getVocabMap() throws WdkModelException {
        return ((EnumParam) param).getVocabMap();
    }

    public String[] getDisplay() {
        return ((EnumParam) param).getDisplay();
    }

    public Map<String, String> getTermDisplayMap() {
        return ((EnumParam) param).getTermDisplayMap();
    }
}
