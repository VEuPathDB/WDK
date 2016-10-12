package org.gusdb.wdk.model.answer.single;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.User;

public class SingleRecordAnswerValue extends AnswerValue {

  private RecordClass _recordClass;
  private Map<String, Object> _pkMap;

  public SingleRecordAnswerValue(User user, RecordClass recordClass,
      Question question, Map<String, Object> pkMap) {
    super(user, question, null, 0, 0, Collections.EMPTY_MAP, null);
    _recordClass = recordClass;
    _pkMap = pkMap;
  }

  @Override
  public RecordInstance[] getRecordInstances() throws WdkModelException, WdkUserException {
    return new RecordInstance[]{ new DynamicRecordInstance(_user, _recordClass, _pkMap) };
  }

  @Override
  public int getResultSize() {
    return 1;
  }

  @Override
  public AnswerValue cloneWithNewPaging(int startIndex, int endIndex) {
    // paging is irrelevant since there's only one record
    return this;
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
    String[] pkColNames = _recordClass.getPrimaryKeyAttributeField().getColumnRefs();
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
