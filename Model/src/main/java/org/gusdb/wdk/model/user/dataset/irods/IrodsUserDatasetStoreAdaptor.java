package org.gusdb.wdk.model.user.dataset.irods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.transfer.TransferControlBlock;

/**
 * IRODS adaptor class provides an IRODS file system implementation of methods for user
 * dataset manipulation.  The adaptor uses a single set of credentials for access.
 * @author crisl-adm
 *
 */
public class IrodsUserDatasetStoreAdaptor implements JsonUserDatasetStoreAdaptor {
  private static IRODSFileSystem system;
  private static IRODSAccount account;
  private static IRODSFileFactory factory;
  
  /**
   * This method returns the IRODSFileSystem object instance variable if it is populated.  Otherwise
   * it creates a new instance to be used subsequently by all IRODS methods.
   * @return - IRODSFileSystem instance
   * @throws WdkModelException
   */
  private static IRODSFileSystem getSystem() throws WdkModelException {
    if(system == null) {
      try {
        system = IRODSFileSystem.instance();
      }
      catch(JargonException je) {
        throw new WdkModelException("Cannot initialize IRODS file system: " + je);
      }
    }
    return system;
  }
  
  /**
   * Provides the IRODS file factory object
   * @return - IRODSFileFactory object
   */
  public static IRODSFileFactory getFactory() {
	return factory;
  }

  /**
   * Provides the IRODS data transfer operation object
   * @return - DataTransferOperations
   * @throws WdkModelException
   */
  public static DataTransferOperations getDataTransferOperations() throws WdkModelException {
	try {
	  return system.getIRODSAccessObjectFactory().getDataTransferOperations(account);
	}
	catch (JargonException je) {
	  throw new WdkModelException("Unable to obtain data transfer operations object " + je);
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
  private static IRODSAccount initializeAccount(String host, int port, String user, String password, String zone, String resource) throws WdkModelException {
    if(account == null) {
      String homeDir = "/" + zone + "/home/" + user;
      account = new IRODSAccount(host,port,user,password,homeDir,zone,resource);
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
    getSystem();
    initializeAccount(host, port, user, pwd, zone, resource);
    try {
      IRODSAccessObjectFactory accessObjectFactory = system.getIRODSAccessObjectFactory();
      factory = accessObjectFactory.getIRODSFileFactory(account);
    }
    catch(JargonException je) {
      throw new WdkModelException("Problem creating IRODS file factory. - " + je);
    }
  }
  

  /**
   * Convenience method to re-factor out common code for many IRODS methods that result in
   * the return of a IRODSFile.  Note that Jargon does not throw an exception if the result
   * does not point to a collection/data object.
   * @param pathName
   * @return
   * @throws WdkModelException
   */
  public static IRODSFile getFile(String pathName) throws WdkModelException {
    try {
      return factory.instanceIRODSFile(pathName);
    }
    catch(JargonException je) {
      throw new WdkModelException("Problem accessing IRODS collection/data object. - " + je);
    }
  }
  
  /**
   * Convenience method to close the IRODSFile.  This class does not implement Autoclosable.
   * @param file - IRODSFile object
   * @throws WdkModelException
   */
  protected static void closeFile(IRODSFile file) throws WdkModelException {
    if(file != null) {
      try {
        file.close();
      }
      catch(JargonException je) {
        throw new WdkModelException("Problem closing IRODS file. - " + je);
      }
    }  
  }
  
 
  @Override
  public void moveFileAtomic(Path from, Path to) throws WdkModelException {
    String fromPathName = from.toString();
    String toPathName = to.toString();
    try {
      IRODSAccessObjectFactory accessObjectFactory = system.getIRODSAccessObjectFactory();
      IRODSFile sourceFile = getFile(fromPathName);
      DataTransferOperations dataXferOps = getDataTransferOperations();
      // Resorted to this workaround because Jargon cannot move one file into an already occupied
      // location and no 'force' flag is implemented (apparently) in Jargon yet.
      TransferControlBlock tcb = accessObjectFactory.buildDefaultTransferControlBlockBasedOnJargonProperties();
      tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
      dataXferOps.copy(fromPathName, "", toPathName, null, tcb);
      sourceFile.delete();
    }
    catch(JargonException je) {
      throw new WdkModelException("Unable to transfer " + fromPathName + " to " + toPathName + ". - ", je);
    }
  }

  @Override
  public List<Path> getPathsInDir(Path dir) throws WdkModelException {
    List<Path> paths = new ArrayList<>(); 
    String pathName = dir.toString();
    IRODSFile file = getFile(pathName);
    if(!file.isDirectory()) {
      throw new WdkModelException("The path " + pathName + " does not refer to a directory.");
    }
    String[] names = file.list();
    for(String name : names) {
      paths.add(Paths.get(pathName,name));
    }
    return paths;
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#readFileContents(Path)
   */
  @Override
  public String readFileContents(Path file) throws WdkModelException {
    IRODSFile irodsFile = null;
    StringBuilder output = null;
    String pathName = file.toString();
    try {
      irodsFile = factory.instanceIRODSFile(pathName);
      try(BufferedReader buffer = new BufferedReader(new IRODSFileReader(irodsFile, factory))) { 
        String s = "";
        output = new StringBuilder();
        while((s = buffer.readLine()) != null) { 
          output.append(s);
        }
      }  
    }
    catch(JargonException | IOException ex) {
      throw new WdkModelException("Problem reading IRODS file " + ex);
    }
    finally {
      closeFile(irodsFile);
    }  
    return output.toString();
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#isDirectory(Path)
   */
  @Override
  public boolean isDirectory(Path path) throws WdkModelException {
    String pathName = path.toString();
    return getFile(pathName).isDirectory();
  }
  

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#writeFile(Path, String)
   */
  @Override
  public void writeFile(Path path, String contents, boolean errorIfTargetExists) throws WdkModelException {
    IRODSFile irodsTempFile = null;
    IRODSFile irodsFile = null;
    String pathName = path.toString();
    String tempPathName = path.getParent().toString() + "/temp." + Long.toString(System.currentTimeMillis());
    try {
      irodsTempFile = factory.instanceIRODSFile(tempPathName);
      irodsFile = factory.instanceIRODSFile(pathName);
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
       IRODSFileWriter writer = new IRODSFileWriter(irodsTempFile, factory);
       BufferedWriter buffer = new BufferedWriter(writer);)
      {
        buffer.write(contents);
      }
      catch(IOException ioe) {
        throw new WdkModelException("Problem writing IRODS file " + pathName + ". - ", ioe);
      }
      finally {
        closeFile(irodsTempFile);
      }
      DataTransferOperations dataXferOps = getDataTransferOperations();
      dataXferOps.move(tempPathName, pathName);
    }
    catch(JargonException je) {
      throw new WdkModelException("Problem writing IRODS file " + pathName + ". - ", je);
    }
    finally {
      closeFile(irodsFile);
    }

  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#createDirectory(Path)
   */
  @Override
  public void createDirectory(Path dir) throws WdkModelException {
    String pathName = dir.toString();
    IRODSFile file = getFile(pathName);
    if(file.exists()) {
      throw new WdkModelException("The directory specified already exists.");
    }
    file.mkdir();
  }


  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#deleteFileOrDirectory(Path)
   */
  @Override
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException {
    String pathName = fileOrDir.toString();
    IRODSFile irodsObject = getFile(pathName);
    if(!irodsObject.exists()) {
      throw new WdkModelException("No such file/directory exists.");
    }
    if(irodsObject.isDirectory() && irodsObject.list().length > 0) {
      throw new WdkModelException("The directory to be deleted is not empty.");
    }
    irodsObject.delete();
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#getModificationTime(Path)
   */
  @Override
  public Long getModificationTime(Path fileOrDir) throws WdkModelException {
    String pathName = fileOrDir.toString();
    IRODSFile file = getFile(pathName);
    return file.lastModified();
  }

  @Override
  public String readSingleLineFile(Path file) throws WdkModelException {
    IRODSFile irodsFile = null;
    String pathName = file.toString();
    try {
      irodsFile = getFile(pathName);
      try(
       IRODSFileReader reader = new IRODSFileReader(irodsFile, getFactory());
       BufferedReader buffer = new BufferedReader(reader);) { 
        return buffer.readLine();
      }  
    }
    catch(IOException ex) {
      throw new WdkModelException("Problem reading IRODS file " + ex);
    }
    finally {
      closeFile(irodsFile);
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
    String pathName = path.toString();
    IRODSFile irodsFile = getFile(pathName);
    return irodsFile.exists();
  }
  
  @Override
  public void writeEmptyFile(Path file) throws WdkModelException {
    IRODSFile irodsFile = null;
    String pathName = file.toString();
    try {
      IRODSAccessObjectFactory accessObjectFactory = system.getIRODSAccessObjectFactory();
      IRODSFileFactory irodsFileFactory = accessObjectFactory.getIRODSFileFactory(account);
      irodsFile = irodsFileFactory.instanceIRODSFile(pathName);
      if(!irodsFile.exists()) {
        irodsFile.createNewFile();
      }  
    }
    catch(JargonException | IOException ex) {
      throw new WdkModelException("Problem creating IRODS file. ", ex);
    }
    finally {
      closeFile(irodsFile);
    }  
  }
  
  /**
   * The connection to the IRODS system is closed much as a DB connection would be.
   */
  public void close() {
	try {
	  if(system != null) {
		system.close();
	  }
	}
	catch(JargonException je) {
	  throw new RuntimeException("Problem closing IRODS connection. ", je);
	}
  }

}