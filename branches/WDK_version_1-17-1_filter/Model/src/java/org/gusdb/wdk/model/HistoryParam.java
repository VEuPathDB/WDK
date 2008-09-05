/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author xingao
 * 
 */
public class HistoryParam extends Param {

    private UserFactory factory;

    public HistoryParam() {}
    
    public HistoryParam(HistoryParam param) {
        super(param);
        this.factory = param.factory;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    @Override
    public String validateValue(Object value) throws WdkModelException {
        String compound = value.toString();
        String[] parts = compound.split(":");
        if (parts.length != 2)
            return "Invalid value format of HistoryParam " + name + ": "
                    + value;

        // the input have a valid user id and history id
        String signature = parts[0].trim();
        String strHistId = parts[1].trim();
        if (strHistId.matches("\\d+")) {
            int historyId = Integer.parseInt(strHistId);
            try {
                User user = factory.loadUserBySignature(signature);
                //user.getHistory(historyId);
		user.getStep(historyId);
            } catch (WdkUserException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            }
            return null;
        } else {
            return "Invalid value format of HistoryParam " + name + ": "
                    + value;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void resolveReferences(WdkModel model) throws WdkModelException {}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#setResources(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void setResources(WdkModel model) throws WdkModelException {
        super.setResources(model);
        try {
            factory = model.getUserFactory();
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    protected String getInternalValue(String value) throws WdkModelException {
        try {
            //History history = getHistory(value);
            Step history = getStep(value);
	    return history.getCacheFullTable();
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*public History getHistory(String combinedId)
            throws WdkUserException, WdkModelException {
        String[] parts = combinedId.split(":");
        // the input have a valid user id and history id
        String signature = parts[0].trim();
        String strHistId = parts[1].trim();

        int historyId = Integer.parseInt(strHistId);
        User user = factory.loadUserBySignature(signature);
        return user.getHistory(historyId);
	}*/

    public Step getStep(String combinedId)
	throws WdkUserException, WdkModelException {
	String[] parts = combinedId.split(":");
	String signature = parts[0].trim();
	String strHistId = parts[1].trim();

	int userAnswerId = Integer.parseInt(strHistId);
	User user = factory.loadUserBySignature(signature);
	return user.getStep(userAnswerId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#compressValue(java.lang.Object)
     */
    @Override
    public String compressValue(Object value) throws WdkModelException {
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            StringBuffer sb = new StringBuffer();
            for (String strVal : array) {
                if (sb.length() > 0) sb.append(",");
                sb.append(strVal);
            }
            value = sb.toString();
        }
        return super.compressValue(value);
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new HistoryParam(this);
    }
}
