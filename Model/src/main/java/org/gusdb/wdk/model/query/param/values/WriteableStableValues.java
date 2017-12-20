package org.gusdb.wdk.model.query.param.values;

import java.util.Map;

import org.gusdb.fgputil.collection.WriteableMap;
import org.gusdb.wdk.model.query.Query;

/**
 * Writeable extension of BasicStableValues
 * 
 * @author rdoherty
 */
public class WriteableStableValues extends AbstractStableValues implements WriteableMap<String,String> {

  public WriteableStableValues(Query query) {
    super(query);
  }

  public WriteableStableValues(StableValues stableValues) {
    super(stableValues);
  }

  public WriteableStableValues(Query query, Map<String, String> stableValues) {
    super(query, stableValues);
  }

  public WriteableStableValues(Query query, StableValues stableValues) {
    super(query, stableValues);
  }

  @Override
  public Map<String, String> getUnderlyingMap() {
    return _values;
  }

}
