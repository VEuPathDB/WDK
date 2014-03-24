<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div class="step-analysis-pane" data-analysis-id="${analysisId}"
           data-controller="wdk.stepAnalysis.loadDisplaySubpanes">
        <div class="step-analysis-subpane">
          <div class="step-analysis-errors-pane">
            <jsp:text/>
          </div>
          <div class="step-analysis-form-pane">
            <jsp:text/>
          </div>
        </div>
        <div class="step-analysis-subpane step-analysis-results-pane">
          <jsp:text/>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
