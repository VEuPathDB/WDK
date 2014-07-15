package org.gusdb.wdk.controller.actionutil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines set of response types (i.e. MIME types) of responses the server
 * sends.
 * 
 * @author rdoherty
 */
public enum ResponseType {

  /* page part types */
  html           ("text/html", "page", ".html"),
  html_part      ("text/html", "page", ".html"),
  javascript     ("application/javascript", "script", ".js"),
  css            ("text/css", "styles", ".css"),

  /* service types */
  xml            ("application/xml", "document", ".xml"),
  json           ("application/json", "object", ".json"),

  /* image types */
  gif            ("image/gif", "image", ".gif"),
  jpeg           ("image/jpeg", "image", ".jpg"),
  png            ("image/png", "image", ".png"),
  svg            ("image/svg+xml", "image", ".svg"),
  icon           ("image/x-icon", "icon", ".ico"),

  /* document types */
  text           ("text/plain", "file", ".txt"),
  tab_delim_text ("text/tab-separated-values", "file", ".tab"),
  excel          ("application/vnd.ms-excel", "spreadsheet", ".xls"),
  pdf            ("application/pdf", "document", ".pdf"),

  /* compressed/lib types */
  tar            ("application/x-tar", "archive", ".tar"),
  zip            ("application/zip", "compressedFile", ".zip"),
  gzip           ("application/gzip", "compressedFile", ".gz"),
  bz2            ("application/x-bzip2", "compressedFile", ".bz2"),

  /* arbitrary binary data */
  binary_data    ("application/octet-stream", "data", ".bin");
  
  private static Map<String, ResponseType> EXTENSION_MAP = new HashMap<>();
  
  private String _mimeType;
  private String _defaultFileName;
  private String _primaryExtension;
  private String[] _secondaryExtensions;
  
  private ResponseType(String mimeType, String defaultFileNameRoot,
      String primaryExtension, String... secondaryExtensions) {
    _mimeType = mimeType;
    _defaultFileName = defaultFileNameRoot + primaryExtension;
    _primaryExtension = primaryExtension;
    _secondaryExtensions = secondaryExtensions;
  }
  
  /**
   * @return MIME type of this response type
   */
  public String getMimeType() {
    return _mimeType;
  }
  
  /**
   * @return default file name for this response (e.g. if a response is to be
   * saved onto the client's disk)
   */
  public String getDefaultFileName() {
    return _defaultFileName;
  }
  
  /**
   * @return extension for this type
   */
  public String getExtension() {
    return _primaryExtension;
  }
  
  public static ResponseType resolveType(Path filename) {
    if (EXTENSION_MAP.isEmpty()) initializeExtensionMap();
    if (filename == null || filename.getFileName() == null)
      throw new NullPointerException("filename cannot be null");
    String finalElement = filename.getFileName().toString();
    String finalElementLower = finalElement.toLowerCase();
    for (String extension : EXTENSION_MAP.keySet()) {
      if (finalElementLower.endsWith(extension)) {
        return EXTENSION_MAP.get(extension);
      }
    }
    throw new IllegalArgumentException("Unable to determine valid response type from filename: " + finalElement);
  }

  private static void initializeExtensionMap() {
    synchronized(EXTENSION_MAP) {
      if (EXTENSION_MAP.isEmpty()) {
        for (ResponseType type : values()) {
          EXTENSION_MAP.put(type._primaryExtension, type);
          for (String secondaryExtension : type._secondaryExtensions) {
            EXTENSION_MAP.put(secondaryExtension, type);
          }
        }
      }
    }
  }
}
