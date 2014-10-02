<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div class="step-analysis-pane" data-analysis-id="${analysisId}"
           data-controller="wdk.stepAnalysis.loadDisplaySubpanes">
        <div class="ui-helper-clearfix">
          <span style="float:right">
           [ <a href="#rename">Rename This Analysis</a> |
             <a href="#copy">Copy These Parameter Values</a> ]
          </span>
          <h2 id="step-analysis-title" data-bind="displayName"><jsp:text/></h2>
          <div class="step-analysis-description">
            <span data-bind="shortDescription"><jsp:text/></span>
            <span class="toggle-description" title="Toggle full description">Read More</span>
          </div>
          <div data-bind="description" class="step-analysis-description"><jsp:text/></div>
        </div>
        <div class="step-analysis-subpane">
          <div class="step-analysis-errors-pane">
            <jsp:text/>
          </div>
          <div class="step-analysis-form-pane">
            <h3>Parameters</h3>
            <div> <jsp:text/> </div>
          </div>
        </div>

        <div class="step-analysis-subpane step-analysis-results-pane">
          <jsp:text/>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
