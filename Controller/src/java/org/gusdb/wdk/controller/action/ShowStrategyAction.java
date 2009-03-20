package org.gusdb.wdk.controller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This Action handles loading a search strategy from the database. It loads the
 * strategy and forwards to a simple jsp for diplaying the strategy
 */

public class ShowStrategyAction extends ShowQuestionAction {
    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowStrategyAction...");

        try {
        // Make sure a protocol is specified
        String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
        String strBranchId = null;

        if (strStratId == null || strStratId.length() == 0) {
            throw new WdkModelException(
                    "No strategy was specified for loading!");
        }

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        if (strStratId.indexOf("_") > 0) {
            strBranchId = strStratId.split("_")[1];
            strStratId = strStratId.split("_")[0];
        }

        StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

        ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();

        if (activeStrategies == null) {
            activeStrategies = new ArrayList<Integer>();
        }
        if (!activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
            activeStrategies.add(0, new Integer(strategy.getStrategyId()));
        }
        wdkUser.setActiveStrategies(activeStrategies);

        if (strBranchId == null) {
            request.setAttribute(CConstants.WDK_STEP_KEY,
                    strategy.getLatestStep());
        } else {
            request.setAttribute(CConstants.WDK_STEP_KEY,
                    strategy.getStepById(Integer.parseInt(strBranchId)));
        }
        request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

        String output = request.getParameter("output");
        if (output != null && output.equals("json")) {
            outputJSON(strategy, response);
            return null;
        }

        // forward to strategyPage.jsp
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?strategy=" + URLEncoder.encode(strStratId, "utf-8"));
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strBranchId, "utf-8"));
        }
        String viewStep = request.getParameter("step");
        if (viewStep != null && viewStep.length() != 0) {
            url.append("&step=" + URLEncoder.encode(viewStep, "utf-8"));
        }
        String subQuery = request.getParameter("subquery");
        if (subQuery != null && subQuery.length() != 0) {
            url.append("&subquery=" + URLEncoder.encode(subQuery, "utf-8"));
        }

        logger.debug("URL: " + url);

        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(false);
        return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    private void outputJSON(StrategyBean strategy, HttpServletResponse response)
            throws JSONException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, IOException {
        JSONObject jsStrategy = new JSONObject();
        jsStrategy.put("name", strategy.getName());
        jsStrategy.put("id", strategy.getStrategyId());
        jsStrategy.put("saved", strategy.getIsSaved());
        jsStrategy.put("savedName", strategy.getSavedName());
        jsStrategy.put("importId", strategy.getImportId());

        JSONArray jsSteps = new JSONArray();
        for (StepBean step : strategy.getLatestStep().getAllSteps()) {
            JSONObject jsStep = new JSONObject();
            jsStep.put("name", step.getCustomName());
            jsStep.put("customName", step.getCustomName());
            jsStep.put("id", step.getStepId());
            jsStep.put("answerId", step.getAnswerId());
            jsStep.put("isCollapsed", step.getIsCollapsible());
            jsStep.put("dataType", step.getDataType());
            jsStep.put("shortName", step.getShortDisplayName());
            jsStep.put("results", step.getResultSize());
            jsStep.put("questionName", step.getQuestionName());
            jsStep.put("displayName",
                    step.getAnswerValue().getQuestion().getDisplayName());
            jsStep.put("isboolean", step.getIsBoolean());
            jsStep.put("istransform", step.getIsTransform());
            jsStep.put("filtered", step.isFiltered());
            jsStep.put("filterName", step.getFilterDisplayName());
            jsStep.put("urlParams",
                    step.getAnswerValue().getQuestionUrlParams());

            JSONArray jsParams = new JSONArray();
            Map<String, ParamBean> params = step.getAnswerValue().getQuestion().getParamsMap();
            Map<String, String> paramValues = step.getParams();
            for (String paramName : step.getParams().keySet()) {
                ParamBean param = params.get(paramName);
                JSONObject jsParam = new JSONObject();
                jsParam.put("name", paramName);
                jsParam.put("prompt", param.getPrompt());
                jsParam.put("value", paramValues.get(paramName));
                jsParam.put("visible", param.getIsVisible());
                jsParam.put("className", param.getClass().getName());
                jsParams.put(jsParam);
            }
            jsStep.put("params", jsParams);
            jsSteps.put(jsStep);
        }
        jsStrategy.put("steps", jsSteps);

        PrintWriter writer = response.getWriter();
        writer.print(jsStrategy.toString());
    }
}
