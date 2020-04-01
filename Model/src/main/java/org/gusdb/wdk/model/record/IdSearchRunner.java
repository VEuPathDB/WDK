package org.gusdb.wdk.model.record;

import static java.util.Arrays.asList;
import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;

public class IdSearchRunner {

  public static List<Map<String, Object>> runSearch(Question idSearch, Map<String, Object> pkValues)
      throws WdkModelException, RecordNotFoundException {
    // a bit of a hack here so we don't have to put a dataset in the DB every time
    String rawSql = ((SqlQuery)idSearch.getQuery()).getSql(); // must be SqlQuery
    Param dsParam = idSearch.getQuery().getParams()[0]; // exactly one param
    DatabaseInstance appDb = idSearch.getWdkModel().getAppDb();
    String paramInternalValue = "select " + pkValues.entrySet().stream()
        .map(pk -> "'" + pk.getValue() + "' as " + pk.getKey())
        .collect(Collectors.joining(", ")) + " from " + appDb.getPlatform().getDummyTable();
    String sql = dsParam.replaceSql(rawSql, paramInternalValue);
    PrimaryKeyDefinition pkDef = idSearch.getRecordClass().getPrimaryKeyDefinition();
    List<Map<String,Object>> foundIds = new SQLRunner(appDb.getDataSource(), sql).executeQuery(rs -> {
      List<Map<String,Object>> ids = new ArrayList<>();
      String[] pkCols = pkDef.getColumnRefs();
      while (rs.next()) {
        ids.add(getMapFromKeys(asList(pkCols), fSwallow(pkCol -> rs.getString(pkCol))));
      }
      return ids;
    });
    if (foundIds.isEmpty()) {
      String pksString = new PrimaryKeyValue(pkDef, pkValues).getValuesAsString();
      throw new RecordNotFoundException("No record found with primary key " + pksString);
    }
    return foundIds;
  }

}
