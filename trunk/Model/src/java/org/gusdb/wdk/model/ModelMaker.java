package org.gusdb.gus.wdk.model;

import org.gusdb.gus.wdk.model.*;
import org.gusdb.gus.wdk.model.implementation.*;


import java.io.File;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * ModelMaker.java
 * 
 * A simple class that instantiates a WdkModel from three files. 
 * These are the model config file, model definition file, 
 * model poperties file,  and the RNG schema file for wdkModels.
 *
 * @author Angel Pizarro
 * @version $Revision$ $Date$ $Author$
 */

public class ModelMaker {

  // PUBLIC VARIABLES
  File modelConfigXmlFile;
  File modelXmlFile;
  File modelPropertyFile;
  File schemaFile;
  WdkModel wdkModel;

  // CONSTRUCTOR
  /**
   * SimpleModel bean constructor
   *
   */
  public ModelMaker () {}

  public ModelMaker (String modelXmlFile, 
		     String modelPropertyFile,
		     String modelConfigXmlFile,
		     String schemaFile) 
  {
    this.modelXmlFile = new File(modelXmlFile);
    this.modelConfigXmlFile = new File(modelConfigXmlFile);
    this.modelPropertyFile = new File(modelPropertyFile);
    this.schemaFile = new File(schemaFile);
    initialize();
  }

  // SETTERS/ACCESSORS 
  public void setModelConfigXmlFile (String xmlFile) {
    this.modelConfigXmlFile = new File(xmlFile);
    initialize();
  }

  public File getModelConfigXmlFile () {
    return this.modelConfigXmlFile;
  }

  public void setModelXmlFile (String xmlFile) {
    this.modelXmlFile = new File(xmlFile);
    initialize();
  }

  public File getModelXmlFile () {
    return this.modelXmlFile;
  }
  
  public void setModelPropertyFile (String propertyFile) {
    this.modelPropertyFile = new File(propertyFile);
    initialize();
  }

  public File getModelPropertyFile () {
    return this.modelPropertyFile;
  }

  public void setSchemaFile (String schemaFile) {
    this.schemaFile = new File(schemaFile);
    initialize();
  }

  public File getSchemaFile () {
    return this.schemaFile;
  }

  public WdkModel getModel() {
    return this.wdkModel;
  }

  public static WdkModel makeModelInstance(String modelXmlFile, 
				     String modelPropFile, 
				     String modelCfgFile, 
				     String schemaFile) 
  {
    WdkModel model =  ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL()) ;
    
    
    ModelConfig modelConfig = 
      ModelConfigParser.parseXmlFile(modelConfigXmlFile);
    String connectionUrl = modelConfig.getConnectionUrl();
    String login = modelConfig.getLogin();
    String password = modelConfig.getPassword();
    String platformClass = modelConfig.getPlatformClass();
    RDBMSPlatformI platform = 
      (RDBMSPlatformI)Class.forName(platformClass).newInstance();
    DataSource dataSource = 
      setupDataSource(connectionUrl,login, password);
    platform.setDataSource(dataSource);
    String instanceTable = modelConfig.getQueryInstanceTable();
    ResultFactory resultFactory = new ResultFactory(dataSource, platform, 
						    login, instanceTable);
    
    model.setResources(resultFactory, platform);
    return model;
  }
    
  /**
   * Initializes the WdkModel if all required files are present
   *
   */
  protected void initialize() {
    if (modelXmlFile != null 
	&& modelPropertyFile != null
	&& modelConfigXmlFile != null
	&& schemaFile!= null) 
      {
	wdkModel = makeModelInstance(modelXmlFile, modelPropertyFile, modelConfigXmlFile, schemaFile);
      }
  }
  
  private static DataSource setupDataSource(String connectURI, String login, 
					    String password)  {
    
    //	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    
    //
    // First, we'll need a ObjectPool that serves as the
    // actual pool of connections.
    //
    // We'll use a GenericObjectPool instance, although
    // any ObjectPool implementation will suffice.
    //
    ObjectPool connectionPool = new GenericObjectPool(null);
    
    //
    // Next, we'll create a ConnectionFactory that the
    // pool will use to create Connections.
    // We'll use the DriverManagerConnectionFactory,
    // using the connect string passed in the command line
    // arguments.
    //
    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, login, password);
    
    //
    // Now we'll create the PoolableConnectionFactory, which wraps
    // the "real" Connections created by the ConnectionFactory with
    // the classes that implement the pooling functionality.
    //
    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
    
    //
    // Finally, we create the PoolingDriver itself,
    // passing in the object pool we created.
    //
    PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
    
    return dataSource;
  }
}
