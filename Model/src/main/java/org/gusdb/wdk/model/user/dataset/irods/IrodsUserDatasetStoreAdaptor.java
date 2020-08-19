package org.gusdb.wdk.model.user.dataset.irods;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.TraceLog;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatAdaptor;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatNode;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.io.*;
import org.irods.jargon.core.query.*;
import org.irods.jargon.core.transfer.TransferControlBlock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * iRODS adaptor class provides an iRODS file system implementation of methods
 * for user dataset manipulation.
 * <p>
 * The adaptor uses a single set of credentials for access.
 *
 * @author crisl-adm
 */
public class IrodsUserDatasetStoreAdaptor implements UserDatasetStoreAdaptor {

  private static final String IRODS_ID_ATTRIBUTE = "irods_id";
  private static final String LOG_QUERY_NAME = "SUMMARY OF IRODS CALL";
  private static final TraceLog TRACE = new TraceLog(IrodsUserDatasetStoreAdaptor.class);

  private static IRODSAccount account;

  private Path _wdkTempDir;

  private ICatAdaptor iCatAdaptor;

  private IRODSAccessObjectFactory iFactory;

  IrodsUserDatasetStoreAdaptor(Path wdkTempDir) {
    TRACE.start(wdkTempDir);
    _wdkTempDir = wdkTempDir;
    TRACE.end();
  }

  @Override
  public void moveFileAtomic(Path from, Path to) throws WdkModelException {
    TRACE.start(from, to);

    long start = System.currentTimeMillis();
    String fromPathName = from.toString();
    String toPathName = to.toString();
    IRODSFile sourceFile = null;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      sourceFile = getIrodsFile(fileFactory, fromPathName);
      DataTransferOperations dataXferOps = getDataTransferOperations();

      // Resorted to this workaround because Jargon cannot move one file into an
      // already occupied location and no 'force' flag is implemented
      // (apparently) in Jargon yet.
      TransferControlBlock tcb = getTransferControlBlock();

      tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
      dataXferOps.copy(fromPathName, "", toPathName, null, tcb);
      sourceFile.delete();
    } catch(JargonException je) {
      throw new WdkModelException(je);
    } finally {
      closeFile(sourceFile);
      queryLog("moveFileAtomic-irods: " + fromPathName,start);
    }
    TRACE.end();
  }

  @Override
  public List<Path> getPathsInDir(Path dir) throws WdkModelException {
    return TRACE.start(dir).end(time(() -> getICatAdaptor()
      .fetchShallowCollection(dir)
      .map(c -> c.streamShallow()
        .map(ICatNode::getPath)
        .collect(Collectors.toList()))
      .orElse(Collections.emptyList()), "getPathsInDir-irods: " + dir));
  }

  @Override
  public String readFileContents(Path file) throws WdkModelException {
    TRACE.start(file);

    long start = System.currentTimeMillis();
    String pathName = file.toString();
    IRODSFile irodsFile = null;
    StringBuilder output;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);
      try(BufferedReader buffer = new BufferedReader(new IRODSFileReader(irodsFile, fileFactory))) {
        String s;
        output = new StringBuilder();
        while((s = buffer.readLine()) != null) {
          output.append(s);
        }
        return output.toString();
      }
    } catch(IOException ioe) {
      throw new WdkModelException(ioe);
    } finally {
      closeFile(irodsFile);
      queryLog("readFile-iRODS (orig): " + file.toString(),start);
      TRACE.end("<omitted file contents from log>");
    }
  }

  @Override
  public boolean isDirectory(Path path) throws WdkModelException {
    return TRACE.start(path).end(time(() -> getICatAdaptor()
      .fetchNodeAt(path)
      .map(ICatNode::isCollection)
      .orElse(false), "isDirectory-irods: " + path));
  }

  @Override
  public void writeFile(Path path, String contents, boolean errorIfTargetExists) throws WdkModelException {
    TRACE.start(path, contents, errorIfTargetExists);

    long start = System.currentTimeMillis();
    String pathName = path.toString();
    String tempPathName = path.getParent().toString() + "/temp." + System.currentTimeMillis();
    IRODSFile irodsTempFile;
    IRODSFile irodsFile = null;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsTempFile = getIrodsFile(fileFactory, tempPathName);
      irodsFile = getIrodsFile(fileFactory, pathName);

      // Looks like writer does not truncate any existing file content.  So we can just
      // delete and let the writer re-create it.
      if(irodsTempFile.exists())
        irodsTempFile.delete();

      if(irodsFile.exists()) {
        if(errorIfTargetExists)
          throw new WdkModelException(
            "The file to be written, " + pathName + ", already exists.");

        irodsFile.delete();
      }

      try(
        IRODSFileWriter writer = new IRODSFileWriter(irodsTempFile, fileFactory);
        BufferedWriter buffer = new BufferedWriter(writer)
      ) {
        buffer.write(contents);
      } catch(IOException ioe) {
        throw new WdkModelException(ioe);
      } finally {
        closeFile(irodsTempFile);
      }

      DataTransferOperations dataXferOps = getDataTransferOperations();
      dataXferOps.move(tempPathName, pathName);
    } catch(JargonException je) {
      throw new WdkModelException(je);
    } finally {
      closeFile(irodsFile);
      queryLog("writeFile-irods: " + pathName,start);
      TRACE.end();
    }
  }

  @Override
  public void createDirectory(Path dir) throws WdkModelException {
    TRACE.start(dir);

    long start = System.currentTimeMillis();
    String pathName = dir.toString();
    IRODSFile irodsObject = null;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsObject = getIrodsFile(fileFactory, pathName);

      if(irodsObject.exists())
        throw new WdkModelException(
          "The directory " + pathName + " already exists.");

      irodsObject.mkdir();
    } finally {
      closeFile(irodsObject);
      queryLog("createDirectory-irods: " + pathName,start);
    }
    TRACE.end();
  }

  @Override
  public void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException {
    TRACE.start(fileOrDir);

    long start = System.currentTimeMillis();
    String pathName = fileOrDir.toString();
    IRODSFile irodsObject = null;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsObject = getIrodsFile(fileFactory, pathName);

      if(!irodsObject.exists())
        throw new WdkModelException(
          "The object " + pathName + " does not exist.");

      // deleting even non-empty directories
      // need the force option because a plain old delete() will not trigger a delete PEP.
      irodsObject.deleteWithForceOption();
    } finally {
      closeFile(irodsObject);
      queryLog("deleteFileOrDirectory-irods: " + pathName, start);
    }
    TRACE.end();
  }

  @Override
  public Long getModificationTime(Path fileOrDir) throws WdkModelException {
    return TRACE.start(fileOrDir).end(time(() -> getICatAdaptor()
      .fetchNodeAt(fileOrDir)
      .map(ICatNode::getLastModified)
      .orElse(0L), "getModificationTime-irods: " + fileOrDir));
  }

  @Override
  public String readSingleLineFile(Path file) throws WdkModelException {
    TRACE.start(file);
    long start = System.currentTimeMillis();
    String pathName = file.toString();
    IRODSFile irodsFile = null;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);

      try(
        IRODSFileReader reader = new IRODSFileReader(irodsFile, fileFactory);
        BufferedReader buffer = new BufferedReader(reader)
      ) {
        return TRACE.end(buffer.readLine());
      }
    } catch(IOException ioe) {
      throw new WdkModelException(ioe);
    } finally {
      closeFile(irodsFile);
      queryLog("readSingleLineFile-irods: " + pathName,start);
    }
  }

  /**
   * Returns true if either a collection or data object exists for the path
   * provided and false otherwise.
   */
  @Override
  public boolean fileExists(Path path) throws WdkModelException {
    return TRACE.start(path).end(time(() ->
      getICatAdaptor().fetchNodeAt(path).isPresent(),
      "fileExists-iRODS: " + path));
  }

  /**
   * Note that this method makes no use of iRODS POSIX FS mimics as it appears
   * that creating the empty file directly in iRODS (e.g., via
   * {@link IRODSFile#createNewFile()}) does not initiate a replication.
   * Only an {@code iput} (the putOperation here) will do that, it appears.
   * That requires us to use temporary POSIX stores.
   */
  @Override
  public void writeEmptyFile(Path file) throws WdkModelException {
    TRACE.start(file);

    long start = System.currentTimeMillis();
    String pathName = file.toString();
    IRODSFile irodsFile = null;
    Path temporaryDirPath = null;
    Path localPath = null;
    File localFile = null;

    try {
      IRODSFileFactory fileFactory = getIrodsFileFactory();
      irodsFile = getIrodsFile(fileFactory, pathName);

      if(!irodsFile.exists()) {
        temporaryDirPath = IoUtil.createOpenPermsTempDir(_wdkTempDir, "irods_");
        localPath = Paths.get(temporaryDirPath.toString(), file.getFileName().toString());
        localFile = new File(localPath.toString());
        localFile.createNewFile();
        getDataTransferOperations().putOperation(localFile, irodsFile, null, null);
      }
    } catch(IOException | JargonException e) {
      throw new WdkModelException(e);
    } finally {
      closeFile(irodsFile);
      try {
        if(temporaryDirPath != null) {
          if(localFile != null)
            Files.delete(localPath);
          Files.delete(temporaryDirPath);
        }
      } catch(IOException ioe) {
        throw new WdkModelException(ioe);
      } finally {
        queryLog("writeEmptyFile-irods: " + pathName,start);
      }
    }
    TRACE.end();
  }

  /**
   * Finds the value for the iRODS user dataset store id.
   * <p>
   * This is an attribute found on the root directory.
   */
  @Override
  public String findUserDatasetStoreId(Path userRootDir) throws WdkModelException {
    TRACE.start(userRootDir);

    long start = System.currentTimeMillis();

    if(userRootDir == null)
      throw new WdkModelException("No user root directory provided.");

    String pathName = userRootDir.toString().substring(0, userRootDir.toString().lastIndexOf('/'));

    try {
      CollectionAO collection = getIrodsAccessFac().getCollectionAO(account);
      List<MetaDataAndDomainData> metadata = collection.findMetadataValuesForCollection(pathName);
      String id = null;

      for(MetaDataAndDomainData item : metadata)
        if (IRODS_ID_ATTRIBUTE.equals(item.getAvuAttribute()))
          id = item.getAvuValue();

      queryLog("findUserDatasetStoreId-irods: " + id,start);

      return TRACE.end(id);
    } catch (JargonException | JargonQueryException je) {
      throw new WdkModelException(je);
    }
  }

  public void close() {
    TRACE.start();
    if (iFactory != null) {
      TRACE.log("Closing iRODS Session");
      iFactory.closeSessionAndEatExceptions();
    }
    TRACE.end();
  }

  /**
   * Convenience method to close the {@link IRODSFile}.
   *
   * This class does not implement {@link AutoCloseable}.
   *
   * @param file
   *   IRODSFile object
   */
  void closeFile(IRODSFile file) throws WdkModelException {
    TRACE.start(file);
    if(file != null) {
      try {
        file.close();
      } catch(JargonException je) {
        throw new WdkModelException("Problem closing IRODS file. - " + je);
      }
    }
    TRACE.end();
  }

  /**
   * Runs a simple iRODS query and returns the first column of results as a list
   * of strings.
   * <p>
   * Only 1000 results maximum are returned.  Not all that flexible a method as
   * it serves a single purpose currently.
   *
   * @param queryString
   *   iRODS genQuery string
   *
   * @return listing of first column of results
   */
  List<String> executeIrodsQuery(String queryString) throws WdkModelException {
    TRACE.start(queryString);
    long start = System.currentTimeMillis();
    List<String> results = getICatAdaptor().singleColumnQuery(queryString, 1000);
    queryLog("executeIrodsQuery-irods: " + queryString, start);
    return TRACE.end(results);
  }

  /**
   * Convenience method to retrieve the iRODS data transfer operations object
   * (hiding account)
   *
   * @return the iRODS data transfer operations object
   */
  DataTransferOperations getDataTransferOperations() throws WdkModelException {
    TRACE.start();
    try {
      return TRACE.end(getIrodsAccessFac().getDataTransferOperations(account));
    } catch(JargonException je) {
      throw new WdkModelException(je);
    }
  }

  /**
   * Convenience method to retrieve the iRODS object given the path name
   *
   * @param fileFactory
   *   iRODS file factory
   *
   * @return the iRODS file
   */
  IRODSFile getIrodsFile(IRODSFileFactory fileFactory, String pathName) throws WdkModelException {
    TRACE.start(fileFactory, pathName);
    try {
      if (fileFactory != null) {
        return TRACE.end(fileFactory.instanceIRODSFile(pathName));
      } else {
        throw new WdkModelException("The IRODS file factory cannot be null");
      }
    } catch(JargonException je) {
      throw new WdkModelException(je);
    }
  }

  /**
   * Convenience method to retrieve the iRODS file factory (hiding account)
   *
   * @return iRODS file factory
   */
  IRODSFileFactory getIrodsFileFactory() throws WdkModelException {
    TRACE.start();
    try {
      return TRACE.end(getIrodsAccessFac().getIRODSFileFactory(account));
    } catch(JargonException je) {
      throw new WdkModelException(je);
    }
  }

  IRODSRandomAccessFile getIrodsRandomAccessFile(
    final IRODSFileFactory fileFactory,
    final String           pathName
  ) throws WdkModelException {
    TRACE.start(fileFactory, pathName);

    try {
      if(fileFactory != null) {
        return TRACE.end(fileFactory.instanceIRODSRandomAccessFile(pathName));
      } else {
        throw new WdkModelException("The IRODS file factory cannot be null");
      }
    } catch(JargonException je) {
      throw new WdkModelException(je);
    }
  }

  TransferControlBlock getTransferControlBlock() throws WdkModelException {
    TRACE.start();
    try {
      return TRACE.end(getIrodsAccessFac()
        .buildDefaultTransferControlBlockBasedOnJargonProperties());
    } catch(JargonException je) {
      throw new WdkModelException(je);
    }
  }

  /**
   * Retrieves an in memory mirror of the iRODS state as a tree of collections
   * and data objects.
   *
   * @param path
   *   path for which details should be retrieved from iRODS
   *
   * @return a tree representation of the collections and data objects in iRODS
   *   at the given path
   *
   * @throws WdkModelException see {@link ICatAdaptor#fetchFullTreeAt(Path)}
   */
  Optional<ICatCollection> readFullPath(final Path path) throws WdkModelException {
    return TRACE.start(path).end(getICatAdaptor().fetchFullTreeAt(path));
  }

  void readMetadataInto(final ICatNode node) throws WdkModelException {
    TRACE.start(node);
    getICatAdaptor().fetchMetadataInto(node);
    TRACE.end();
  }

  public void readAllMetadataInto(final ICatNode node)
  throws WdkModelException {
    TRACE.start(node);
    if (node.isObject())
      getICatAdaptor().fetchMetadataInto(node);
    else
      getICatAdaptor().fetchAllMetadataInto((ICatCollection) node);
    TRACE.end();
  }

  public CollectionAO getCollectionAccessObject() throws WdkModelException {
    try {
      return getIrodsAccessFac().getCollectionAO(account);
    } catch (JargonException e) {
      throw new WdkModelException(e);
    }
  }

  @SuppressWarnings("unused")
  private String readFileContents2(Path file) throws WdkModelException {
    TRACE.start(file);

    long start = System.currentTimeMillis();
    String irodsFileName = file.getFileName().toString();
    Path localPath = null;
    Path temporaryPath = null;

    try {
      temporaryPath = IoUtil.createOpenPermsTempDir(_wdkTempDir, "irods_");
      localPath = Paths.get(temporaryPath.toString(), irodsFileName);
      DataTransferOperations dataXferOps = getDataTransferOperations();
      dataXferOps.getOperation(file.toString(), localPath.toString(), "", null, null);
      StringBuilder output = new StringBuilder();

      try (
        BufferedReader reader = Files.newBufferedReader(localPath, Charset.defaultCharset())
      ) {
        String line;
        while ((line = reader.readLine()) != null)
          output.append(line);
      } catch (IOException ex) {
        throw new WdkModelException(ex);
      }

      return output.toString();
    } catch(JargonException | IOException e) {
      throw new WdkModelException(e);
    } finally {
      if(localPath != null) {
        try {
          Files.delete(localPath);
          Files.delete(temporaryPath);
        } catch (IOException e) {
          throw new WdkModelException(e);
        }
      }
      queryLog("readFile-irods: " + file.toString(),start);
      TRACE.end("<omitted file contents from log>");
    }
  }

  private ICatAdaptor getICatAdaptor() throws WdkModelException {
    TRACE.start();
    if (iCatAdaptor == null) {
      TRACE.log("instantiating new ICatAdaptor");
      try {
        iCatAdaptor = new ICatAdaptor(getIrodsAccessFac()
          .getIRODSGenQueryExecutor(account));
      } catch (JargonException e) {
        throw new WdkModelException(e);
      }
    }
    return TRACE.end(iCatAdaptor);
  }

  private IRODSAccessObjectFactory getIrodsAccessFac()
  throws WdkModelException {
    TRACE.start();
    if (iFactory == null) {
      TRACE.log("Instantiating new iRODS Access Object factory");
      try {
        iFactory = IRODSFileSystem.instance().getIRODSAccessObjectFactory();
      } catch (JargonException e) {
        throw new WdkModelException(e);
      }
    }
    return TRACE.end(iFactory);
  }

  /**
   * Convenience method to create system and account instance variables for
   * IRODS if either is not already created.
   */
  static void initializeIrods(
    final String host,
    final int port,
    final String user,
    final String pwd,
    final String zone,
    final String resource
  ) throws WdkModelException {
    TRACE.start(host, port, user, "***", zone, resource);
    initializeAccount(host, port, user, pwd, zone, resource);
    TRACE.end();
  }

  /**
   * This method returns the IRODSAccount object instance variable if populated.
   * Otherwise, it uses the method args to create a new IRODSAccount to be used
   * subsequently by all iRODS methods.
   */
  private static void initializeAccount(
    final String host,
    final int    port,
    final String user,
    final String password,
    final String zone,
    final String resource
  ) throws WdkModelException {
    TRACE.start(host, port, user, "***", zone, resource);
    if (account == null) {
      try {
        ClientServerNegotiationPolicy csnp = new ClientServerNegotiationPolicy();
        csnp.setSslNegotiationPolicy(SslNegotiationPolicy.CS_NEG_REQUIRE);
        String homeDir = "/" + zone + "/home/" + user;
        account  = IRODSAccount.instance(host, port, user, password, homeDir,
          zone, resource, csnp);
      } catch(JargonException je) {
        throw new WdkModelException(je);
      }
    }
    TRACE.end();
  }

  // TODO: Replace me with a checked function once strategy loading branch is
  //   merged in
  private interface Task<T> { T run() throws WdkModelException; }

  private static <T> T time(final Task<T> task, final String message)
  throws  WdkModelException {
    final long start = System.currentTimeMillis();
    final T    val   = task.run();
    queryLog(message, start);
    return val;
  }

  private static void queryLog(final String val, final long start) {
    QueryLogger.logEndStatementExecution(LOG_QUERY_NAME, val, start);
  }
}
