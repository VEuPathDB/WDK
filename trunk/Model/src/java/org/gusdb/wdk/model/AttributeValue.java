package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONException;

public abstract class AttributeValue {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AttributeValue.class.getName());

    protected AttributeField field;
    protected Object value;

    public abstract Object getValue() throws WdkModelException;

    public AttributeValue(AttributeField field) {
        this.field = field;
    }

    public AttributeField getAttributeField() {
        return this.field;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getDisplayName()
     */
    public String getDisplayName() {
        return field.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getName()
     */
    public String getName() {
        return field.getName();
    }
    
    public String getBriefDisplay() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        String display = getDisplay();
        int truncateTo = field.getTruncateTo();
        switch (truncateTo) {
          case -1:
            return display;
          case 0:
            truncateTo = Utilities.TRUNCATE_DEFAULT;
            // drop through
          default:
            return (display.length() <= truncateTo ? display :
                display.substring(0, truncateTo) + "...");
        }
    }

    public String getDisplay() throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        Object value = getValue();
        return (value != null) ? value.toString() : "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            Object value = getValue();
            return (value == null) ? "" : value.toString();
        }
        catch (WdkModelException ex) {
            throw new RuntimeException(ex);
        }
    }
}
