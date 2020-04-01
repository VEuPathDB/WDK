package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;

/**
 * <p>
 * Defines the {@link Column}s that can be used to uniquely
 * identify a {@link RecordInstance}.
 * </p>
 * <p>
 * A primary key is a combination of one or more {@link Column}s; if the
 * columns are needed as separate attributes, corresponding
 * {@link PkColumnAttributeField}s can to be defined inside the {@link RecordClass}.
 * If they are not defined, they will be generated.
 * </p>
 * <p>
 * Due to the limitation of the basket/dataset tables, we can only support a
 * limited number of columns in a primary key. the number is defined
 * {@link Utilities#MAX_PK_COLUMN_COUNT}.
 * </p>
 *
 * @author jerric
 */
public class PrimaryKeyDefinition extends WdkModelBase {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(PrimaryKeyDefinition.class);

  private RecordClass _recordClass;

  private List<WdkModelText> _columnRefList = new ArrayList<>();
  private Set<String> _columnRefSet = new LinkedHashSet<>();

  public void setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public void addColumnRef(WdkModelText columnRef) {
    _columnRefList.add(columnRef);
  }

  public String[] getColumnRefs() {
    return _columnRefSet.toArray(new String[0]);
  }

  public boolean hasColumn(String columnName) {
    return _columnRefSet.contains(columnName);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude columnRefs
    for (WdkModelText columnRef : _columnRefList) {
      if (columnRef.include(projectId)) {
        columnRef.excludeResources(projectId);
        String columnName = columnRef.getText();

        if (_columnRefSet.contains(columnName)) {
          throw new WdkModelException("The columnRef " + columnRef
              + " is duplicated in primaryKetAttribute in " + "recordClass "
              + _recordClass.getFullName());
        }
        else {
          _columnRefSet.add(columnName);
        }
      }
    }
    _columnRefList = null;

    if (_columnRefSet.isEmpty())
      throw new WdkModelException("No primary key column defined in "
          + "recordClass " + _recordClass.getFullName());
    if (_columnRefSet.size() > Utilities.MAX_PK_COLUMN_COUNT)
      throw new WdkModelException("You can specify up to "
          + Utilities.MAX_PK_COLUMN_COUNT + " primary key "
          + "columns in recordClass " + _recordClass.getFullName());
  }

  public PrimaryKeyValue getPrimaryKeyFromResultList(ResultList resultList) throws WdkModelException {
    Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
    for (String column : getColumnRefs()) {
      pkValues.put(column, resultList.get(column));
    }
    return new PrimaryKeyValue(this, pkValues);
  }

  public String createJoinClause(String subQuery1Name, String subQuery2Name) {
    return Arrays.stream(getColumnRefs())
        .map(colName -> " " + subQuery1Name + "." + colName + " = " + subQuery2Name + "." + colName + " ")
        .collect(Collectors.joining(" AND "));
  }
}
