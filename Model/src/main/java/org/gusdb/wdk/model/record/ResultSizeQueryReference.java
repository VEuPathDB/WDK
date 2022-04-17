package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModelException;

/**
 * <p>
 * a representation of a @{code &lt;recordClass>/&lt;resultSizeQueryReference>} that is a specification for a ResultSizeSqlPlugin.  
 * Has a reference to a {@link org.gusdb.wdk.model.query.Query}
 * </p>
 *
 * @author Steve
 */
public class ResultSizeQueryReference extends Reference {

  private String recordDisplayName;
  private String recordDisplayNamePlural;
  private String recordShortDisplayName;
  private String recordShortDisplayNamePlural;

  public ResultSizeQueryReference() {}

  /**
   * @param twoPartName
   */
  public ResultSizeQueryReference(String twoPartName) throws WdkModelException {
    super(twoPartName);
  }

  public void setRecordDisplayName(String displayName) {
	  recordDisplayName = displayName;
  }

  public void setRecordDisplayNamePlural(String displayNamePlural) {
	  recordDisplayNamePlural = displayNamePlural;
  }
  
  public void setRecordShortDisplayName(String shortDisplayName) {
	  recordShortDisplayName = shortDisplayName;
  }

  public void setRecordShortDisplayNamePlural(String shortDisplayNamePlural) {
	  recordDisplayNamePlural = shortDisplayNamePlural;
  }		
 
  public String getRecordDisplayName() { return recordDisplayName; }
  public String getRecordDisplayNamePlural() { return recordDisplayNamePlural == null? recordDisplayName + "s" : recordDisplayNamePlural; }
  public String getRecordShortDisplayName() { return recordShortDisplayName == null? recordDisplayName : recordShortDisplayName; }
  public String getRecordShortDisplayNamePlural() { return recordShortDisplayNamePlural == null? getRecordShortDisplayName() + "s" : recordShortDisplayNamePlural; }

}
