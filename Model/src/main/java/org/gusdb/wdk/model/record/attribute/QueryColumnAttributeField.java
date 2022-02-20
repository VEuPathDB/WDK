package org.gusdb.wdk.model.record.attribute;

import java.util.Map;

import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.columntool.ColumnToolElementPair;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;

/**
 * This is an {@link AttributeField} that maps an underlying {@link Column} from an attribute or table
 * {@link Query}.
 *
 * @author jerric
 */
public class QueryColumnAttributeField extends ColumnAttributeField {

  private Column _column;

  @Override
  public ColumnAttributeField clone() {
    return (ColumnAttributeField) super.clone();
  }

  /**
   * @return Returns the column.
   */
  public Column getColumn() {
    return _column;
  }

  /**
   * @param column
   *          The column to set.
   */
  @RngUndefined
  public void setColumn(Column column) {
    _column = column;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // verify the name
    if (!_name.equals(_column.getName())) {
      throw new WdkModelException("The name of the ColumnAttributeField '" +
        _name + "' does not match the column name '" + _column.getName() + "'");
    }
  }

  @Override
  public Map<String, ColumnToolElementPair> getColumnToolElementPairs() {
    return _columnToolElementPairs;
  }

}
