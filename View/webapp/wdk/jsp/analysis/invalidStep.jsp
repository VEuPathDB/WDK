<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div>
        <h3>Invalid Step</h3>
        <p>
          Changes to your strategy have made this step no longer analyzable by this tool for the following reason:
        </p><p>
          <em>${reason}</em>
        </p>
      </div>
    </body>
  </html>
</jsp:root>
