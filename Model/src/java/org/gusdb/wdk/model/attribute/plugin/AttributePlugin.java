package org.gusdb.wdk.model.attribute.plugin;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.ColumnAttributeField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public interface AttributePlugin {

    String getName();

    void setName(String name);

    String getDisplay();

    void setDisplay(String display);

    String getView();

    void setView(String view);

    void setProperties(Map<String, String> properties);

    void setAttribute(ColumnAttributeField attribute);

    Map<String, Object> process(AnswerValue answerValue)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException;

}
