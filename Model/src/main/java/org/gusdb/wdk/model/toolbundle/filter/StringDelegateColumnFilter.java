package org.gusdb.wdk.model.toolbundle.filter;

import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.STRING;

/**
 * Delegate column filter for String data.
 * <p>
 * This filter will automatically delegate to either {@link
 * StringPatternColumnFilter} or {@link StringSetColumnFilter} based on the
 * input client configuration.
 */
public class StringDelegateColumnFilter extends DelegateFilter {

  public StringDelegateColumnFilter() {
    super(
      new StringPatternColumnFilter(),
      new StringSetColumnFilter()
    );
  }

  @Override
  public ColumnFilter copy() {
    return copyInto(new StringDelegateColumnFilter());
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == STRING;
  }
}
