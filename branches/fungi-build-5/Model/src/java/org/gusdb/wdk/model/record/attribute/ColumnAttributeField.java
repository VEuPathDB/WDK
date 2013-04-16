package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Column;

/**
 * This is an {@link AttributeField} that maps an underlying {@link Column} from
 * an attribute or table {@link Query}.
 * 
 * @author jerric
 * 
 */
public class ColumnAttributeField extends AttributeField {

  private Column column;

  public ColumnAttributeField() {
    super();
    // initialize the optional field values
  }

  /**
   * @return Returns the column.
   */
  public Column getColumn() {
    return this.column;
  }

  /**
   * @param column
   *          The column to set.
   * @throws WdkModelException
   */
  public void setColumn(Column column) {
    this.column = column;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.Field#presolveReferences(org.gusdb.wdk.model.WdkModel )
   */
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // verify the name
    if (!name.equals(column.getName()))
      throw new WdkModelException("The name of the ColumnAttributeField" + " '"
          + name + "' does not match the column name '" + column.getName()
          + "'");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeField#getDependents()
   */
  @Override
  protected Collection<AttributeField> getDependents() {
    return new ArrayList<AttributeField>();
  }

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() {
    Map<String, ColumnAttributeField> fields = new LinkedHashMap<String, ColumnAttributeField>();
    fields.put(name, this);
    return fields;
  }
}
