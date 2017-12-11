<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <c:set var="question" value="${wdkStep.question}"/>
  <html>
    <body>
      <div class="analysis-menu-tab-pane">
        <h3>Analyze your ${question.recordClass.displayName} results with a tool below.</h3>

        <div class="analysis-selector-container">
          <c:forEach items="${question.stepAnalyses}" var="analysisEntry">
            <!-- VAR ASSIGNMENT -->
            <c:set var="analysis" value="${analysisEntry.value}"/>
            <c:set var="style">
              <c:if test="${not empty analysis.customThumbnail}">
                background-image: url(${analysis.customThumbnail})
              </c:if>
            </c:set>
            <c:set var="class">
              <c:if test="${analysis.releaseVersion le 0}">inactive</c:if>
            </c:set>
            <!-- element -->
            <div class="${class} analysis-selector wdk-tooltip"
              role="${class eq 'inactive' ? '' : 'link'}"
              tabindex="${class eq 'inactive' ? '' : '0'}"
              title="${analysis.shortDescription}"
              data-name="${analysis.name}"
              data-step-id="${wdkStep.stepId}"
            >
              <c:if test="${analysis.releaseVersion le 0}">
                <div class="analysis-selection-banner">Coming soon...</div>
              </c:if>
              <c:if test="${analysis.releaseVersion eq wdkModel.model.buildNumber}">
                <div class="analysis-selection-banner new-analysis"></div>
              </c:if>
              <div class="analysis-selector-image" style="${style}"> </div>
              <div class="analysis-selector-content">
                <div class="analysis-selector-title">${analysis.displayName}</div>
                <p class="analysis-selector-description">${analysis.shortDescription}</p>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>
    </body>
  </html>
</jsp:root>
