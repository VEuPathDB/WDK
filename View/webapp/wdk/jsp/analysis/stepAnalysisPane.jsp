<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div class="step-analysis-pane" data-analysis-id="${analysisId}"
           data-controller="wdk.stepAnalysis.loadDisplaySubpanes">
        <div>
          <span style="float:right">
           [ <a href="javascript:wdk.stepAnalysis.renameStepAnalysis(${analysisId})">Rename This Tab</a> |
             <a href="javascript:wdk.stepAnalysis.copyStepAnalysis(${analysisId})">Copy This Configuration</a> ]
          </span>
          <div style="clear:both"><jsp:text/></div>
        </div>
        <div class="step-analysis-subpane">
          <div class="step-analysis-errors-pane">
            <jsp:text/>
          </div>
          <div class="step-analysis-form-pane">
            <jsp:text/>
          </div>
        </div>
        <hr/>
        <div class="step-analysis-subpane step-analysis-results-pane">
          <jsp:text/>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
