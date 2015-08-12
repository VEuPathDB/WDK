package org.gusdb.wdk.model.filter;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class StepFilterDefinition extends FilterDefinition {

  private static final Logger LOG = Logger.getLogger(StepFilterDefinition.class);

  private Class<? extends StepFilter> _class;

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    try {
      String impl = getImplementation();
      LOG.debug("Checking filter '" + getName() + "' implementation class: " + impl);
      _class = Class.forName(impl).asSubclass(StepFilter.class);
    }
    catch (ClassNotFoundException | ClassCastException ex) {
      throw new WdkModelException(ex);
    }
  }

  public StepFilter getStepFilter() throws WdkModelException {
    try {
      StepFilter filter = _class.newInstance();
      initializeFilter(filter);
      return filter;
    }
    catch (InstantiationException | IllegalAccessException ex) {
      throw new WdkModelException(ex);
    }
  }
}
