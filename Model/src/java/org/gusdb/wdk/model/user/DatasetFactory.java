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
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {
    
    private static Logger logger = Logger.getLogger( DatasetFactory.class );
    
    private RDBMSPlatformI platform;
    private String datasetSchema;
    
    public DatasetFactory( RDBMSPlatformI platform, String datasetSchema ) {
        this.platform = platform;
        this.datasetSchema = datasetSchema;
    }
    
    String getDatasetSchema() {
        return datasetSchema;
    }
    
    public Dataset makeDataset( User user, String uploadFile, String[ ] values )
            throws WdkUserException, WdkModelException {
        // put the dataset into index
        Dataset dataset = putDatasetIndex( values );
        
        // put dataset into user domain
        return putUserDataset( user, dataset, uploadFile );
    }
    
    public Dataset getDataset( User user, String datasetChecksum )
            throws WdkUserException {
        // check if the dataset exist, globally
        Dataset dataset = getDatasetIndex( datasetChecksum );
        if ( dataset == null ) return null;
        
        // check if dataset exists in the user's domain
        return getUserDataset( user, dataset );
    }
    
    public Dataset getDataset( User user, int datasetId )
            throws WdkUserException {
        // check if the dataset exist, globally
        Dataset dataset = getDatasetIndex( datasetId );
        if ( dataset == null ) return null;
        
        // check if dataset exists in the user's domain
        return getUserDataset( user, dataset );
    }
    
    private Dataset putDatasetIndex( String[ ] values )
            throws WdkModelException, WdkUserException {
        // validate the value
        if ( values == null )
            throw new WdkUserException( "The dataset is empty" );
        
        // refactor values
        List< String > list = new ArrayList< String >();
        StringBuffer sb = new StringBuffer();
        for ( String value : values ) {
            value = value.trim();
            if ( value.length() == 0 ) continue;
            list.add( value );
            
            if ( sb.length() > 0 ) sb.append( ", " );
            sb.append( value );
        }
        values = new String[ list.size() ];
        list.toArray( values );
        
        // create dataset checksum
        String valueContent = sb.toString();
        if ( valueContent.length() == 0 )
            throw new WdkUserException( "The dataset is empty" );
        String checksum = Utilities.encrypt( valueContent );
        
        // check if the dataset value exists
        Dataset dataset = getDatasetIndex( checksum );
        if ( dataset != null ) return dataset;
        
        // dataset doesn't exist in the database, save it
        
        // compose the summary
        int pos = 0;
        while (true) {
            int nextPos = valueContent.indexOf(",", pos + 1);
            if (nextPos >= Utilities.MAX_PARAM_VALUE_SIZE - 3) break;
            pos = nextPos;
            if (pos < 0) break;
        }
        String summary;
        if (pos > 0) summary = valueContent.substring(0, pos).trim() + "...";
        else summary = valueContent;
        
        DataSource dataSource = platform.getDataSource();
        PreparedStatement psIndex = null;
        PreparedStatement psValue = null;
        try {
            // insert dataset index
            int datasetId = Integer.parseInt( platform.getNextId(
                    datasetSchema, "dataset_indices" ) );
            int size = valueContent.length();
            
            psIndex = SqlUtils.getPreparedStatement( dataSource, "INSERT INTO "
                    + datasetSchema + "dataset_indices (dataset_id, "
                    + "dataset_checksum, summary, dataset_size) VALUES "
                    + "(?, ?, ?, ?)" );
            psIndex.setInt( 1, datasetId );
            psIndex.setString( 2, checksum );
            psIndex.setString( 3, summary );
            psIndex.setInt( 4, size );
            psIndex.execute();
            
            // insert dataset values
            psValue = SqlUtils.getPreparedStatement( dataSource, "INSERT INTO "
                    + datasetSchema + "dataset_values (dataset_id, "
                    + "dataset_value) VALUES (?, ?)" );
            for ( int i = 0; i < values.length; i++ ) {
                psValue.setInt( 1, datasetId );
                psValue.setString( 2, values[ i ].trim() );
                psValue.addBatch();
                if ( ( i + 1 ) % 1000 == 0 ) psValue.executeBatch();
            }
            psValue.executeBatch();
            
            dataset = new Dataset( this, datasetId, checksum );
            dataset.setSummary( summary );
            dataset.setSize( size );
            return dataset;
        } catch ( SQLException ex ) {
            throw new WdkUserException( ex );
        } finally {
            try {
                SqlUtils.closeStatement( psIndex );
                SqlUtils.closeStatement( psValue );
            } catch ( SQLException ex ) {
                throw new WdkUserException( ex );
            }
        }
    }
    
    private Dataset getDatasetIndex( String datasetChecksum )
            throws WdkUserException {
        DataSource dataSource = platform.getDataSource();
        ResultSet rs = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement( dataSource,
                    "SELECT dataset_id, summary, dataset_size FROM "
                            + datasetSchema + "dataset_indices "
                            + "WHERE dataset_checksum = ?" );
            ps.setString( 1, datasetChecksum );
            rs = ps.executeQuery();
            
            if ( !rs.next() ) return null;
            
            int datasetId = rs.getInt( "dataset_id" );
            String summary = rs.getString( "summary" );
            int datasetSize = rs.getInt( "dataset_size" );
            
            Dataset dataset = new Dataset( this, datasetId, datasetChecksum );
            dataset.setSummary( summary );
            dataset.setSize( datasetSize );
            return dataset;
        } catch ( SQLException ex ) {
            throw new WdkUserException( ex );
        } finally {
            try {
                SqlUtils.closeResultSet( rs );
            } catch ( SQLException ex ) {
                throw new WdkUserException( ex );
            }
        }
    }
    
    private Dataset getDatasetIndex( int datasetId )
            throws WdkUserException {
        DataSource dataSource = platform.getDataSource();
        ResultSet rs = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement( dataSource,
                    "SELECT dataset_checksum, summary, dataset_size FROM "
                            + datasetSchema + "dataset_indices "
                            + "WHERE dataset_id = ?" );
            ps.setInt( 1, datasetId);
            rs = ps.executeQuery();
            
            if ( !rs.next() ) return null;
            
            String datasetChecksum = rs.getString( "dataset_checksum" );
            String summary = rs.getString( "summary" );
            int datasetSize = rs.getInt( "dataset_size" );
            
            Dataset dataset = new Dataset( this, datasetId, datasetChecksum );
            dataset.setSummary( summary );
            dataset.setSize( datasetSize );
            return dataset;
        } catch ( SQLException ex ) {
            throw new WdkUserException( ex );
        } finally {
            try {
                SqlUtils.closeResultSet( rs );
            } catch ( SQLException ex ) {
                throw new WdkUserException( ex );
            }
        }
    }
   
    private Dataset putUserDataset( User user, Dataset dataset,
            String uploadFile ) throws WdkUserException {
        logger.info( "save upload file: " + uploadFile );
        int userId = user.getUserId();
        int datasetId = dataset.getDatasetId();
        Date createTime = new Date( System.currentTimeMillis() );
        DataSource dataSource = platform.getDataSource();
        
        // check if dataset exists for this user
        Dataset ds = getUserDataset( user, dataset );
        String sql;
        if ( ds != null ) {
            sql = "UPDATE " + datasetSchema + "user_datasets SET "
                    + "create_time = ?, upload_file = ? WHERE dataset_id = ? "
                    + "AND user_id = ?";
        } else {
            sql = "INSERT INTO " + datasetSchema + "user_datasets (create_time"
                    + ", upload_file, dataset_id, user_id) VALUES (?, ?, ?, ?)";
        }
        
        // dataset doesn't exist for this user, add one
        PreparedStatement ps = null;
        try {
            ps = SqlUtils.getPreparedStatement( dataSource, sql );
            ps.setDate( 1, new java.sql.Date( createTime.getTime() ) );
            ps.setString( 2, uploadFile );
            ps.setInt( 3, datasetId );
            ps.setInt( 4, userId );
            ps.execute();
            
            // complete the result
            dataset.setUser( user );
            dataset.setCreateTime( createTime );
            dataset.setUploadFile( uploadFile );
            return dataset;
        } catch ( SQLException ex ) {
            throw new WdkUserException( ex );
        } finally {
            try {
                SqlUtils.closeStatement( ps );
            } catch ( SQLException ex ) {
                throw new WdkUserException( ex );
            }
        }
    }
    
    private Dataset getUserDataset( User user, Dataset dataset )
            throws WdkUserException {
        int userId = user.getUserId();
        int datasetId = dataset.getDatasetId();
        DataSource dataSource = platform.getDataSource();
        
        ResultSet rs = null;
        PreparedStatement psInsert = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement( dataSource,
                    "SELECT create_time, upload_file FROM " + datasetSchema
                            + "user_datasets WHERE user_id = ? "
                            + "AND dataset_id = ?" );
            ps.setInt( 1, userId );
            ps.setInt( 2, datasetId );
            rs = ps.executeQuery();
            
            if ( !rs.next() ) {
                logger.info( "dataset #" + datasetId
                        + " is not in user's domain, creating it." );
                // dataset is not in the user's domain, add it
                Date createTime = new Date( System.currentTimeMillis() );
                String uploadFile = ""; // empty upload file
                psInsert = SqlUtils.getPreparedStatement( dataSource, "INSERT "
                        + "INTO " + datasetSchema + "user_datasets "
                        + "(dataset_id, user_id, create_time, upload_file) "
                        + "VALUES (?, ?, ?, ?)" );
                psInsert.setInt( 1, datasetId );
                psInsert.setInt( 2, userId );
                psInsert.setDate( 3, new java.sql.Date( createTime.getTime() ) );
                psInsert.setString( 4, uploadFile );
                psInsert.execute();
                
                // complete the result
                dataset.setUser( user );
                dataset.setCreateTime( createTime );
                dataset.setUploadFile( uploadFile );
                return dataset;
            }
            
            dataset.setUser( user );
            dataset.setCreateTime( rs.getDate( "create_time" ) );
            dataset.setUploadFile( rs.getString( "upload_file" ) );
            return dataset;
        } catch ( SQLException ex ) {
            throw new WdkUserException( ex );
        } finally {
            try {
                SqlUtils.closeResultSet( rs );
                SqlUtils.closeStatement( psInsert );
            } catch ( SQLException ex ) {
                throw new WdkUserException( ex );
            }
        }
    }
    
    String[ ] getDatasetValues( Dataset dataset ) throws WdkUserException {
        ResultSet rsData = null;
        try {
            PreparedStatement psData = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT dataset_value FROM "
                            + datasetSchema + "dataset_values "
                            + "WHERE dataset_id = ?" );
            psData.setInt( 1, dataset.getDatasetId() );
            rsData = psData.executeQuery();
            List< String > values = new ArrayList< String >();
            while ( rsData.next() ) {
                String value = rsData.getString( "dataset_value" );
                values.add( value );
            }
            String[ ] array = new String[ values.size() ];
            values.toArray( array );
            return array;
        } catch ( SQLException ex ) {
            throw new WdkUserException( ex );
        } finally {
            try {
                SqlUtils.closeResultSet( rsData );
            } catch ( SQLException ex ) {
                throw new WdkUserException( ex );
            }
        }
    }
}
