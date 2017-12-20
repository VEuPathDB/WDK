package org.gusdb.wdk.model.query.param.values;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.wdk.model.query.Query;

/**
 * A read-only collection of param stable values attached to the query they are meant to serve
 * 
 * @author rdoherty
 */
public abstract class AbstractStableValues implements StableValues {

  private final Query _query;
  protected final HashMap<String,String> _values;

  protected AbstractStableValues(Query query) {
    _query = query;
    _values = new HashMap<>();
  }

  protected AbstractStableValues(StableValues initialValues) {
    this(initialValues.getQuery(), initialValues);
  }

  protected AbstractStableValues(Query query, Map<String, String> initialValues) {
    this(query);
    for (Entry<String,String> entry : initialValues.entrySet()) {
      _values.put(entry.getKey(), entry.getValue());
    }
  }

  protected AbstractStableValues(Query query, StableValues initialValues) {
    this(query);
    for (Entry<String,String> entry : initialValues.entrySet()) {
      _values.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Query getQuery() {
    return _query;
  }

  @Override
  public String prettyPrint() {
    return FormatUtil.prettyPrint(_values, Style.SINGLE_LINE);
  }

  @Override
  public int size() {
    return _values.size();
  }

  @Override
  public boolean isEmpty() {
    return _values.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return _values.containsKey(key);
  }

  @Override
  public String get(Object key) {
    return _values.get(key);
  }

  @Override
  public Set<String> keySet() {
    return Collections.unmodifiableSet(_values.keySet());
  }

  @Override
  public Collection<String> values() {
    return Collections.unmodifiableCollection(_values.values());
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return Collections.unmodifiableSet(_values.entrySet());
  }

}
