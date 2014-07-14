package org.gusdb.wdk.model.user.analysis;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkResourceChecker;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins.ViewConfig;

public class StepAnalysisViewResolver {

  public static final String DEFAULT_PREFIX = "/wdk/jsp/analysis/";
  public static final String DEFAULT_SUFFIX = ".jsp";
  
  private final ViewConfig _viewConfig;
  
  public StepAnalysisViewResolver(ViewConfig viewConfig) {
    _viewConfig = viewConfig;
  }

  public String resolveFormView(WdkResourceChecker resourceChecker, StepAnalysisContext context) throws WdkModelException {
    // first try to resolve name with prefix/suffix
    return resolveView(resourceChecker, context, context.getStepAnalysis().getFormViewName(), "form");
  }

  public String resolveResultsView(WdkResourceChecker resourceChecker, StepAnalysisContext context) throws WdkModelException {
    // first try to resolve name with prefix/suffix
    return resolveView(resourceChecker, context, context.getStepAnalysis().getAnalysisViewName(), "analysis");
  }
  
  private String resolveView(WdkResourceChecker resourceChecker, StepAnalysisContext context,
      String viewName, String viewType) throws WdkModelException {

    String defaultFixedName = DEFAULT_PREFIX + viewName + DEFAULT_SUFFIX;
    
    String userFixedName =
        (_viewConfig.getPrefix() != null ? _viewConfig.getPrefix() : "") +
        viewName +
        (_viewConfig.getSuffix() != null ? _viewConfig.getSuffix() : "");
    
    String resolvedView =
        resourceChecker.wdkResourceExists(viewName) ? viewName :
        resourceChecker.wdkResourceExists(userFixedName) ? userFixedName :
        resourceChecker.wdkResourceExists(defaultFixedName) ? defaultFixedName :
        null;
    
    if (resolvedView == null) {
      throw new WdkModelException("StepAnalysis " + viewType + " view [" + viewName + "] configured " +
          "for step analysis plugin [" + context.getStepAnalysis().getName() + "] cannot be resolved.");
    }
    
    return resolvedView;
  }
}
