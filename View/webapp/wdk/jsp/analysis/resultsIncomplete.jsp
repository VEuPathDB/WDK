<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div class="analysis-incomplete-pane">
        <h3>Results Unavailable</h3>
        <p>
          <em>The results of this analysis are not available for the reason below.  Run this analysis again to receive results.</em>
        </p><p>
          ${reason}
        </p>
      </div>
    </body>
  </html>
</jsp:root>
