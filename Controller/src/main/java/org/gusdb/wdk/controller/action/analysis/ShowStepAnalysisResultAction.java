package org.gusdb.wdk.controller.action.analysis;

import static org.gusdb.fgputil.FormatUtil.NL;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.AnalysisResult;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;

public class ShowStepAnalysisResultAction extends AbstractStepAnalysisIdAction {

  private static final Logger LOG = Logger.getLogger(ShowStepAnalysisResultAction.class);
  
  private static final String ERROR_REASON_TEXT =
      "A run of this analysis encountered an error before it could complete.";
  private static final String INTERRUPTED_REASON_TEXT =
      "A run of this analysis was interrupted before it could complete";
  private static final String OUTOFDATE_REASON_TEXT =
      "Your previous run of this analysis is out-of-date and results must be " +
      "regenerated.  Please confirm your parameters above and re-run.";
  private static final String EXPIRED_REASON_TEXT =
      "The last run of this analysis took too long to complete and was " +
      "cancelled.  If this problem persists, please contact us.";
  private static final String REVISED_REASON_TEXT =
		"Your previous analysis results are not available because the gene result " +
    " changed when you used the filter table " +
    " above or revised a search strategy step. Please confirm your analysis " +
    " parameters and re-run.";
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = getContextFromPassedId();
    String reason = null;

    if (LOG.isDebugEnabled()) { // check first so expensive ops below aren't unnecessarily done
      LOG.debug("Retrieving results for Step Analysis with" +
          " ID: " + context.getAnalysisId() +
          ", hash: " + context.createHash() +
          ", " + NL + context);
    }

    switch (context.getStatus()) {
      case CREATED:
      case INVALID:
        // analysis has not yet been run; return an empty result
        return new ActionResult().setStream(IoUtil.getStreamFromString(""));
      case COMPLETE:
        // NOTE: there is a race condition here if cache is cleared between
        //       getting status above and retrieving status below.
        AnalysisResult result = getAnalysisMgr().getAnalysisResult(context);
        String viewPath = getAnalysisMgr().getViewResolver().resolveResultsView(this, context);
        return new ActionResult()
            .setViewPath(viewPath)
            .setRequestAttribute("wdkModel", getWdkModel())
            .setRequestAttribute("analysisId", context.getAnalysisId())
            .setRequestAttribute("viewModel", result.getResultViewModel());
      case PENDING:
      case RUNNING:
        return new ActionResult()
            .setViewName("pending")
            // send 202 status so the client knows the result isn't ready
            .setHttpResponseStatus(202)
            .setRequestAttribute("analysisId", context.getAnalysisId());
      case ERROR:
        reason = ERROR_REASON_TEXT;
      case INTERRUPTED:
        reason = (reason == null ? INTERRUPTED_REASON_TEXT : reason);
      case OUT_OF_DATE:
        reason = (reason == null ? OUTOFDATE_REASON_TEXT : reason);
      case EXPIRED:
        reason = (reason == null ? EXPIRED_REASON_TEXT : reason);
      case STEP_REVISED:
        reason = (reason == null ? REVISED_REASON_TEXT : reason);
        return new ActionResult()
            .setViewName("incomplete")
            .setRequestAttribute("reason", reason);
      case UNKNOWN:
      default:
        throw new WdkModelException("Invalid status " + context.getStatus() +
            " found on context.  This should never happen.");
    }
  }
  
}
