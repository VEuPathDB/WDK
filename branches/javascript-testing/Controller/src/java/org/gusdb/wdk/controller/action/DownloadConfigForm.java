package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;
import org.gusdb.wdk.controller.CConstants;

/**
 *  form bean for download
 */

public class DownloadConfigForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = -5135214285985578539L;
    private String[] selectedFields = new String[] { CConstants.ALL };
    private String includeHeader = "yes";

    public void setSelectedFields(String[] selectedFields) {
	//System.err.println("DEBUG: DownloadConfigForm: setting selectedFields with num elememnts: " + selectedFields.length);
	this.selectedFields = selectedFields;
    }

    public String[] getSelectedFields() {
	return this.selectedFields;
    }

    public void setIncludeHeader(String incHeader) {
	includeHeader = incHeader;
    }

    public String getIncludeHeader() {
	return this.includeHeader;
    }
}
