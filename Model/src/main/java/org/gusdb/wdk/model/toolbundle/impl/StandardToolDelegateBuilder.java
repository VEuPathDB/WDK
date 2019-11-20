package org.gusdb.wdk.model.toolbundle.impl;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.toolbundle.ColumnTool;
import org.gusdb.wdk.model.toolbundle.ColumnToolBuilder;
import org.gusdb.wdk.model.toolbundle.ColumnToolDelegate;
import org.gusdb.wdk.model.toolbundle.ColumnToolDelegate.ColumnToolDelegateBuilder;
import org.gusdb.wdk.model.toolbundle.ColumnToolInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Builder for the default column tool delegate.
 *
 * @param <T> type of the ColumnTool that the constructed tool delegate will
 *           handle.
 */
public class StandardToolDelegateBuilder<S extends ColumnToolInstance, T extends ColumnTool<S>>
implements ColumnToolDelegateBuilder<S,T> {

  private static final String ERR_CONFLICT = "More than one tool was defined " +
    "for the type(s): %s";

  /**
   * This is a collection instead of map(type -> handler) to
   * more easily allow reporting more than one error at a
   * time for multiple handlers assigned to the same type.
   */
  private Collection<ColumnToolBuilder<S,T>> builders;

  /**
   * Index of flags for whether or not a tool was set for
   * each AttributeFieldDataType value.  Effectively
   * map(enum -> boolean).q
   */
  private boolean[] typeFlags;

  public StandardToolDelegateBuilder() {
    builders  = new ArrayList<>();
    typeFlags = new boolean[AttributeFieldDataType.values().length];
  }

  @Override
  public ColumnToolDelegateBuilder<S,T> addTool(final ColumnToolBuilder<S,T> tool) {
    builders.add(tool);
    typeFlags[tool.getColumnType().ordinal()] = true;
    return this;
  }

  @Override
  public boolean hasToolFor(AttributeFieldDataType type) {
    return typeFlags[type.ordinal()];
  }

  @Override
  public ColumnToolDelegate<S,T> build(WdkModel wdk) throws WdkModelException {
    final Map<AttributeFieldDataType, T> out = new HashMap<>();

    for (final ColumnToolBuilder<S,T> builder : builders) {
      final var type = builder.getColumnType();

      if (out.containsKey(type))
        throw new WdkModelException(format(ERR_CONFLICT, type.name()));

      final T tmp = builder.build(wdk);

      // If tool was globally assigned it may not actually be compatible with
      // column type.  In this case, just exclude the tool to avoid confusion.
      if (tmp.isCompatibleWith(type)) {
        out.put(type, tmp);
      }
    }
    builders.clear();

    var del = new StandardToolDelegate<>(out);
    return del;
  }
}
