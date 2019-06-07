package org.gusdb.wdk.model.bundle.impl;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.bundle.ColumnToolBundle;
import org.gusdb.wdk.model.bundle.ColumnToolBundleBuilder;
import org.gusdb.wdk.model.bundle.ColumnToolSet;
import org.gusdb.wdk.model.bundle.ColumnToolSetBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;

public class StandardToolBundleBuilder implements ColumnToolBundleBuilder {

  private static final String ERR_DUPLICATE_TOOL = "Duplicate column tool with "
    + "name \"%s\" defined in bundle \"%s\"";

  private static final String ERR_WRAP = "Error while building column tool "
    + "bundle \"%s\"";

  private String name;

  private final Collection<ColumnToolSetBuilder> builders;

  public StandardToolBundleBuilder() {
    builders = new ArrayList<>();
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void addTool(final ColumnToolSetBuilder tool) {
    builders.add(tool);
  }

  @Override
  public ColumnToolBundle build(final WdkModel wdk) throws WdkModelException {
    final Map<String, ColumnToolSet> toolSets = new HashMap<>();

    try {
      for (final ColumnToolSetBuilder builder : builders) {
        final ColumnToolSet set = builder.build(wdk);
        final String setName = set.getName();

        if (toolSets.containsKey(setName))
          throw new WdkModelException(format(ERR_DUPLICATE_TOOL, setName, getName()));
        toolSets.put(setName, set);
      }
    } catch (Exception e) {
      throw new WdkModelException(format(ERR_WRAP, name), e);
    }

    return new StandardToolBundle(name, toolSets);
  }

  @Override
  public String toString() {
    return Jackson.createObjectNode()
      .set(
        getClass().getSimpleName(),
        Jackson.createObjectNode()
          .put("name", name)
          .putPOJO("builders", builders)
      )
      .toString();
  }
}
