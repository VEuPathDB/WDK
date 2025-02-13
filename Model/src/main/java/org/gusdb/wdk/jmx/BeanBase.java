package org.gusdb.wdk.jmx;

import javax.servlet.ServletContext;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

/**
 * Parent abstract class for most WDK-related MBeans. Provides
 * handles on WDK model objects and the applications ServletContext.
 */
public abstract class BeanBase {

  private WdkModel _wdkModel;
  private ServletContext _context;

  protected BeanBase() {
    _context = ContextThreadLocal.get();
    _wdkModel = (WdkModel)_context.getAttribute(Utilities.CONTEXT_KEY_WDK_MODEL_OBJECT);
  }

  public ServletContext getContext() {
    return _context;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }
}