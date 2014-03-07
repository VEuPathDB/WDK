<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <form style="text-align:center">
        <h3>Click below to run this analysis.</h3>
        <div style="white-space:nowrap; margin:5px"
             title="This name will appear in the tab for this analysis. You can change it later.">
          <span>Name this analysis (optional): </span>
          <input type="text" name="displayName" size="40"/>
        </div>
        <input type="submit" value="Run Analysis"/>
      </form>
    </body>
  </html>
</jsp:root>
