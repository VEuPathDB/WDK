package org.gusdb.wdk.controller.actionutil;

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
  binary_data    ("application/octet-stream","data.bin");
  
  private String _mimeType;
  private String _defaultFileName;
  
  private ResponseType(String mimeType, String defaultFileName) {
    _mimeType = mimeType;
    _defaultFileName = defaultFileName;
  }
  
  public String getMimeType() {
    return _mimeType;
  }
  
  public String getDefaultFileName() {
    return _defaultFileName;
  }
}
