package org.gusdb.wdk.model.columntool;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class ColumnToolBundle extends WdkModelBase implements NamedObject  {

  private String _name;
  private List<ColumnTool> _tools = new ArrayList<>();

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  public void addTool(ColumnTool tool) {
    _tools.add(tool);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    for (ColumnTool tool : _tools) {
      tool.resolveReferences(wdkModel);
    }
  }

  public List<ColumnTool> getTools() {
    return _tools;
  }
}
