package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.json.JSONException;

public abstract class AttributeValue {

    // private static final Logger logger =
    // WdkLogManager.getLogger("org.gusdb.wdk.model.FieldValue");

    protected AttributeField field;
    protected Object value;

    public abstract Object getValue() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

    public AttributeValue(AttributeField field) {
        this.field = field;
    }

    public AttributeField getAttributeField() {
        return this.field;
    }

    public String getBriefValue() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Object value = getValue();
        if (value == null) return "";
        String strValue = value.toString();
        int truncateTo = field.getTruncateTo();
        if (strValue.length() > truncateTo)
            strValue = strValue.substring(0, truncateTo) + "...";
        return strValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            return (String) getValue();
        } catch (WdkModelException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        } catch (WdkUserException ex) {
            throw new RuntimeException(ex);
        }
    }
}
