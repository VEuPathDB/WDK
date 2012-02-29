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
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;

/**
 * @author Jerric
 * 
 */
public class ProcessSummaryAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessSummaryAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // get user, or create one, if not exist
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        // get the query string
        String queryString = request.getQueryString();
        logger.debug("url before process: " + queryString);

        // get question
        String questionName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        if (questionName == null || questionName.length() == 0) {
            String stepId = request.getParameter(CConstants.WDK_STEP_ID_KEY);
            StepBean step = null;
            if (stepId != null && stepId.length() != 0) {
                step = wdkUser.getStep(Integer.parseInt(stepId));
                step.resetAnswerValue();
            } else {
                throw new WdkModelException(
                        "No step was specified for ProcessSummary!");
            }

            questionName = step.getQuestionName();
        }
        QuestionBean question = wdkModel.getQuestion(questionName);

        // get summary checksum, if have
        String summaryChecksum = null; // request.getParameter(CConstants.WDK_SUMMARY_KEY);
        if (summaryChecksum != null && summaryChecksum.length() > 0) {
            // apply the current summary to the question first, then do other
            // command
            wdkUser.applySummaryChecksum(questionName, summaryChecksum);
        } else summaryChecksum = null;

        // get sorting checksum, if have
        String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
        if (sortingChecksum != null && sortingChecksum.length() > 0) {
            // apply the current sorting to the question first, then do other
            // command
            wdkUser.applySortingChecksum(questionName, sortingChecksum);
        } else sortingChecksum = null;

        // get command
        String command = request.getParameter(CConstants.WDK_SUMMARY_COMMAND_KEY);
        if (command != null) {
            if (command.equalsIgnoreCase("sort")) { // sorting
                String attributeName = request.getParameter(CConstants.WDK_SUMMARY_ATTRIBUTE_KEY);
                String sortingOrder = request.getParameter(CConstants.WDK_SUMMARY_SORTING_ORDER_KEY);
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
                    System.out.println("Summary list: " + summaryList);
                    for (String summaryName : summary) {
                        if (attributeNamesList.contains(summaryName)) {
                            summaryList.add(summaryName);
                            attributeNamesList.remove(summaryName);
                        }
                    }
                    logger.debug("Old Summary list: " + summaryList);
                    for (String attributeName : attributeNamesList) {
                        summaryList.add(attributeName);
                    }
                } else {
                    for (String attribute : summary) {
                        summaryList.add(attribute);
                    }

                    if (command.equalsIgnoreCase("add")) {
                        for (String attributeName : attributeNames) {
                            if (!summaryList.contains(attributeName))
                                summaryList.add(attributeName);
                        }
                    } else if (command.equalsIgnoreCase("remove")) {
                        for (String attributeName : attributeNames) {
                            summaryList.remove(attributeName);
                        }
                    } else if (command.equalsIgnoreCase("arrange")) {
                        // Get the attribute that will be to the left of
                        // attributeName after attributeName is moved
                        String attributeToLeft = request.getParameter(CConstants.WDK_SUMMARY_ARRANGE_ORDER_KEY);
                        // If attributeToLeft is null (or not in list), make
                        // attributeName the first element.
                        // Otherwise, make it the first element AFTER
                        // attributeToLeft
                        for (String attributeName : attributeNames) {
                            summaryList.remove(attributeName);
                            int toIndex = summaryList.indexOf(attributeToLeft) + 1;
                            summaryList.add(toIndex, attributeName);
                        }
                    } else {
                        throw new WdkModelException("Unknown command: "
                                + command);
                    }
                }
                logger.debug("New Summary list: " + summaryList);

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
        queryString = queryString.replaceAll("&"
                + CConstants.WDK_SUMMARY_KEY + "=[^&]*", "");

        String strBasket = request.getParameter("from_basket");
        boolean fromBasket = (strBasket != null && strBasket.equals("true"));
        logger.debug("to basket: " + fromBasket);

        StringBuffer url = new StringBuffer();
        if (!fromBasket) {
            // construct url to show summary action
            ActionForward showSummary = mapping.findForward(CConstants.PQ_SHOW_SUMMARY_MAPKEY);
            url.append(showSummary.getPath());
            url.append("?");
            url.append(queryString);
            // don't skip to the record page
            url.append("&noskip=1&resultsOnly=true");
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
