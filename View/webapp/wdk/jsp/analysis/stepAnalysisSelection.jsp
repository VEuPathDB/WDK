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
        <div class="new-analysis-menu">
          <ul>
            <c:set var="question" value="${wdkStep.question}"/>
            <c:forEach items="${question.stepAnalyses}" var="analysisEntry">
              <c:set var="analysis" value="${analysisEntry.value}"/>
              <c:set var="hyphen" value="${empty analysis.description ? '' : ': '}"/>
              <li>
                <a href="javascript:createStepAnalysis('${analysis.name}', ${wdkStrategy.strategyId}, ${wdkStep.stepId})">
                  ${analysis.displayName}
                </a>
                ${hyphen}${analysis.description}
              </li>
            </c:forEach>
          </ul>
        </div>
      </div>
    </body>
  </html>
</jsp:root>