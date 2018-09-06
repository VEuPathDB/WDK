package org.gusdb.wdk.model.report;

import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.gusdb.fgputil.iterator.ReadOnlyIterator;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.report.AbstractTabularReporter.RowsProvider;

public class AttributesRowProvider implements RowsProvider {

  private final RecordInstance _record;
  private final Set<AttributeField> _attributes;

  public AttributesRowProvider(RecordInstance record, Set<AttributeField> attributes) {
    _record = record;
    _attributes = attributes;
  }

  @Override
  public Iterator<List<Object>> iterator() {
    return new AttributeRowIterator(_record, _attributes);
  }

  private static class AttributeRowIterator extends ReadOnlyIterator<List<Object>> {

    private final RecordInstance _record;
    private final Set<AttributeField> _attributes;
    private boolean _recordFetched = false;

    public AttributeRowIterator(RecordInstance record, Set<AttributeField> attributes) {
      _record = record;
      _attributes = attributes;
    }
  
    @Override
    public boolean hasNext() {
      return !_recordFetched;
    }
  
    @Override
    public List<Object> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      _recordFetched = true;
      return mapToList(_attributes, fSwallow(field -> {
        AttributeValue value = _record.getAttributeValue(field.getName());
        return (value == null ? "N/A" : value.getValue());
      }));
    }
  }
}
