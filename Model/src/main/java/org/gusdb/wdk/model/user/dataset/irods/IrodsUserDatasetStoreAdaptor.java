package org.gusdb.wdk.model.user.dataset.irods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.transfer.TransferControlBlock;

/**
 * IRODS adaptor class provides an IRODS file system implementation of methods for user
 * dataset manipulation.  The adaptor uses a single set of credentials for access.
 * 
 * @author crisl-adm
 */
public class IrodsUserDatasetStoreAdaptor implements UserDatasetStoreAdaptor {

  private static IRODSFileSystem system;
  private static IRODSAccount account;
  private static IRODSAccessObjectFactory accessObjectFactory;
  private static final String IRODS_ID_ATTRIBUTE = "irods_id";
  private static String _homeDir;
  private String _wdkTempDirName;
  
  public IrodsUserDatasetStoreAdaptor(String wdkTempDirName) {
    _wdkTempDirName = wdkTempDirName;
  }
  
  /**
   * This method sets the IRODSFileSystem object instance variable if it is not already populated.
   * It creates a new instance to be used subsequently by all IRODS methods.
   * @throws WdkModelException
   */
  private static void setSystem() throws JargonException {
    if(system == null) {
      system = IRODSFileSystem.instance();
    }
  }
  
  /**
   * This method returns the IRODSAccount object instance variable if populated.  Otherwise, it
   * uses the method args to create a new IRODSAccount to be used subsequently by all IRODS methods.
   * @param host
   * @param port
   * @param user
   * @param pwd
   * @param zone
   * @param resource
   * @return
   */
  private static IRODSAccount initializeAccount(String host,
		  int port, String user, String password, String zone, String resource) throws WdkModelException {
    if(account == null) {
    	  try {
        ClientServerNegotiationPolicy csnp = new ClientServerNegotiationPolicy();
        csnp.setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
        _homeDir = "/" + zone + "/home/" + user;
        account = IRODSAccount.instance(host,port,user,password,_homeDir,zone,resource,csnp);
    	  }
    	  catch(JargonException je) {
    	    throw new WdkModelException(je);
    	  }
    }
    return account;
  }

  /**
   * Convenience method to create system and account instance variables for IRODS if either is not
   * already created.
   * @param host
   * @param port
   * @param user
   * @param pwd
   * @param zone
   * @param resource
   * @throws WdkModelException
   */
  public static void initializeIrods(String host, int port, String user, String pwd, String zone, String resource) throws WdkModelException {
	try {  
      setSystem();
      initializeAccount(host, port, user, pwd, zone, resource);
      accessObjectFactory = system.getIRODSAccessObjectFactory();
//      IRODSSession irodsSession = accessObjectFactory.getIrodsSession();
//      SettableJargonProperties jargonProperties = new SettableJargonProperties();
//      jargonProperties.setEncryptionAlgorithmEnum(EncryptionAlgorithmEnum.AES_256_CBC);
//      jargonProperties.setEncryptionKeySize(32);
//      jargonProperties.setEncryptionNumberHashRounds(16);
//      jargonProperties.setEncryptionSaltSize(8);
//      jargonProperties.setNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
//      irodsSession.setJargonProperties(jargonProperties);
	}
	catch(JargonException je) {
	  throw new WdkModelException(je);
	}
  }
  
  
  public static IRODSAccessObjectFactory getAccessObjectFactory() {
    return accessObjectFactory;
  }

  /**
   * Convenience method to retrieve the IRODS file factory (hiding account)
   * @param accessObjectFactory
   * @return - IRODS file factory
   * @throws WdkModelException
   */
  public IRODSFileFactory getIrodsFileFactory() throws WdkModelException {
	  try {
	  if(accessObjectFactory != null) {
		return accessObjectFactory.getIRODSFileFactory(account);
	  }
	  else {
		throw new WdkModelException("The IRODS access object factory cannot be null");
	  }
	}
	catch(JargonException je) {
	  throw new WdkModelException(je);
	}
  }
  
  /**
   * Convenience method to retrieve the IRODS data transfer operations object (hiding account)
   * @param accessObjectFactory
   * @return - the IRODS data transfer operations object
   * @throws WdkModelException
   */
  public DataTransferOperations getDataTransferOperations() throws WdkModelException {
	try {
	  if(accessObjectFactory != null) {
		return accessObjectFactory.getDataTransferOperations(account);
	  }
	  else {
		throw new WdkModelException("The IRODS access object factory cannot be null");
	  }
	}
	catch(JargonException je) {
	  throw new WdkModelException(je);
	}
  }
  
  /**
   * Convenience method to retrieve the IRODS object given the path name
   * @param fileFactory - IRODS file factory
   * @return - the IRODS file
   * @throws WdkModelException
   */
  public IRODSFile getIrodsFile(IRODSFileFactory fileFactory, String pathName) throws WdkModelException {
	try {  
	  if(fileFactory != null) {
	    return fileFactory.instanceIRODSFile(pathName);
	  }
	  else {
	    throw new WdkModelException("The IRODS file factory cannot be null"); 
	  }
	}
	catch(JargonException je) {
	  throw new WdkModelException(je);
	}
  }
  
  public IRODSRandomAccessFile getIrodsRandomAccessFile(IRODSFileFactory fileFactory, String pathName) throws WdkModelException {
    try {
    	  if(fileFactory != null) {
        return fileFactory.instanceIRODSRandomAccessFile(pathName);
    	  }
    	  else {
    	    throw new WdkModelException("The IRODS file factory cannot be null");
    	  }
    }
    catch(JargonException je) {
    	  throw new WdkModelException(je);
    }
  }
  
  /**
   * Convenience method to close the IRODSFile.  This class does not implement Autoclosable.
   * @param file - IRODSFile object
   * @throws WdkModelException
   */
  public void closeFile(IRODSFile file) throws WdkModelException {
    if(file != null) {
      try {
        file.close();
      }
      catch(JargonException je) {
        throw new WdkModelException("Problem closing IRODS file. - " + je);
      }
    }  
  }
  
  /**
   * Convenience method to close the Jargon session.  Note that the Jargon connection must be
   * closed to avoid dangling irodsAgent processes.
   */
  public void closeSession() {
	if(accessObjectFactory != null) {
	  accessObjectFactory.closeSessionAndEatExceptions();
	}
  }
  
  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#moveFileAtomic(Path,Path)
   */
  @Override
  public void moveFileAtomic(Path from, Path to) throws WdkModelException {
	long start = System.currentTimeMillis();
    String fromPathName = from.toString();
    String toPathName = to.toString();
    IRODSFile sourceFile = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      sourceFile = getIrodsFile(fileFactory, fromPathName);
      DataTransferOperations dataXferOps = getDataTransferOperations();
      // Resorted to this workaround because Jargon cannot move one file into an already occupied
      // location and no 'force' flag is implemented (apparently) in Jargon yet.
      TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
      tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
      dataXferOps.copy(fromPathName, "", toPathName, null, tcb);
      sourceFile.delete();
    }
    catch(JargonException je) {
      throw new WdkModelException(je);
    }
    finally {
      closeFile(sourceFile);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","moveFileAtomic-irods: " + fromPathName,start);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#getPathsInDir(Path)
   */
  @Override
  public List<Path> getPathsInDir(Path dir) throws WdkModelException {
	long start = System.currentTimeMillis();
	String pathName = dir.toString();
    IRODSFile irodsObject = null;
    List<Path> paths = new ArrayList<>(); 
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsObject = getIrodsFile(fileFactory,pathName);
      if(!irodsObject.isDirectory()) {
        throw new WdkModelException("The path " + pathName + " does not refer to a directory.");
      }
      String[] names = irodsObject.list();
      for(String name : names) {
        paths.add(Paths.get(pathName,name));
      }
      return paths;
    }
    finally {
      closeFile(irodsObject);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getPathsInDir-irods: " + pathName,start);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#readFileContents(Path)
   */
  @Override
  public String readFileContents(Path file) throws WdkModelException {
    long start = System.currentTimeMillis();
    String pathName = file.toString();
    IRODSFile irodsFile = null;
    StringBuilder output = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);
      try(BufferedReader buffer = new BufferedReader(new IRODSFileReader(irodsFile, fileFactory))) { 
        String s = "";
        output = new StringBuilder();
        while((s = buffer.readLine()) != null) { 
          output.append(s);
        }
        return output.toString();
      }  
    }
    catch(IOException ioe) {
      throw new WdkModelException(ioe);
    }
    finally {
      closeFile(irodsFile);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","readFile-irods (orig): " + file.toString(),start);
    }  
  }

  @SuppressWarnings("unused")
  private String readFileContents2(Path file) throws WdkModelException {
    long start = System.currentTimeMillis();
    String irodsFileName = file.getFileName().toString();
    Path localPath = null;
    Path temporaryPath = null;
    try {
      temporaryPath = IoUtil.createOpenPermsTempDir(Paths.get(_wdkTempDirName), "irods_");
      localPath = Paths.get(temporaryPath.toString(), irodsFileName);
      DataTransferOperations dataXferOps = getDataTransferOperations();
      dataXferOps.getOperation(file.toString(), localPath.toString(), "", null, null);
      StringBuilder output = new StringBuilder();
      try (BufferedReader reader = Files.newBufferedReader(localPath, Charset.defaultCharset())) {
        String line = null;
        while ((line = reader.readLine()) != null) output.append(line);
      }
      catch (IOException ex) {
        throw new WdkModelException(ex);
      }
      return output.toString();
    }
    catch(JargonException | IOException e) {
      throw new WdkModelException(e);
    }
    finally {
      if(localPath != null) {
        try {
          Files.delete(localPath);
          Files.delete(temporaryPath);
        }
        catch (IOException e) {
          throw new WdkModelException(e);
        }
      }
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","readFile-irods: " + file.toString(),start);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#isDirectory(Path)
   */
  @Override
  public boolean isDirectory(Path path) throws WdkModelException {
	long start = System.currentTimeMillis();
    String pathName = path.toString();
    IRODSFile irodsObject = null;
    try {   
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsObject = getIrodsFile(fileFactory, pathName);
      return irodsObject.isDirectory();
    }
    finally {
      closeFile(irodsObject);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","isDirectory-irods: " + pathName,start);
    }
  }
  

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#writeFile(Path, String)
   */
  @Override
  public void writeFile(Path path, String contents, boolean errorIfTargetExists) throws WdkModelException {
	long start = System.currentTimeMillis();
    String pathName = path.toString();
    String tempPathName = path.getParent().toString() + "/temp." + Long.toString(System.currentTimeMillis());
    IRODSFile irodsTempFile = null;
    IRODSFile irodsFile = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsTempFile = getIrodsFile(fileFactory, tempPathName);
      irodsFile = getIrodsFile(fileFactory, pathName);
      // Looks like writer does not truncate any existing file content.  So we can just
      // delete and let the writer re-create it.
      if(irodsTempFile.exists()) {
        irodsTempFile.delete();
      }
      if(irodsFile.exists()) {
        if(errorIfTargetExists) {
          throw new WdkModelException("The file to be written, " + pathName + ", already exists.");
        }
        irodsFile.delete();
      }
      try(
       IRODSFileWriter writer = new IRODSFileWriter(irodsTempFile, fileFactory);
       BufferedWriter buffer = new BufferedWriter(writer);)
      {
        buffer.write(contents);
      }
      catch(IOException ioe) {
        throw new WdkModelException(ioe);
      }
      finally {
        closeFile(irodsTempFile);
      }
      DataTransferOperations dataXferOps = getDataTransferOperations();
      dataXferOps.move(tempPathName, pathName);
    }
    catch(JargonException je) {
      throw new WdkModelException(je);
    }
    finally {
      closeFile(irodsFile);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","writeFile-irods: " + pathName,start);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#createDirectory(Path)
   */
  @Override
  public void createDirectory(Path dir) throws WdkModelException {
	long start = System.currentTimeMillis();
	String pathName = dir.toString();
	IRODSFile irodsObject = null;
	try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsObject = getIrodsFile(fileFactory, pathName); 
      if(irodsObject.exists()) {
        throw new WdkModelException("The directory " + pathName + " already exists.");
      }
      irodsObject.mkdir();
	}
	finally {
	  closeFile(irodsObject);
	  QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","createDirectory-irods: " + pathName,start);
	}
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#deleteFileOrDirectory(Path)
   */
  @Override
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException { 
	long start = System.currentTimeMillis();
	String pathName = fileOrDir.toString();
	IRODSFile irodsObject = null;
	try {
	  IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsObject = getIrodsFile(fileFactory, pathName);
      if(!irodsObject.exists()) {
        throw new WdkModelException("The object " + pathName + " does not exist.");
      }
      // deleting even non-empty directories
      // need the force option because a plain old delete() will not trigger a delete PEP.
      irodsObject.deleteWithForceOption();
	}
	finally {
	  closeFile(irodsObject);
	  QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","deleteFileOrDirectory-irods: " + pathName,start);
	}
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#getModificationTime(Path)
   */
  @Override
  public Long getModificationTime(Path fileOrDir) throws WdkModelException {
	long start = System.currentTimeMillis();
    String pathName = fileOrDir.toString();
    IRODSFile irodsFile = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);
      return irodsFile.lastModified();
    }
    finally {
      closeFile(irodsFile);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getModificationTime-irods: " + pathName,start);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#readSingleLineFile(Path)
   */
  @Override
  public String readSingleLineFile(Path file) throws WdkModelException {
	long start = System.currentTimeMillis();
    String pathName = file.toString();
    IRODSFile irodsFile = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName); 
      try(
       IRODSFileReader reader = new IRODSFileReader(irodsFile, fileFactory);
       BufferedReader buffer = new BufferedReader(reader);) { 
        return buffer.readLine();
      }
    }
    catch(IOException ioe) {
      throw new WdkModelException(ioe);
    }
    finally {
      closeFile(irodsFile);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","readSingleLineFile-irods: " + pathName,start);
    }
  }

  /**
   * Returns true if either a collection or data object exists for the path provided and
   * false otherwise.
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#fileExists(Path)
   */
  @Override
  public boolean fileExists(Path path) throws WdkModelException {
	long start = System.currentTimeMillis();
    String pathName = path.toString();
    IRODSFile irodsFile = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);
      return irodsFile.exists();
    }
    finally {
      closeFile(irodsFile);	
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","fileExists-irods: " + pathName,start);
    }
  }
  
  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#writeEmptyFile(Path)
   */
  @Override
  public void writeEmptyFile(Path file) throws WdkModelException {
	long start = System.currentTimeMillis();
    String pathName = file.toString();
    IRODSFile irodsFile = null;
    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);
      if(!irodsFile.exists()) {
        irodsFile.createNewFile();
      }  
    }
    catch(IOException ioe) {
      throw new WdkModelException(ioe);
    }
    finally {
      closeFile(irodsFile);
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","writeEmptyFile-irods: " + pathName,start);
    }  
  }

  /**
   * Finds the value for the IRODS user dataset store id.  This is an attribute
   * found on the root directory.  
   * @param dir
   * @param attrKey
   * @return
   */
  @Override
  public String findUserDatasetStoreId(Path userRootDir) throws WdkModelException {
	long start = System.currentTimeMillis();
    if(userRootDir == null) {
      throw new WdkModelException("No user root directory provided.");
    }
    String pathName = userRootDir.toString().substring(0, userRootDir.toString().lastIndexOf('/'));
    try {
      IRODSAccessObjectFactory objectFactory = getAccessObjectFactory();
      CollectionAO collection = objectFactory.getCollectionAO(account);
      List<MetaDataAndDomainData> metadata = collection.findMetadataValuesForCollection(pathName);
      String id = null;
      for(MetaDataAndDomainData item : metadata) {
        if(IRODS_ID_ATTRIBUTE.equals(item.getAvuAttribute())) {
          id = item.getAvuValue();
        }
      }
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","findUserDatasetStoreId-irods: " + id,start);
      return id;
	}
	catch (JargonException | JargonQueryException je) {
	  throw new WdkModelException(je);
	}
  }
  
  /**
   * Runs a simple IRODS query and returns the first column of results as a list of strings.  Only
   * 1000 results maximum are returned.  Not all that flexible a method as it serves a single purpose
   * currently.
   * @param queryString - IRODS genQuery string
   * @return - listing of first column of results
   * @throws WdkModelException
   */
  public List<String> executeIrodsQuery(String queryString) throws WdkModelException {
	long start = System.currentTimeMillis();
	List<String> results = new ArrayList<>();  
	try {
	  IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 1000);
      IRODSGenQueryExecutor irodsGenQueryExecutor = accessObjectFactory.getIRODSGenQueryExecutor(account);
    	  IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery,0);
    	  for(IRODSQueryResultRow row : resultSet.getResults()) {
        results.add(row.getColumn(0));
    	  }
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","executeIrodsQuery-irods: " + queryString, start);
    	  return results;
	}
	catch(JargonException | JargonQueryException je) {
	  throw new WdkModelException(je);
	}
  }
  
  public String getWdkTempDirName() {
    return _wdkTempDirName;
  }
  
  public TransferControlBlock getTranferControlBlock() throws WdkModelException {
    try {  
      return accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
    }
    catch(JargonException je) {
    	  throw new WdkModelException(je);
    }
  }
  
}