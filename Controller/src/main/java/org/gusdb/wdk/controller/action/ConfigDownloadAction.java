package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.form.DownloadConfigForm;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;

/**
 * This Action is called by the ActionServlet when a download config request is
 * made. It 1) reads download attributes param 2a) set selected attributes for
 * download in the answer bean 2b) forwards control to a GetDownloadResult
 * action
 */

public class ConfigDownloadAction extends DownloadStepAnswerValueAction {

	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Map<String, Integer> downloadConfigMap = new LinkedHashMap<String, Integer>();
        DownloadConfigForm downloadConfigForm = (DownloadConfigForm) form;
        String[] selectedFields = downloadConfigForm.getSelectedFields();
        for (int i = 0; i < selectedFields.length; i++) {
            // System.err.println("DEBUG: ConfigDownloadAction: selected field: "
            // + selectedFields[i]);
            if (!CConstants.ALL.equals(selectedFields[i])) {
                downloadConfigMap.put(selectedFields[i], 1);
            }
        }

        AnswerValueBean wdkAnswerValue = getStep(request).getAnswerValue();
        wdkAnswerValue.setDownloadConfigMap(downloadConfigMap);

        ActionForward forward = mapping.findForward(CConstants.CONFIG_DOWNLOAD_MAPKEY);
        return forward;
    }
}
