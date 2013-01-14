package org.gusdb.wdk.controller.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONObject;

public class FileUploadAction extends WdkAction {

  private static final Logger LOG = Logger.getLogger(FileUploadAction.class.getName());
  
  private static final String UPLOAD_NAME_PREFIX = "file";
  private static final int PREVIEW_CHARS = 100;
  
  @Override protected boolean shouldValidateParams() { return false; }
  @Override protected Map<String, ParamDef> getParamDefs() { return EMPTY_PARAMS; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    LOG.info("Entering FileUploadAction...");
    JSONObject result = new JSONObject();
    for (String fieldName : params.getKeys()) {
      if (fieldName.startsWith(UPLOAD_NAME_PREFIX)) {
        JSONObject fileInfo = new JSONObject();
        DiskFileItem file = params.getUpload(fieldName);
        if (file == null) {
          fileInfo.put("uploadSuccessful", false);
        }
        else {
          fileInfo.put("uploadSuccessful", true);
          fileInfo.put("fieldName", file.getFieldName());
          fileInfo.put("fileName", file.getName());
          fileInfo.put("size", file.getSize());
          fileInfo.put("contentType", file.getContentType());
          fileInfo.put("preview", getFilePreview(file));
        }
        result.append("results", fileInfo);
      }
    }
    return new ActionResult(ResponseType.json).setStream(getStreamFromString(result.toString(2)));
  }
  
  private String getFilePreview(DiskFileItem file) throws WdkModelException {
    // NOTE: Could also use file.getContent().substring(0, PREVIEW_CHARS);
    //       ...but we would be reading the whole thing into memory just to get
    //       the preview; probably not a good thing for mid- to large-size files.
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < PREVIEW_CHARS && reader.ready(); i++) {
        sb.append((char)reader.read());
      }
      return sb.toString();
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to read uploaded file.", e);
    }
    finally {
      IoUtil.closeQuietly(reader);
    }
  }
}
