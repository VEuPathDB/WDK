package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.EnumParam;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.WdkModelException;

import java.util.Map;

/**
 * A wrapper on a {@link FlatVocabParam} that provides simplified access for
 * consumption by a view
 */
public class FlatVocabParamBean extends ParamBean {

    public FlatVocabParamBean(FlatVocabParam param) {
        super(param);
    }

    public Boolean getMultiPick() {
        return ((FlatVocabParam) param).getMultiPick();
    }

    public String[] getVocab() throws WdkModelException {
        return ((FlatVocabParam) param).getVocab();
    }

    public Map<String, String> getVocabMap() throws WdkModelException {
        return ((FlatVocabParam) param).getVocabMap();
    }
    
    public String getDisplayType() {
        return ((FlatVocabParam) param).getDisplayType();
    }
}
