package org.gusdb.wdk.model.answer;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.single.SingleRecordQuestionParam;
import org.gusdb.wdk.model.dbms.ArrayResultList;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.ProcessQueryInstance;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;

public class TableFieldProcessQueryResult {

  public static ArrayResultList getResultList(User user, TableField tableField, Map<String, Object> _pkMap) throws WdkModelException {

    // create param map
    Map<String,String> params = Map.of(

        // add primary key param
        SingleRecordQuestionParam.PRIMARY_KEY_PARAM_NAME,
        new JSONArray(_pkMap.values()).toString(),

        // add table name param
        TableField.TABLE_NAME_PARAM_NAME,
        tableField.getName()
    );

    // create process query instance to fetch results
    ProcessQueryInstance queryInstance = (ProcessQueryInstance)ProcessQuery.makeQueryInstance(
      QueryInstanceSpec.builder().putAll(params)
        .buildRunnable(user, tableField.getProcessQuery(), StepContainer.emptyContainer()));

    return queryInstance.getUncachedResults();
  }

}