package org.gusdb.wdk.model.answer.single;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.ResultSizeFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.PrimaryKeyIterator;
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

  public SingleRecordAnswerValue(User user, RunnableObj<AnswerSpec> validSpec) throws WdkModelException {
    super(user, validSpec, 1, UNBOUNDED_END_PAGE_INDEX, Collections.EMPTY_MAP, false);
    SingleRecordQuestion question = (SingleRecordQuestion)validSpec.get().getQuestion();
    SingleRecordQuestionParam param = question.getParam();
    _recordClass = question.getRecordClass();
    _pkMap = param.parseParamValue(validSpec.get().getQueryInstanceSpec().get(param.getName()));
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
  public AnswerValue clone() {
    // paging/sorting is irrelevant since there's only one record
    return this;
  }

  @Override
  public AnswerValue cloneWithNewPaging(int startIndex, int endIndex) {
    // paging/sorting is irrelevant since there's only one record
    return this;
  }

  @Override
  protected String getIdSql(String excludeFilter) throws WdkModelException {
    DBPlatform platform = _recordClass.getWdkModel().getAppDb().getPlatform();
    return new StringBuilder("( select ")
      .append(join(mapToList(_pkMap.entrySet(), pkColumnValue -> {
        Object valueObj = pkColumnValue.getValue();
        String value = (valueObj instanceof Number ? valueObj.toString() :
          "'" + DBPlatform.normalizeString(valueObj.toString()) + "'");
        return value + " as " + pkColumnValue.getKey();
      }).toArray(), ", "))
      .append(", 10 as " + Utilities.COLUMN_WEIGHT)
      .append(platform.getDummyTable())
      .append(" )")
      .toString();
  }

  @Override
  protected String getNoFiltersIdSql() throws WdkModelException {
    // no filters can be applied to single-record questions/answers
    return getIdSql();
  }

  @Override
  public String getChecksum() throws WdkModelException {
    return EncryptionUtil.encrypt(new StringBuilder("SingleRecordAnswer_")
      .append(_recordClass.getFullName()).append("_")
      .append(FormatUtil.prettyPrint(_pkMap)).toString());
  }
  
  @Override
  public PrimaryKeyIterator getAllIds() throws WdkModelException {
    String[] pkArray = new String[_pkMap.size()];
    String[] pkColNames = _recordClass.getPrimaryKeyDefinition().getColumnRefs();
    if (pkArray.length != pkColNames.length)
      throw new WdkModelException("Incoming primary key array does not match recordclass PK column ref array");
    for (int i = 0; i < pkColNames.length; i++) {
      pkArray[i] = (String)_pkMap.get(pkColNames[i]);
    }
    return new PrimaryKeyIterator() {

      private boolean valueReturned = false;

      @Override
      public boolean hasNext() {
        return !valueReturned;
      }

      @Override
      public String[] next() {
        if (valueReturned) throw new NoSuchElementException();
        valueReturned = true;
        return pkArray;
      }

      @Override
      public void close() {
        // nothing to do here
      }
    };
  }

  @Override
  public Map<String, String> getParamDisplays() {
    return new MapBuilder<String,String>(
        SingleRecordQuestionParam.PRIMARY_KEY_PARAM_NAME,
        join(_pkMap.values().toArray(), ",")
    ).toMap();
  }

  @Override
  public void setSortingMap(Map<String, Boolean> sortingMap) {
    // no-op since sorting is irrelevant in a single record answer
  }

  @Override
  public boolean cacheInitiallyExistedForSpec() throws WdkModelException {
    // does not use WDK cache
    return false;
  }
}
