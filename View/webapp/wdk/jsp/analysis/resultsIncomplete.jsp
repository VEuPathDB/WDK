<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div class="analysis-incomplete-pane">
        <h3>Results Incomplete</h3>
        <p>
          This analysis failed to complete.<br/>
          <!-- This analysis was started at ${analysis.startDate}.<br/> -->
          Its resulting status is: ${status}.
        </p>
      </div>
    </body>
  </html>
</jsp:root>
