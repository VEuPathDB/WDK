package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;
import java.util.List;

import org.gusdb.wdk.model.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.User;

public class BasketBean {

    private User user;
    private RecordClass recordClass;

    public BasketBean(User user) {
        this.user = user;
    }

    public void setRecordClassRef(String recordClassRef)
            throws WdkModelException {
        WdkModel wdkModel = user.getWdkModel();
        this.recordClass = wdkModel.getRecordClass(recordClassRef);
    }

    public List<PrimaryKeyAttributeValue> getDeprecatedRecordsByType()
            throws WdkModelException, SQLException {
        BasketFactory basketFactory = user.getWdkModel().getBasketFactory();
        return basketFactory.getDeprecatedRecords(user, recordClass);
    }
}
