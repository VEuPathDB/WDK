package org.gusdb.wdk.model.record;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * The scope of a {@link Field}, which will affect the availability of the
 * {@link Field} in certain functions on the website.  This is a convenient
 * abstraction over the inReportMaker and internal XML attributes of certain
 * model objects.
 * 
 * @author xingao
 */
public enum FieldScope {
  /**
   * All fields (no exclusions)
   */
  ALL(false, false),
  /**
   * Excludes fields where internal=true
   */
  NON_INTERNAL(true, false),
  /**
   * Excludes fields where inReportMaker=false
   */
  REPORT_MAKER(false, true);

  private boolean _excludeInternal;
  private boolean _excludeNonReportMaker;

  private FieldScope(boolean excludeInternal, boolean excludeNonReportMaker) {
    _excludeInternal = excludeInternal;
    _excludeNonReportMaker = excludeNonReportMaker;
  }

  public boolean isFieldInScope(ScopedField field) {
    if (_excludeInternal && field.isInternal()) {
      return false;
    }
    if (_excludeNonReportMaker && !field.isInReportMaker()) {
      return false;
    }
    return true;
  }

  public <T extends ScopedField> Map<String, T> filter(Map<String, T> fields) {
    return fields.entrySet().stream()
      .filter(entry -> isFieldInScope(entry.getValue()))
      .collect(Collectors.toMap(
          Entry::getKey,
          Entry::getValue,
          (val1, val2) -> val2,
          LinkedHashMap::new));
  }
}
