<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <html>
    <body>
      <div class="analysis-menu-tab-pane">
        <h3>Select an analysis tool below with which to perform analysis</h3>
        <em>Clicking on a link below will open a new tab where you can configure and run a Step Analysis.</em>
        <hr/>
        <div class="sa-selector-container">
          <ul class="ui-helper-clearfix">
            <c:set var="question" value="${wdkStep.question}"/>
            <c:forEach items="${question.stepAnalyses}" var="analysisEntry">
              <c:set var="analysis" value="${analysisEntry.value}"/>
              <!-- <li onclick="javascript:wdk.stepAnalysis.createStepAnalysis('${analysis.name}', ${wdkStep.stepId})"> -->
              <li data-name="${analysis.name}" data-step-id="${wdkStep.stepId}"
                  data-release-version="${analysis.releaseVersion}">
                <c:if test="${analysis.releaseVersion eq -1}">
                  <div class="analysis-coming-soon">Coming soon...</div>
                </c:if>
                <div class="analysis-wrapper">
                  <div class="analysis-title">${analysis.displayName}</div>
                  <div class="analysis-description">
                    ${analysis.description}
                  </div>
                </div>
              </li>
            </c:forEach>
          </ul>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
