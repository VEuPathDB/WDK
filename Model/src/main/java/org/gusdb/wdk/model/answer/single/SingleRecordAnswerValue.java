package org.gusdb.wdk.model.answer.single;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.ResultSizeFactory;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.User;

public class SingleRecordAnswerValue extends AnswerValue {

  // define our own result size factory that always returns 1
  private static class SingleRecordResultSizeFactory extends ResultSizeFactory {
    public SingleRecordResultSizeFactory(AnswerValue answerValue) {
      super(answerValue);
    }
    @Override public int getResultSize() { return 1; }
    @Override public int getDisplayResultSize() { return 1; }
  }

  private RecordClass _recordClass;
  private Map<String, Object> _pkMap;

  public SingleRecordAnswerValue(User user, RecordClass recordClass,
      Question question, Map<String, Object> pkMap) {
    super(user, question, null, 1, 1, Collections.EMPTY_MAP, null);
    _recordClass = recordClass;
    _pkMap = pkMap;
    _resultSizeFactory = new SingleRecordResultSizeFactory(this);
  }

  public Map<String, Object> getPrimaryKeyValueMap() {
    return _pkMap;
  }

  @Override
  public RecordInstance[] getRecordInstances() throws WdkModelException, WdkUserException {
    return new RecordInstance[]{ new DynamicRecordInstance(_user, _recordClass, _pkMap) };
  }

  @Override
  public AnswerValue cloneWithNewPaging(int startIndex, int endIndex) {
    // paging is irrelevant since there's only one record
    return this;
  }

  @Override
  protected String getIdSql(String excludeFilter, boolean excludeViewFilters) throws WdkModelException, WdkUserException {
    DBPlatform platform = _recordClass.getWdkModel().getAppDb().getPlatform();
    return new StringBuilder("( select ")
      .append(FormatUtil.join(Functions.mapToList(_pkMap.entrySet(),
        new Function<Entry<String,Object>, String>() {
          @Override
          public String apply(Entry<String, Object> pkColumnValue) {
            Object valueObj = pkColumnValue.getValue();
            String value = (valueObj instanceof Number ? valueObj.toString() :
              "'" + DBPlatform.normalizeString(valueObj.toString()) + "'");
            return value + " as " + pkColumnValue.getKey();
          }
        }
      ).toArray(), ", "))
      .append(platform.getDummyTable())
      .append(" )")
      .toString();
  }

  @Override
  protected String getNoFiltersIdSql() throws WdkModelException, WdkUserException {
    // no filters can be applied to single-record questions/answers
    return getIdSql();
  }

  @Override
  public String getChecksum() throws WdkModelException, WdkUserException {
    return EncryptionUtil.encrypt(new StringBuilder("SingleRecordAnswer_")
      .append(_recordClass.getFullName()).append("_")
      .append(FormatUtil.prettyPrint(_pkMap)).toString());
  }
  
  @Override
  public List<String[]> getAllIds() throws WdkModelException, WdkUserException {
    String[] pkArray = new String[_pkMap.size()];
    String[] pkColNames = _recordClass.getPrimaryKeyDefinition().getColumnRefs();
    if (pkArray.length != pkColNames.length)
      throw new WdkModelException("Incoming primary key array does not match recordclass PK column ref array");
    for (int i = 0; i < pkColNames.length; i++) {
      pkArray[i] = (String)_pkMap.get(pkColNames[i]);
    }
    return new ListBuilder<String[]>().add(pkArray).toList();
  }

  @Override
  public Map<String, String> getParamDisplays() {
    return new MapBuilder<String,String>()
        .put(SingleRecordQuestion.PRIMARY_KEY_PARAM_NAME,
            FormatUtil.join(_pkMap.values().toArray(), ","))
        .toMap();
  }
}
