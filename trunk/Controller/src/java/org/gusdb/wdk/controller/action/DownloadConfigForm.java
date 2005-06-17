package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;

/**
 *  form bean for download
 */

public class DownloadConfigForm extends ActionForm {

    private String[] selectedFields = null;

    public void setSelectedFields(String[] selectedFields) {
	System.err.println("DEBUG: DownloadConfigForm: setting selectedFields with num elememnts: " + selectedFields.length);
	this.selectedFields = selectedFields;
    }

    public String[] getSelectedFields() {
	return this.selectedFields;
    }


}
