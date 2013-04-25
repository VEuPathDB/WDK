/**
 * 
 */
package org.gusdb.wdk.model.record;


/**
 * The scope of a {@link Field}, which will affect the availability of the
 * {@link Field} in certain functions on the website.
 * 
 * @author xingao
 */
public enum FieldScope {
  /**
   * A field is available in all places.
   */
  ALL(false, false), /**
   * A field is available in the summary page, but not in
   * download report.
   */
  NON_INTERNAL(true, false), /**
   * A field is available in download report, but not
   * in summary page.
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
}
