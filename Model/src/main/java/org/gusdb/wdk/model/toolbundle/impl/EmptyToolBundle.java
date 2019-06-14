package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.toolbundle.ColumnToolBundle;
import org.gusdb.wdk.model.toolbundle.ColumnToolSet;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Empty implementation of the ColumnToolBundle.
 *
 * Used as a fallback when no other tool bundle has been
 * defined.
 */
public class EmptyToolBundle implements ColumnToolBundle {
  @Override
  public String getName() {
    return null;
  }

  @Override
  public Optional<ColumnToolSet> getTool(String name) {
    return Optional.empty();
  }

  @Override
  public Map<String, ColumnToolSet> getTools() {
    return Collections.emptyMap();
  }
}
