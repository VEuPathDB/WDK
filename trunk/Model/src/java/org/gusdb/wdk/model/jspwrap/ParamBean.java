package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Param;


/**
 * A wrapper on the wdk model that provides simplified access for 
 * consumption by a view
 */ 
public class ParamBean {

    Param param;

    public ParamBean(Param param) {
	this.param = param;
    }

    String getName() {
	return param.getFullName();
    }

    String getPrompt() {
	return param.getPrompt();
    }

    String getHelp() {
	return param.getHelp();
    }
}
