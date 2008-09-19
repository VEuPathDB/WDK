package org.gusdb.wdk.controller.action;

import java.io.File;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

import com.sun.org.apache.regexp.internal.recompile;

/**
 * This Action is called by the ActionServlet when a WDK record is requested. It
 * 1) reads param values from request, 2) makes record 3) forwards control to a
 * jsp page that displays a record
 */

public class ShowRecordAction extends Action {

    private static Logger logger = Logger.getLogger(ShowRecordAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long start = System.currentTimeMillis();

        ServletContext svltCtx = getServlet().getServletContext();
        WdkModelBean wdkModel = (WdkModelBean) svltCtx.getAttribute(CConstants.WDK_MODEL_KEY);
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);

        RecordClassBean wdkRecordClass = wdkModel.findRecordClass(request.getParameter("name"));
        String[] pkColumns = wdkRecordClass.getPrimaryKeyColumns();

        Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
        StringBuffer urlParams = new StringBuffer();
        for (String column : pkColumns) {
            String value = request.getParameter(column);
            // to be backward compatible with older urls
            
            // make project id optional 
            if (value == null && column.equals("project_id")) {
                value = request.getParameter("projectId");
                if (value == null) value = wdkModel.getProjectId();
            }
            
            // recognize old primary keys
            if (value == null) value = request.getParameter("primary_key");
            if (value == null) value = request.getParameter("primaryKey");
            if (value == null) value = request.getParameter("id");
            
            if (value == null)
                throw new WdkModelException("The required primary key value "
                        + column + " for recordClass "
                        + wdkRecordClass.getFullName() + " is missing.");
            pkValues.put(column, value);

            urlParams.append((urlParams.length() == 0) ? "?" : "&");
            urlParams.append(URLEncoder.encode(column, "UTF-8")).append("=");
            urlParams.append(URLEncoder.encode(value, "UTF-8"));
        }

        RecordBean wdkRecord = wdkRecordClass.makeRecord(pkValues);

        request.setAttribute(CConstants.WDK_RECORD_KEY, wdkRecord);

        String customViewFile1 = customViewDir + File.separator
                + wdkRecordClass.getFullName() + ".jsp";
        String customViewFile2 = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_RECORD_PAGE;
        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
            forward = new ActionForward(customViewFile1 + urlParams, false);
        } else if (ApplicationInitListener.resourceExists(customViewFile2,
                svltCtx)) {
            forward = new ActionForward(customViewFile2 + urlParams, false);
        } else {
            forward = mapping.findForward(CConstants.SHOW_RECORD_MAPKEY);
        }

        long end = System.currentTimeMillis();
        logger.info("showRecord took total: " + ((end - start) / 1000F)
                + " seconds.");

        return forward;
    }
}
