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

        <div class="sa-selector-container">
          <ul class="ui-helper-clearfix">
            <c:forEach items="${question.stepAnalyses}" var="analysisEntry">
              <c:set var="analysis" value="${analysisEntry.value}"/>
              <c:set var="style">
                <c:if test="${not empty analysis.customThumbnail}">
                  background-image: url(${analysis.customThumbnail})
                </c:if>
              </c:set>
              <c:set var="class">
                <c:if test="${analysis.releaseVersion eq -1}">inactive</c:if>
              </c:set>

              <li class="${class}"
                  role="${class eq 'inactive' ? '' : 'link'}"
                  tabindex="${class eq 'inactive' ? '' : '0'}"
                  style="${style}"
                  title="${analysis.description}"
                  data-name="${analysis.name}" data-step-id="${wdkStep.stepId}">
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
