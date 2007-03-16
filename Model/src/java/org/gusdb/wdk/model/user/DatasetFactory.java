/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {

    private static Logger logger = Logger.getLogger(DatasetFactory.class);

    private RDBMSPlatformI platform;
    private String datasetSchema;

    public DatasetFactory(RDBMSPlatformI platform, String datasetSchema) {
        this.platform = platform;
        this.datasetSchema = datasetSchema;
    }

    String getDatasetSchema() {
        return datasetSchema;
    }

    Dataset createDataset(User user, String uploadFile, String summary,
            String[] values) throws WdkUserException {
        DataSource dataSource = platform.getDataSource();
        PreparedStatement psIndex = null;
        PreparedStatement psData = null;
        try {
            int datasetId = Integer.parseInt(platform.getNextId(datasetSchema,
                    "dataset_index"));
            int userId = user.getUserId();
            Date createTime = new Date();

            // insert dataset record
            psIndex = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + datasetSchema + "dataset_index (dataset_id, user_id, "
                    + "create_time, upload_file, summary, dataset_size) "
                    + "VALUES (?, ?, ?, ?, ?, ?)");
            psIndex.setInt(1, datasetId);
            psIndex.setInt(2, userId);
            psIndex.setDate(3, new java.sql.Date(createTime.getTime()));
            psIndex.setString(4, uploadFile);
            psIndex.setString(5, summary);
            psIndex.setInt(6, values.length);
            psIndex.executeUpdate();

            // insert dataset values
            psData = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + datasetSchema + "dataset_data "
                    + "(dataset_id, user_id, dataset_value) VALUES (?, ?, ?)");
            int count = 0;
            for (String value : values) {
                psData.setInt(1, datasetId);
                psData.setInt(2, userId);
                psData.setString(3, value.trim());
                psData.addBatch();
                count++;
                if (count % 1000 == 0) psData.executeBatch();
            }
            psData.executeBatch();

            // TEST
            logger.info("Dataset #" + datasetId + " created with "
                    + values.length + " values");

            // create dataset
            Dataset dataset = new Dataset(this, user, datasetId);
            dataset.setCreateTime(createTime);
            dataset.setUploadFile(uploadFile);
            dataset.setSummary(summary);
            dataset.setSize(values.length);
            return dataset;
        } catch (NumberFormatException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psIndex);
                SqlUtils.closeStatement(psData);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    Dataset getDataset(User user, int datasetId) throws WdkUserException {
        ResultSet rsIndex = null;
        try {
            PreparedStatement psIndex = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT create_time, "
                            + "upload_file, summary, dataset_size FROM "
                            + datasetSchema + "dataset_index "
                            + "WHERE user_id = ? AND dataset_id = ?");
            psIndex.setInt(1, user.getUserId());
            psIndex.setInt(2, datasetId);
            rsIndex = psIndex.executeQuery();
            if (rsIndex.next()) {
                Dataset dataset = new Dataset(this, user, datasetId);
                dataset.setCreateTime(rsIndex.getDate("create_time"));
                dataset.setUploadFile(rsIndex.getString("upload_file"));
                dataset.setSummary(rsIndex.getString("summary"));
                dataset.setSize(rsIndex.getShort("dataset_size"));
                return dataset;
            } else throw new WdkUserException("The dataset #" + datasetId
                    + " of user #" + user.getUserId()
                    + " cannot be found in the database");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsIndex);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    String[] getDatasetValues(Dataset dataset) throws WdkUserException {
        ResultSet rsData = null;
        try {
            PreparedStatement psData = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT dataset_value FROM "
                            + datasetSchema + "dataset_data "
                            + "WHERE dataset_id = ? AND user_id = ?");
            psData.setInt(1, dataset.getDatasetId());
            psData.setInt(2, dataset.getUser().getUserId());
            rsData = psData.executeQuery();
            List<String> values = new ArrayList<String>();
            while (rsData.next()) {
                String value = rsData.getString("dataset_value");
                values.add(value);
            }
            String[] array = new String[values.size()];
            values.toArray(array);
            return array;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsData);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void deleteDataset(User user, int datasetId) throws WdkUserException {
        DataSource dataSource = platform.getDataSource();
        int userId = user.getUserId();
        PreparedStatement psIndex = null;
        PreparedStatement psData = null;
        try {
            // delete data of the dataset
            psData = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + datasetSchema + "dataset_data WHERE dataset_id = ? "
                    + " AND user_id = ?");
            psData.setInt(1, datasetId);
            psData.setInt(2, userId);
            psData.executeUpdate();

            // delete index of the dataset
            psIndex = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + datasetSchema + "dataset_index WHERE dataset_id = ? "
                    + "AND user_id = ?");
            psIndex.setInt(1, datasetId);
            psIndex.setInt(2, userId);
            psIndex.executeUpdate();
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psIndex);
                SqlUtils.closeStatement(psData);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }
}
