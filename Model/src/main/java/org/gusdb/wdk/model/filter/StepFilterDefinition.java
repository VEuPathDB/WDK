package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class StepFilterDefinition extends FilterDefinition {

  private Class<? extends StepFilter> _class;

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    try {
	String name = getImplementation();
	if (name == null) throw new WdkModelException("null implementation for filter '" + getName() + "'");
      _class = Class.forName(name).asSubclass(StepFilter.class);
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
