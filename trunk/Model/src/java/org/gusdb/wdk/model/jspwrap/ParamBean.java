package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Param;


/**
 * A wrapper on a {@link Param} that provides simplified access for 
 * consumption by a view
 */ 
public class ParamBean {

    Param param;

    public ParamBean(Param param) {
	this.param = param;
    }

    public String getName() {
	return param.getName();
    }

    public String getFullName() {
	return param.getFullName();
    }

    public String getPrompt() {
	return param.getPrompt();
    }

    public String getHelp() {
	return param.getHelp();
    }

    public String getDefault() {
	return param.getDefault();
    }
}
