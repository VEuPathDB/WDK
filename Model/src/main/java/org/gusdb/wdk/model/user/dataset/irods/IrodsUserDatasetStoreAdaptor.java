package org.gusdb.wdk.model.user.dataset.irods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;
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
public class IrodsUserDatasetStoreAdaptor implements UserDatasetStoreAdaptor {
  private static IRODSFileSystem system;
  private static IRODSAccount account;
  
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
  }
  
  /**
   * Convenience method to retrieve the IRODS access object factory (hidding system)
   * @return - the IRODS access object factory
   * @throws WdkModelException
   */
  public static IRODSAccessObjectFactory getIrodsAccessObjectFactory() throws WdkModelException {
	try {  
	  return system.getIRODSAccessObjectFactory();
	}
	catch(JargonException je) {
	  throw new WdkModelException(je);
	}
  }
  
  
  /**
   * Convenience method to retrieve the IRODS file factory (hiding account)
   * @param accessObjectFactory
   * @return - IRODS file factory
   * @throws WdkModelException
   */
  public static IRODSFileFactory getIrodsFileFactory(IRODSAccessObjectFactory accessObjectFactory) throws WdkModelException {
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
  public static DataTransferOperations getDataTransferOperations(IRODSAccessObjectFactory accessObjectFactory) throws WdkModelException {
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
  public static IRODSFile getIrodsFile(IRODSFileFactory fileFactory, String pathName) throws WdkModelException {
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
  
  /**
   * Convenience method to close the IRODSFile.  This class does not implement Autoclosable.
   * @param file - IRODSFile object
   * @throws WdkModelException
   */
  public static void closeFile(IRODSFile file) throws WdkModelException {
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
   * @param factory - The IRODS access object factory
   */
  public static void closeSession(IRODSAccessObjectFactory accessObjectFactory) {
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
    String fromPathName = from.toString();
    String toPathName = to.toString();
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile sourceFile = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
      sourceFile = getIrodsFile(fileFactory, fromPathName);
      DataTransferOperations dataXferOps = getDataTransferOperations(accessObjectFactory);
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
      closeSession(accessObjectFactory);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#getPathsInDir(Path)
   */
  @Override
  public List<Path> getPathsInDir(Path dir) throws WdkModelException {
	String pathName = dir.toString();
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsObject = null;
    List<Path> paths = new ArrayList<>(); 
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
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
      closeSession(accessObjectFactory);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#readFileContents(Path)
   */
  @Override
  public String readFileContents(Path file) throws WdkModelException {
	String pathName = file.toString();
	IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsFile = null;
    StringBuilder output = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
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
      closeSession(accessObjectFactory);
    }  
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#isDirectory(Path)
   */
  @Override
  public boolean isDirectory(Path path) throws WdkModelException {
    String pathName = path.toString();
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsObject = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();     
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
      irodsObject = getIrodsFile(fileFactory, pathName);
      return irodsObject.isDirectory();
    }
    finally {
      closeFile(irodsObject);
      closeSession(accessObjectFactory);
    }
  }
  

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#writeFile(Path, String)
   */
  @Override
  public void writeFile(Path path, String contents, boolean errorIfTargetExists) throws WdkModelException {
    String pathName = path.toString();
    String tempPathName = path.getParent().toString() + "/temp." + Long.toString(System.currentTimeMillis());
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsTempFile = null;
    IRODSFile irodsFile = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
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
      DataTransferOperations dataXferOps = getDataTransferOperations(accessObjectFactory);
      dataXferOps.move(tempPathName, pathName);
    }
    catch(JargonException je) {
      throw new WdkModelException(je);
    }
    finally {
      closeFile(irodsFile);
      closeSession(accessObjectFactory);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#createDirectory(Path)
   */
  @Override
  public void createDirectory(Path dir) throws WdkModelException { 
	String pathName = dir.toString();
	IRODSAccessObjectFactory accessObjectFactory = null;
	IRODSFile irodsObject = null;
	try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
      irodsObject = getIrodsFile(fileFactory, pathName); 
      if(irodsObject.exists()) {
        throw new WdkModelException("The directory " + pathName + " already exists.");
      }
      irodsObject.mkdir();
	}
	finally {
	  closeFile(irodsObject);
	  closeSession(accessObjectFactory);
	}
  }


  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#deleteFileOrDirectory(Path)
   */
  @Override
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException { 
	String pathName = fileOrDir.toString();
	IRODSAccessObjectFactory accessObjectFactory = null;
	IRODSFile irodsObject = null;
	try {
	  accessObjectFactory = getIrodsAccessObjectFactory();
	  IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
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
	  closeSession(accessObjectFactory);
	}
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#getModificationTime(Path)
   */
  @Override
  public Long getModificationTime(Path fileOrDir) throws WdkModelException {
    String pathName = fileOrDir.toString();
    IRODSFile irodsFile = null;
    IRODSAccessObjectFactory accessObjectFactory = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
      irodsFile = getIrodsFile(fileFactory, pathName);
      return irodsFile.lastModified();
    }
    finally {
      closeFile(irodsFile);
      closeSession(accessObjectFactory);
    }
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#readSingleLineFile(Path)
   */
  @Override
  public String readSingleLineFile(Path file) throws WdkModelException {
    String pathName = file.toString();
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsFile = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
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
      closeSession(accessObjectFactory);
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
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsFile = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
      irodsFile = getIrodsFile(fileFactory, pathName);
      return irodsFile.exists();
    }
    finally {
      closeFile(irodsFile);	
      closeSession(accessObjectFactory);
    }
  }
  
  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStoreAdaptor#writeEmptyFile(Path)
   */
  @Override
  public void writeEmptyFile(Path file) throws WdkModelException {
    String pathName = file.toString();
    IRODSAccessObjectFactory accessObjectFactory = null;
    IRODSFile irodsFile = null;
    try {
      accessObjectFactory = getIrodsAccessObjectFactory();
      IRODSFileFactory fileFactory = getIrodsFileFactory(accessObjectFactory);
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
      closeSession(accessObjectFactory);
    }  
  }
  
  /**
   * Shutdown of IRODS
   */
  public void close() {
	try {
	  if(system != null) {
		system.close();
	  }
	}
	catch(JargonException je) {
	  throw new RuntimeException("Problem closing IRODS. ", je);
	}
  }

}