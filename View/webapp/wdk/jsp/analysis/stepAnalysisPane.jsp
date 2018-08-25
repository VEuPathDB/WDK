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
          <div style="text-align:right">
            <span>
              [ <a href="#rename">Rename This Analysis</a> |
                <a href="#copy">Duplicate</a> ]
            </span>
          </div>
          <h2 id="step-analysis-title" data-bind="displayName"><jsp:text/></h2>
          <div class="step-analysis-description">
            <span data-bind="shortDescription"><jsp:text/></span>
            <span id="toggle-description-${analysisId}" class="toggle-description" title="Toggle full description">Read More</span>
          </div>
          <div data-bind="description" id="step-analysis-description-${analysisId}" class="step-analysis-description"><jsp:text/></div>
          <div class="step-analysis-usernotes">
            <textarea data-bind="userNotes" id="usernotes-data-${analysisId}" class="data" rows="3" cols="60">
            </textarea>
            <br/><button style="font-size:100%" name="usernotes" type="submit" value="submit-true">Update</button>
          </div>
        </div>
        <div class="step-analysis-subpane">
          <div class="step-analysis-errors-pane">
            <jsp:text/>
          </div>
          <c:set var="parameterHeader" value="${hasParameters ? 'Parameters' : ''}"/>
          <div class="step-analysis-form-pane" data-hasparams="${hasParameters}">
            <h3>${parameterHeader}</h3>
            <div> <jsp:text/> </div>
          </div>
        </div>
        <c:if test="${!hasParameters}">
          <div style="text-align:center;font-style:italic">The analysis results will be shown below.</div>
          <hr/>
        </c:if>
        <div class="step-analysis-subpane step-analysis-results-pane">
          <jsp:text/>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
