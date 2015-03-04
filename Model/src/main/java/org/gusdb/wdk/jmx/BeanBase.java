package org.gusdb.wdk.jmx;

import javax.servlet.ServletContext;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * Parent abstract class for most WDK-related MBeans. Provides
 * handles on WDK model objects and the applications ServletContext.
 */
public abstract class BeanBase {

  private WdkModelBean _wdkModelBean;
  private ServletContext _context;

  // matches value of org.gusdb.wdk.controller.CConstants.WDK_MODEL_KEY .
  // Controller classes are not available until after Model is compiled;
  // so, not available for import here.
  public final static String WDK_MODEL_KEY = "wdkModel";

  protected BeanBase() {
    _context = ContextThreadLocal.get();
    _wdkModelBean = (WdkModelBean)_context.getAttribute(WDK_MODEL_KEY);
  }

  public ServletContext getContext() {
    return _context;
  }

  public WdkModel getWdkModel() {
    return _wdkModelBean.getModel();
  }

  public WdkModelBean getWdkModelBean() {
    return _wdkModelBean;
  }
}