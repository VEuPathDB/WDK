<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div style="text-align:center">
        <iframe src="${viewModel.iframeUrl}" width="${viewModel.iframeWidth}" height="${viewModel.iframeHeight}"/>
      </div>
    </body>
  </html>
</jsp:root>