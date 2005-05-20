package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;

/**
 *  form bean for download
 */

public class DownloadForm extends ActionForm {

    private String chooseFields = null;

    public void setChooseFields(String chooseFields) {
	this.chooseFields = chooseFields;
    }

    public String getChooseFields() {
	return this.chooseFields;
    }


}
