package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A wrapper on a {@link FlatVocabParam} that provides simplified access for 
 * consumption by a view
 */ 
public class FlatVocabParamBean extends ParamBean {


    public FlatVocabParamBean(FlatVocabParam param) {
	super(param);
    }

    public Boolean getMultiPick() {
	return ((FlatVocabParam)param).getMultiPick();
    }

    public String[] getVocab() {
	try {
	    return ((FlatVocabParam)param).getVocab();
	} catch (WdkModelException e) {
	    throw new RuntimeException(e);
	}
    }

    
}
