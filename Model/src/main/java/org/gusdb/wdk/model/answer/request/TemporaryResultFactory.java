package org.gusdb.wdk.model.answer.request;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

public class TemporaryResultFactory {

  private static final Logger LOG = Logger.getLogger(TemporaryResultFactory.class);

  private static final long EXPIRATION_MILLIS = 60 * 60 * 1000; // one hour

  /**
   * Inserts a temporary result configuration and returns the ID for the result
   *
   * @param userId user making the request
   * @param request request
   * @return ID for the temporary result
   */
  public static String insertTemporaryResult(User user, AnswerRequest request) {
    String id = UUID.randomUUID().toString();
    CacheMgr.get().getAnswerRequestCache().put(id, new TwoTuple<>(user, request));

    // for debug purposes, write temporary file containing the request and named after the ID
    Path tmpFilePath = Paths.get(user.getWdkModel().getModelConfig().getWdkTempDir().toString(), "wdkTempResult-" + id);
    boolean fileWritten = false;
    try (BufferedWriter out = new BufferedWriter(new FileWriter(tmpFilePath.toFile()))) {
      out.write("User: " + user.getUserId() + " (" + (user.isGuest() ? "guest" : "registered" ) + ")");
      out.newLine();
      out.write("AnswerSpec:");
      out.newLine();
      out.write(request.getAnswerSpec().get().toString());
      out.write("Reporter:");
      out.newLine();
      out.write("  Name: ");
      out.write(request.getFormatting().getFormat());
      out.newLine();
      out.write("  Config: ");
      out.write(request.getFormatting().getFormatConfig().toString());
      out.newLine();
      fileWritten = true;
    }
    catch (IOException e) {
      LOG.error("Could not write temporary result file (for debug) to " + tmpFilePath, e);
    }

    if (fileWritten) {
      try {
        IoUtil.openPosixPermissions(tmpFilePath);
      }
      catch (IOException e) {
        LOG.error("Could not open permissions on temporary result file (for debug) at " + tmpFilePath, e);
      }
    }

    return id;
  }

  /**
   * Retrieves a spec for a temporary result by ID, checking for expiration and removing
   * if expired for any reason.
   *
   * @param temporaryResultId ID of the temporary result
   * @param wdkModel WDK model
   * @return tuple of temporary result spec and the user under which it should be run
   * @throws WdkUserException if temporary result ID is expired or cannot be found
   * @throws WdkModelException if another error occurs
   */
  public static TwoTuple<User, AnswerRequest> retrieveTemporaryResult(String temporaryResultId, WdkModel wdkModel) throws WdkUserException, WdkModelException {
    // get the saved request cache and look up this ID
    Map<String,TwoTuple<User,AnswerRequest>> savedRequests = CacheMgr.get().getAnswerRequestCache();
    TwoTuple<User, AnswerRequest> savedRequest = savedRequests.get(temporaryResultId);

    // two ways this request could be expired
    User user = null;
    if (
        // 1. ID invalid or no longer in cache
        savedRequest == null ||
        // 2. Request creation date is too long in the past
        savedRequest.getSecond().getCreationDate().getTime() < new Date().getTime() - EXPIRATION_MILLIS
    ) {
      // return Not Found, but expire id first if not gone already
      if (savedRequest != null) {
        savedRequests.remove(temporaryResultId);
      }
      throw new WdkUserException("temporary result with ID '" + temporaryResultId + "'");
    }

    // success; return the fetched data
    return new TwoTuple<>(user, savedRequest.getSecond());
  }
}
