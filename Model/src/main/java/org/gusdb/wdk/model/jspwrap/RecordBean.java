package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.User;

/**
 * A wrapper on a {@link RecordInstance} that provides simplified access for
 * consumption by a view
 */
public class RecordBean {

    private static final Logger LOG = Logger.getLogger(RecordBean.class);

    private final User _user;
    private final RecordInstance _recordInstance;

    public RecordBean(User user, RecordInstance recordInstance) {
        _user = user;
        _recordInstance = recordInstance;
    }

    public RecordBean(UserBean user, RecordClassBean recordClass,
            Map<String, Object> pkValues) throws WdkModelException, WdkUserException {
        _user = user.getUser();
        _recordInstance = new DynamicRecordInstance(user.getUser(),
                recordClass.recordClass, pkValues);
    }
    
    public RecordInstance getRecordInstance() {
        return _recordInstance;
    }

    public boolean isValidRecord() {
        return _recordInstance.isValidRecord();
    }

    public PrimaryKeyValue getPrimaryKey() {
        return _recordInstance.getPrimaryKey();
    }

    public IdAttributeValue getIdAttributeValue() throws WdkModelException, WdkUserException {
      return _recordInstance.getIdAttributeValue();
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(_recordInstance.getRecordClass());
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map<String, AttributeValue> getAttributes() {
        return _recordInstance;
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map<String, AttributeValue> getSummaryAttributes() throws WdkModelException, WdkUserException {
      Set<String> summaryAttributeNames = _recordInstance.getRecordClass().getSummaryAttributeFieldMap().keySet();
      Map<String, AttributeValue> summaryAttributeValueMap = new LinkedHashMap<String, AttributeValue>();
      for (String name: summaryAttributeNames) {
        summaryAttributeValueMap.put(name, _recordInstance.getAttributeValue(name));
      }
      return summaryAttributeValueMap;
    }

    /**
     * @return Map of tableName --> {@link org.gusdb.wdk.model.record.TableValue}
     * @throws WdkUserException 
     * @throws WdkModelException 
     */
    public Map<String, TableValue> getTables() throws WdkModelException, WdkUserException {
        return _recordInstance.getTableValueMap();
    }

    public boolean isInBasket() {
        try {
        if (!_recordInstance.getRecordClass().isUseBasket()) return false;
        if (!_recordInstance.isValidRecord()) return false;
        AttributeValue value = _recordInstance.getAttributeValue(BasketFactory.BASKET_ATTRIBUTE);
        return "1".equals(value.getValue());
        } catch(Exception ex) {
            LOG.warn("something wrong when check the inBasket state, need further investigation:\n" + ex);
            ex.printStackTrace();
            return false;
        }
    }

    @Deprecated // pending struts removal
    public boolean isInFavorite() {
      RecordClass recordClass = _recordInstance.getRecordClass();
      Map<String, String> pkValues = _recordInstance.getPrimaryKey().getValues();
      Map<String, Object> values = new LinkedHashMap<String, Object>();
      for (String column : pkValues.keySet()) {
        values.put(column, pkValues.get(column));
      }
      return _user.getWdkModel().getFavoriteFactory().isInFavorite(_user, recordClass, values);
    }

    public void removeTableValue(String tableName) {
      _recordInstance.removeTableValue(tableName);
    }
}
