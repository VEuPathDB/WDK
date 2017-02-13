package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

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

    private static final Logger logger = Logger.getLogger(RecordBean.class);

    private final User user;
    private final RecordInstance recordInstance;

    public RecordBean(User user, RecordInstance recordInstance) {
        this.user = user;
        this.recordInstance = recordInstance;
    }

    public RecordBean(UserBean user, RecordClassBean recordClass,
            Map<String, Object> pkValues) throws WdkModelException, WdkUserException {
        this.user = user.getUser();
        recordInstance = new DynamicRecordInstance(user.getUser(),
                recordClass.recordClass, pkValues);
    }
    
    public RecordInstance getRecordInstance() {
        return recordInstance;
    }

    public boolean isValidRecord() {
        return recordInstance.isValidRecord();
    }

    public PrimaryKeyValue getPrimaryKey() {
        return recordInstance.getPrimaryKey();
    }

    public IdAttributeValue getIdAttributeValue() throws WdkModelException, WdkUserException {
      return recordInstance.getIdAttributeValue();
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(recordInstance.getRecordClass());
    }

    public String[] getSummaryAttributeNames() {
        return recordInstance.getSummaryAttributeNames();
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map<String, AttributeValue> getAttributes() {
        return recordInstance;
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map<String, AttributeValue> getSummaryAttributes() throws WdkModelException, WdkUserException {
      Map<String, AttributeValue> attributeValueMap = recordInstance.getAttributeValueMap();
      Map<String, AttributeValue> summaryAttributeValueMap = new LinkedHashMap<String, AttributeValue>();
      for (String name: getSummaryAttributeNames())
        summaryAttributeValueMap.put(name, attributeValueMap.get(name));
      return summaryAttributeValueMap;
    }

    /**
     * @return Map of tableName --> {@link org.gusdb.wdk.model.record.TableValue}
     * @throws WdkUserException 
     * @throws WdkModelException 
     */
    public Map<String, TableValue> getTables() throws WdkModelException, WdkUserException {
        return recordInstance.getTableValueMap();
    }

    public boolean isInBasket() {
        try {
        if (!recordInstance.getRecordClass().isUseBasket()) return false;
        if (!recordInstance.isValidRecord()) return false;
        AttributeValue value = recordInstance.getAttributeValue(BasketFactory.BASKET_ATTRIBUTE);
        return "1".equals(value.getValue());
        } catch(Exception ex) {
            logger.warn("something wrong when check the inBasket state, need " +
            		"further investigation:\n" + ex);
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isInFavorite() throws WdkModelException {
        try {
            RecordClass recordClass = recordInstance.getRecordClass();
            Map<String, String> pkValues = recordInstance.getPrimaryKey()
                    .getValues();
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (String column : pkValues.keySet()) {
                values.put(column, pkValues.get(column));
            }
            return user.isInFavorite(recordClass, values);
        } catch (WdkModelException ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        }
    }
}
