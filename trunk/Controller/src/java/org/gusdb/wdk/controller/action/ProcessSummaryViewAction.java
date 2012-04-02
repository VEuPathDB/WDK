/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author Jerric
 * 
 */
public class ProcessSummaryViewAction extends Action {

    private static final String PARAM_STEP = "step";
    private static final String PARAM_VIEW = "view";
    private static final String PARAM_COMMAND = "command";
    private static final String PARAM_ATTRIBUTE = "attribute";
    private static final String PARAM_SORT_ORDER = "sortOrder";

    private static final String FORWARD_SHOW_SUMMARY_VIEW = "show-summary-view";

    private static Logger logger = Logger.getLogger(ProcessSummaryViewAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ProcessSummaryViewAction...");

        //WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        UserBean wdkUser = ActionUtility.getUser(servlet, request);

        String stepId = request.getParameter(PARAM_STEP);
        String view = request.getParameter(PARAM_VIEW);
        String command = request.getParameter(PARAM_COMMAND);
        String attributeName = request.getParameter(PARAM_ATTRIBUTE);
        String sortingOrder = request.getParameter(PARAM_SORT_ORDER);

        if (stepId == null || stepId.length() == 0)
            throw new WdkModelException("Step is required!");
        StepBean step = wdkUser.getStep(Integer.parseInt(stepId));
        // step.resetAnswerValue();

        QuestionBean question = step.getQuestion();
        String questionName = question.getFullName();

        // get the query string
        String queryString = request.getQueryString();
        logger.debug("url before process: " + queryString);

        // get summary checksum, if have
        //String summaryChecksum = null; // request.getParameter(CConstants.WDK_SUMMARY_KEY);
        // if (summaryChecksum != null && summaryChecksum.length() > 0) {
        // // apply the current summary to the question first, then do other
        // // command
        // wdkUser.applySummaryChecksum(questionName, summaryChecksum);
        // } else summaryChecksum = null;

        // get sorting checksum, if have
        String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
        if (sortingChecksum != null && sortingChecksum.length() > 0) {
            // apply the current sorting to the question first, then do other
            // command
            wdkUser.applySortingChecksum(questionName, sortingChecksum);
        } else sortingChecksum = null;

        // get command
        if (command != null) {
            if (command.equalsIgnoreCase("sort")) { // sorting
                boolean ascending = !sortingOrder.equalsIgnoreCase("DESC");
                String checksum = wdkUser.addSortingAttribute(questionName,
                        attributeName, ascending);
                // add/replace sorting key
                String sortingParam = CConstants.WDK_SORTING_KEY + "="
                        + checksum;
                queryString = queryString.replaceAll("\\b"
                        + CConstants.WDK_SORTING_KEY + "=[^&]*", "");
                queryString += "&" + sortingParam;
            } else if (command.equalsIgnoreCase("reset")) {
                wdkUser.resetSummaryAttribute(questionName);
                // remove summary key from query string
                queryString = queryString.replaceAll("&"
                        + CConstants.WDK_SUMMARY_KEY + "=[^&]*", "");
            } else {
                String[] summary = wdkUser.getSummaryAttributes(questionName);
                List<String> summaryList = new ArrayList<String>();
                String[] attributeNames = request.getParameterValues(CConstants.WDK_SUMMARY_ATTRIBUTE_KEY);
                if (attributeNames == null) attributeNames = new String[0];

                if (command.equalsIgnoreCase("update")) {
                    List<String> attributeNamesList = new ArrayList<String>(
                            Arrays.asList(attributeNames));
                    for (String summaryName : summary) {
                        if (attributeNamesList.contains(summaryName)) {
                            summaryList.add(summaryName);
                            attributeNamesList.remove(summaryName);
                        }
                    }
                    for (String attrName : attributeNamesList) {
                        summaryList.add(attrName);
                    }
                } else {
                    for (String attribute : summary) {
                        summaryList.add(attribute);
                    }

                    if (command.equalsIgnoreCase("add")) {
                        for (String attrName : attributeNames) {
                            if (!summaryList.contains(attrName))
                                summaryList.add(attrName);
                        }
                    } else if (command.equalsIgnoreCase("remove")) {
                        for (String attrName : attributeNames) {
                            summaryList.remove(attrName);
                        }
                    } else if (command.equalsIgnoreCase("arrange")) {
                        // Get the attribute that will be to the left of
                        // attributeName after attributeName is moved
                        String attributeToLeft = request.getParameter(CConstants.WDK_SUMMARY_ARRANGE_ORDER_KEY);
                        // If attributeToLeft is null (or not in list), make
                        // attributeName the first element.
                        // Otherwise, make it the first element AFTER
                        // attributeToLeft
                        for (String attrName : attributeNames) {
                            summaryList.remove(attrName);
                            int toIndex = summaryList.indexOf(attributeToLeft) + 1;
                            summaryList.add(toIndex, attrName);
                        }
                    } else {
                        throw new WdkModelException("Unknown command: "
                                + command);
                    }
                }

                summary = new String[summaryList.size()];
                summaryList.toArray(summary);
                String checksum = wdkUser.setSummaryAttributes(questionName,
                        summary);

                // add/replace summary key
                String summaryParam = CConstants.WDK_SUMMARY_KEY + "="
                        + checksum;
                queryString = queryString.replaceAll("\\b"
                        + CConstants.WDK_SUMMARY_KEY + "=[^&]*", "");
                queryString += "&" + summaryParam;
            }

            wdkUser.save();
        }

        // remove unneeded parameters from the url
        queryString = queryString.replaceAll("&"
                + CConstants.WDK_SUMMARY_COMMAND_KEY + "=[^&]*", "");
        queryString = queryString.replaceAll("&"
                + CConstants.WDK_SUMMARY_ATTRIBUTE_KEY + "=[^&]*", "");
        queryString = queryString.replaceAll("&"
                + CConstants.WDK_SUMMARY_ARRANGE_ORDER_KEY + "=[^&]*", "");
        queryString = queryString.replaceAll("&"
                + CConstants.WDK_SUMMARY_SORTING_ORDER_KEY + "=[^&]*", "");
        queryString = queryString.replaceAll("&" + CConstants.WDK_SUMMARY_KEY
                + "=[^&]*", "");

        String strBasket = request.getParameter("from_basket");
        boolean fromBasket = (strBasket != null && strBasket.equals("true"));
        logger.debug("to basket: " + fromBasket);

        StringBuffer url = new StringBuffer();
        if (!fromBasket) {
            // construct url to show summary action
            ActionForward showSummaryView = mapping.findForward(FORWARD_SHOW_SUMMARY_VIEW);
            logger.debug("forward: " + showSummaryView);
            url.append(showSummaryView.getPath());
            url.append("?step=").append(stepId);
            url.append("&view=").append(view);
        } else {
            ActionForward showBasket = mapping.findForward(CConstants.PQ_SHOW_BASKET_MAPKEY);
            url.append(showBasket.getPath());
            String rcName = question.getRecordClass().getFullName();
            url.append("?recordClass=" + rcName);
        }

        logger.debug("url after process: " + queryString);

        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(true);
        return forward;
    }
}
