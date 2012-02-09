package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.StringParam;

public class StringParamBean extends ParamBean {

	public StringParamBean(StringParam param) {
		super(param);
	}
	
	public boolean getMultiLine() {
		return ((StringParam)param).getMultiLine();
	}

}
