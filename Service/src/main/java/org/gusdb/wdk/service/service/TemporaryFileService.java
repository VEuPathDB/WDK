package org.gusdb.wdk.service.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

/*
 * We need to write a utility that provides the file and meta-data via uuid lookup to other services if the
 * file is still available. File is still available IFF the session still contains data for the uuid,
 * date-uploaded is later than current time minus some threshold, and file still exists in wdk-tmp. If file is
 * no longer available but is still present, we should manually delete from tmp and session.
 */
@Path("/temporary-file")
public class TemporaryFileService extends AbstractWdkService {
  
  public final static String TEMP_FILE_NAMES = "tempFileNames";  // for use in session

  /*
   * Take file stream from request and write to WdkTempFile directory.
   * Return the generated temp file name in the head.er
   * Also push the temp file name into the session, for other service calls to use.
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response buildResultFromForm(@FormDataParam("file") InputStream fileInputStream)
      throws WdkModelException {
    
    java.nio.file.Path tempDirPath = getWdkModel().getModelConfig().getWdkTempDir();
    java.nio.file.Path tempFilePath;
    
    try {
      tempFilePath = Files.createTempFile(tempDirPath, null, null);

      try (OutputStream outputStream = Files.newOutputStream(tempFilePath);) {
        IoUtil.transferStream(outputStream, fileInputStream);
      }
      catch (IOException e) {
        throw new WdkModelException(e);
      }
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
    java.nio.file.Path tempFileName = tempFilePath.getFileName();
    addTempFileToSession(tempFileName.toString());
    return Response.noContent().header("ID", tempFileName.toString()).build();
  }
  
  private void addTempFileToSession(String tempFileName) {
    HttpSession session = getSession();
    @SuppressWarnings("unchecked")
    Set<String> tempFilesInSession = (Set<String>)(session.getAttribute(TEMP_FILE_NAMES));
    if (tempFilesInSession == null) {
      tempFilesInSession = Collections.synchronizedSet(new HashSet<String>());
    }
    tempFilesInSession.add(tempFileName.toString());
    session.setAttribute(TEMP_FILE_NAMES, tempFilesInSession);
  }
  
  /*
   * Deleting a wdk temp file is always optional, as by design they are purged asynchronously on occasion.
   * But, if a client or test knows the file is no longer needed, it can use this endpoint.
   */
  @DELETE
  @Path("/{id}")
  public Response deleteTempFile(@PathParam("id") String tempFileName) throws WdkModelException {
    
    Optional<java.nio.file.Path> optPath = getTempFileFromSession(tempFileName);
    
    java.nio.file.Path path = optPath.orElseThrow(() -> new NotFoundException(
        "Temporary file with ID " + tempFileName + " is not found in this user's session"));
    
    HttpSession session = getSession();
    @SuppressWarnings("unchecked")
    Set<String> tempFilesInSession = (Set<String>)(session.getAttribute(TEMP_FILE_NAMES));
    if (tempFilesInSession != null) {
      tempFilesInSession.remove(tempFileName.toString());
      session.setAttribute(TEMP_FILE_NAMES, tempFilesInSession);
    }
    
    try {
      Files.deleteIfExists(path);
    }
    catch (IOException e) {
      throw new WdkModelException(e);
    }
    return Response.noContent().build();
  }

  /**
   * Returns a factory function that can look up temporary files by name.  An optional of path is returned;
   * if the file is not in the current session or cannot be found, an empty optional is returned.
   * 
   * @param wdkModel the WDK model
   * @param session the current user session
   * @return a factory function for looking up temporary file paths by name
   */
  public static Function<String, Optional<java.nio.file.Path>> getTempFileFactory(WdkModel wdkModel, HttpSession session) {
    java.nio.file.Path tempDirPath = wdkModel.getModelConfig().getWdkTempDir();
    return tempFileName -> {
      java.nio.file.Path tempFilePath = tempDirPath.resolve(tempFileName);
      
      Optional<java.nio.file.Path> optional = Optional.empty();
      if (Files.exists(tempFilePath)) {
        if (!Files.isReadable(tempFilePath)) {
          throw new WdkRuntimeException("WDK Temp file " + tempFilePath + " exists but is not readable");
        }
        @SuppressWarnings("unchecked")
        Set<String> tempFilesInSession = (Set<String>)(session.getAttribute(TEMP_FILE_NAMES));
        if (tempFilesInSession != null && tempFilesInSession.contains(tempFileName)) {
          optional = Optional.of(tempFilePath);
        }
      } 
      return optional;
    };
  }

  /**
   * Returns the path to a temp file that is known by the user's session (having been put there by the temp
   * file service).  If the file is not found in the session, or does not exist, return empty optional.
   */
  public Optional<java.nio.file.Path> getTempFileFromSession(String tempFileName) {
    return getTempFileFactory(getWdkModel(), getSession()).apply(tempFileName);
  }

}
