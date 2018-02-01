<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <c:set var="question" value="${wdkStep.question}"/>
  <html>
    <body>
      <div class="analysis-menu-tab-pane">
        <h3>Analyze your ${question.recordClass.displayName} results with a tool below.</h3>
        <div class="analysis-selector-container">
          <c:forEach items="${question.stepAnalyses}" var="analysisEntry">
            <c:set var="analysis" value="${analysisEntry.value}"/>
            <imp:stepAnalysisTile analysis="${analysis}" recordClassName="${question.recordClass.fullName}" />
          </c:forEach>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
