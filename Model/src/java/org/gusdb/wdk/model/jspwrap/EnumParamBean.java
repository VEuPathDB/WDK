package org.gusdb.wdk.model.jspwrap;

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

    public String[] getDsplay() {
        return ((EnumParam) param).getDisplay();
    }

}
