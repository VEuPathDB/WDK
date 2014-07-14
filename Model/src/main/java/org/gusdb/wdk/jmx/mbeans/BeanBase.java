package org.gusdb.wdk.jmx.mbeans;

import javax.servlet.ServletContext;

import org.gusdb.wdk.jmx.ContextThreadLocal;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
  * Parent abstract class for most WDK-related MBeans. Provides
  * handles on WDK model objects and the applications ServletContext.
  */
public abstract class BeanBase {

  WdkModelBean wdkModelBean;
  WdkModel wdkModel;
  ServletContext context;
  // matches value of org.gusdb.wdk.controller.CConstants.WDK_MODEL_KEY .
  // Controller classes are not available until after Model is compiled;
  // so, not available for import here.
  public final static String WDK_MODEL_KEY = "wdkModel";
  
  public BeanBase() {
    context = ContextThreadLocal.get();
    wdkModelBean = (WdkModelBean)context.getAttribute(WDK_MODEL_KEY);
    wdkModel = wdkModelBean.getModel();
  }

  public ServletContext getContext() {
    return context;
  }
  
  public WdkModel getWdkModel() {
    return wdkModel;
  }
  
  public WdkModelBean getWdkModelBean() {
    return wdkModelBean;
  }
}