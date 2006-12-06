/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;

/**
 * @author xingao the DatasetParam is not functioning as of Nov.16, 2006, will
 *         be rewritten soon. The User cannot be a member, since DatasetParam
 *         has a global scope, while User object does not.
 */
public class DatasetParam extends Param {

    public static enum InputType {
        Single, Dataset, History,
    }

    private WdkModel wdkModel;
    private User user;
    private String dataType;

    /**
     * 
     */
    public DatasetParam() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    @Override
    public String validateValue(Object value) {
        String data = value.toString();

        // validate input type
        InputType inputType;
        try {
            inputType = getInputType(data);
        } catch (WdkModelException ex) {
            return ex.toString();
        }
        // get the value part
        int pos = data.indexOf(':');
        String val = data.substring(pos + 1).trim();
        if (val.length() == 0) return "The value is empty.";

        // validate dataset name
        try {
            if (inputType == InputType.Dataset) {
                if (null == user.getDataset(val))
                    return "The dataset name '" + val + "' is invalid.";
            } else if (inputType == InputType.History) {
                String[] parts = val.split(":");
                String signature = parts[0].trim();
                int historyId = Integer.parseInt(parts[1].trim());
                if (null == user.getHistory(historyId))
                    return "The history id #" + historyId + " is invalid.";
            }
        } catch (NumberFormatException ex) {
            return ex.toString();
        } catch (WdkUserException ex) {
            return ex.toString();
        } catch (WdkModelException ex) {
            return ex.toString();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    protected void resolveReferences(WdkModel model) throws WdkModelException {
        this.wdkModel = model;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        DatasetParam param = new DatasetParam();
        super.clone(param);
        param.wdkModel = wdkModel;
        param.user = user;
        param.dataType = dataType;
        return param;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return Returns the type.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public InputType getInputType(String value) throws WdkModelException {
        // get the input type of the value
        int pos = value.indexOf(':');
        if (pos < 0)
            throw new WdkModelException("Invalid value for DatasetParam '"
                    + name + "': " + value);
        String type = value.substring(0, pos).trim();
        try {
            return InputType.valueOf(InputType.class, type);
        } catch (IllegalArgumentException ex) {
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
            return getDatasetTable(value);
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    Dataset getDataset(String value) throws WdkModelException, WdkUserException {
        InputType type = getInputType(value);

        int pos = value.indexOf(':');
        String data = value.substring(pos + 1).trim();
        Dataset dataset = null;
        if (type == InputType.Single) {
            dataset = createDataset(data);
        } else if (type == InputType.Dataset) {
            dataset = getDatasetByName(data);
        } else if (type == InputType.History) {
            int historyId = Integer.parseInt(data);
            dataset = convertToDataset(historyId);
        }
        return dataset;
    }

    /**
     * @param value
     *            the value must start with Single:<single_value>, Dataset:<dataset_name>,
     *            or History:<history_id>
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public String getDatasetTable(String value) throws WdkModelException,
            WdkUserException {
        return getDataset(value).getCacheTable();
    }

    /**
     * create a temporary dataset table with the given single value
     * 
     * @param data
     * @return
     * @throws WdkUserException
     */
    private Dataset createDataset(String data) throws WdkUserException {
        // get dataset factory
        DatasetFactory factory = wdkModel.getDatasetFactory();
        String projectId = wdkModel.getProjectId();

        // split the value, if needed
        String[] rows = data.split("[;\n]");
        String[][] values = new String[rows.length][2];
        for (int i = 0; i < rows.length; i++) {
            String[] parts = rows[i].split(",");
            if (parts.length < 2) {
                values[i][0] = projectId;
                values[i][1] = parts[0];
            } else {
                values[i][0] = parts[0];
                values[i][1] = parts[1];
            }
        }
        String datasetName = "temp_" + user.getUserId();
        return factory.createDataset(user, datasetName, dataType, values, true);
    }

    /**
     * load the dataset with the given dataset id
     * 
     * @param datasetId
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    private Dataset getDatasetByName(String datasetName)
            throws WdkModelException, WdkUserException {
        // validate the data type
        Dataset dataset = user.getDataset(datasetName);
        if (!dataType.equalsIgnoreCase(dataset.getDataType()))
            throw new WdkModelException("Incompatible data types between "
                    + "dataset '" + datasetName + "' and DatasetParam '"
                    + getFullName() + "'.");
        return dataset;
    }

    /**
     * convert from history id into a dataset
     * 
     * @param historyId
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    private Dataset convertToDataset(int historyId) throws WdkUserException,
            WdkModelException {
        // check the data type
        History history = user.getHistory(historyId);
        if (!dataType.equalsIgnoreCase(history.getDataType()))
            throw new WdkModelException("Incompatible data types between "
                    + "history #" + historyId + " and DatasetParam '"
                    + getFullName() + "'.");

        // get dataset factory
        DatasetFactory factory = wdkModel.getDatasetFactory();

        String datasetName = "temp_" + user.getUserId();
        Answer answer = history.getAnswer();
        QueryInstance qinstance = answer.getIdsQueryInstance();
        String cacheTable = qinstance.getResultAsTableName();

        // check whether the answer contains projectIds or not
        RecordClass recordClass = answer.getQuestion().getRecordClass();
        String projectIdColumn = null;
        if (recordClass.getPrimaryKeyField().getProjectParam() != null)
            projectIdColumn = qinstance.projectColumnName;
        String primaryKeyColumn = qinstance.primaryKeyColumnName;

        return factory.createDataset(user, datasetName, dataType, cacheTable,
                primaryKeyColumn, true);
    }
}
