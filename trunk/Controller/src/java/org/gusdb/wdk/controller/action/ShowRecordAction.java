package org.gusdb.wdk.controller.action;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

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
import org.gusdb.wdk.model.jspwrap.AttributeFieldBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a WDK record is requested. It
 * 1) reads param values from request, 2) makes record 3) forwards control to a
 * jsp page that displays a record
 */

public class ShowRecordAction extends Action {

    private static final String ATTR_PAGE_ID = "wdkPageId";

    private static Logger logger = Logger.getLogger(ShowRecordAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowRecordAction...");
        long start = System.currentTimeMillis();

        ServletContext svltCtx = getServlet().getServletContext();
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        UserBean user = ActionUtility.getUser(servlet, request);
        String customViewDir = CConstants.WDK_CUSTOM_VIEW_DIR + File.separator
                + CConstants.WDK_PAGES_DIR;

        RecordClassBean wdkRecordClass = wdkModel.findRecordClass(request.getParameter("name"));
        String[] pkColumns = wdkRecordClass.getPrimaryKeyColumns();

        Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
        StringBuffer urlParams = new StringBuffer();
        for (String column : pkColumns) {
            String value = request.getParameter(column);
            // to be backward compatible with older urls

            if (column.equalsIgnoreCase("project_id")) {
                // make project id optional
                if (value == null || value.trim().length() == 0) {
                    value = request.getParameter("projectId");
                    if (value == null || value.trim().length() == 0)
                        value = wdkModel.getProjectId();
                }
            } else {
                // recognize old primary keys
                if (value == null) value = request.getParameter("primary_key");
                if (value == null) value = request.getParameter("primaryKey");
                if (value == null) value = request.getParameter("id");

                if (value == null)
                    throw new WdkModelException("The required primary key "
                            + "value " + column + " for recordClass "
                            + wdkRecordClass.getFullName() + " is missing.");
            }
            pkValues.put(column, value);

            urlParams.append((urlParams.length() == 0) ? "?" : "&");
            urlParams.append(URLEncoder.encode(column, "UTF-8")).append("=");
            urlParams.append(URLEncoder.encode(value, "UTF-8"));
        }

        RecordBean wdkRecord = new RecordBean(user, wdkRecordClass, pkValues);

        // try getting some attributes
        Map<String, AttributeFieldBean> attributes = wdkRecordClass.getAttributeFields();
        // record will always have primary key field as the first attribute
        if (attributes.size() > 1) {
            Iterator<String> names = attributes.keySet().iterator();
            names.next();
            String fieldName = names.next();
            try {
                wdkRecord.getAttributes().get(fieldName);
            }
            catch (WdkModelException ex) {
                // most likely user provided an invalid record id, the record
                // has been marked as invalid, and ex can be ignored.
                logger.info(ex);
                ex.printStackTrace();
            }
        }

        request.setAttribute(CConstants.WDK_RECORD_KEY, wdkRecord);

        String frontAction = user.getFrontAction();
        if (frontAction != null) {
            request.setAttribute("action", frontAction);
        }
        user.resetFrontAction();

        String defaultViewFile = customViewDir + File.separator
                + CConstants.WDK_RECORD_PAGE;

        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_RECORDS_DIR + File.separator
                + wdkRecordClass.getFullName() + ".jsp";

        ActionForward forward = null;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            forward = new ActionForward(customViewFile + urlParams, false);
        } else {
            forward = new ActionForward(defaultViewFile);
        }

        long end = System.currentTimeMillis();
        logger.info("showRecord took total: " + ((end - start) / 1000F)
                + " seconds.");

        // generate a page id
        Random random = new Random();
        int pageId = random.nextInt(Integer.MAX_VALUE);
        request.setAttribute(ATTR_PAGE_ID, pageId);
        logger.info("wdk-record-page-id=" + pageId + " --- start page loading.");

        return forward;
    }
}
