package org.gusdb.wdk.controller.actionutil;

/**
 * Defines set of response types (i.e. MIME types) of responses the server
 * sends.
 * 
 * @author rdoherty
 */
public enum ResponseType {
  html           ("text/html", "page.html"),
  html_part      ("text/html", "page.html"),
  xml            ("application/xml", "document.xml"),
  json           ("application/json", "object.json"),
  text           ("text/plain", "file.txt"),
  tab_delim_text ("text/tab-separated-values", "file.tab"),
  jpeg           ("image/jpeg", "image.jpg"),
  png            ("image/png", "image.png"),
  svg            ("image/svg+xml", "image.svg"),
  excel          ("application/vnd.ms-excel", "spreadsheet.xls"),
  zip            ("application/zip", "compressedFile.zip"),
  binary_data    ("application/octet-stream", "data.bin");
  
  private String _mimeType;
  private String _defaultFileName;
  
  private ResponseType(String mimeType, String defaultFileName) {
    _mimeType = mimeType;
    _defaultFileName = defaultFileName;
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
}
