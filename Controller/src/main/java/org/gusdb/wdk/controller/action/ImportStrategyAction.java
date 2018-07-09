package org.gusdb.wdk.controller.action;

import static org.gusdb.fgputil.functional.Functions.findFirstIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;

public class ImportStrategyAction extends Action {

    private static final Logger logger = Logger.getLogger(ImportStrategyAction.class);

    private static final String SELECTED_TAB = "selectedTab";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ImportStrategyAction...");

        // Change to importing by answer checksum
        String strategyKey = request.getParameter("strategy");

        if (strategyKey == null || strategyKey.length() == 0) {
            strategyKey = request.getParameter("s"); // try a shorter version
        }
        if (strategyKey == null || strategyKey.length() == 0) {
            throw new WdkUserException(
                    "No strategy key was specified for importing!");
        }

        // load model, user, referenced strategy
        WdkModel wdkModel = ActionUtility.getWdkModel(getServlet()).getModel();
        UserBean wdkUser = ActionUtility.getUser(request);
        Strategy oldStrategy = StepUtilities.getStrategyByStrategyKey(wdkModel, strategyKey);

        // import strategy to the current user
        StrategyBean newStrategy = new StrategyBean(wdkUser,
            StepUtilities.importStrategy(wdkUser.getUser(), oldStrategy, new HashMap<>()));

        // run any associated analyses on the root step of the new strategy; user will probably want them run just as before
        StepAnalysisFactory saFactory = wdkModel.getStepAnalysisFactory();
        Collection<StepAnalysisInstance> rootStepAnalyses =  saFactory.getAppliedAnalyses(newStrategy.getStrategy().getLatestStep()).values();
        for (StepAnalysisInstance saContext : rootStepAnalyses) {
          saFactory.runAnalysis(saContext);
        }

        // Set new strategy to active and add any substrategies to the active strategies
        wdkUser.getUser().getSession().addActiveStrategy(Long.toString(newStrategy.getStrategyId()));
        addActiveSubstrategies(wdkUser, newStrategy.getStrategyId(), newStrategy.getLatestStep());

        // determine which result tab is preferred (if any)
        String selectedTab = chooseSelectedTab(oldStrategy, newStrategy.getStrategy(), request.getParameter(SELECTED_TAB));
        String tabParam = selectedTab == null ? "" : "?" + SELECTED_TAB + "=" + selectedTab;

        /*
         * Charles Treatman 4/23/09 Add code here to set the
         * current_application_tab cookie so that user will go to the Run
         * Strategies tab after importing a strategy
         */
        ShowApplicationAction.setWdkTabStateCookie(request, response);
        ShowApplicationAction.setStrategyPanelVisibilityCookie(response, true);

        ActionForward forward = mapping.findForward(CConstants.SHOW_APPLICATION_MAPKEY);
        forward = new ActionForward(forward.getPath() + tabParam, true);
        return forward;
    }

    private String chooseSelectedTab(Strategy oldStrategy, Strategy newStrategy, String selectedTabParam) throws WdkModelException {
      if (selectedTabParam == null || selectedTabParam.isEmpty()) {
        return null;
      }
      WdkModel wdkModel = oldStrategy.getUser().getWdkModel();
      if (selectedTabParam.equals("first_analysis")) {
        Collection<StepAnalysisInstance> analyses =
            wdkModel.getStepAnalysisFactory().getAppliedAnalyses(newStrategy.getLatestStep()).values();
        return (analyses.isEmpty() ? null : "step-analysis-" + analyses.iterator().next().getAnalysisId());
      }
      else if (selectedTabParam.startsWith("step-analysis-")) {
        String analysisIdStr = selectedTabParam.substring("step-analysis-".length());
        if (FormatUtil.isInteger(analysisIdStr)) {
          long oldAnalysisId = Long.parseLong(analysisIdStr);
          List<StepAnalysisInstance> oldAnalyses = new ArrayList<>(
              wdkModel.getStepAnalysisFactory().getAppliedAnalyses(oldStrategy.getLatestStep()).values());
          int oldAnalysisIndex = findFirstIndex(oldAnalyses, analysis -> analysis.getAnalysisId() == oldAnalysisId);
          if (oldAnalysisIndex == -1) {
            // passed ID does not match an ID in the old strategy; do not convey preference
            return null;
          }
          List<StepAnalysisInstance> newAnalyses = new ArrayList<>(
              wdkModel.getStepAnalysisFactory().getAppliedAnalyses(newStrategy.getLatestStep()).values());
          return "step-analysis-" + newAnalyses.get(oldAnalysisIndex).getAnalysisId();
        }
        else {
          return null;
        }
      }
      else {
        // name probably refers to a summary view; any 'bad' names will be ignored on client
        return selectedTabParam;
      }
    }

    private void addActiveSubstrategies(UserBean wdkUser, long strategyId,
            StepBean step) throws Exception {
        if (step == null) return;

        if (step.getIsCollapsible() && step.getParentStep() != null) {
            logger.debug("open sub-strategy: " + strategyId + "_"
                    + step.getStepId());
            wdkUser.addActiveStrategy(strategyId + "_"
                    + Long.toString(step.getStepId()));
        }
        addActiveSubstrategies(wdkUser, strategyId, step.getPreviousStep());
        addActiveSubstrategies(wdkUser, strategyId, step.getChildStep());
    }
}
