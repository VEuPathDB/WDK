<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ attribute name="analysis"
              required="true"
              type="org.gusdb.wdk.model.analysis.StepAnalysis"
              description="The Analysis To Display As Tile"
%>
<%@ attribute name="recordClassName"
              required="false"
              description="The parent question's RecordClassName"
%>

<!-- VAR ASSIGNMENT -->
<c:set var="style">
  <c:if test="${not empty analysis.customThumbnail}">
    background-image: url(${analysis.customThumbnail})
  </c:if>
</c:set>
<c:set var="className">
  <c:if test="${analysis.releaseVersion le 0}">inactive</c:if>
</c:set>
<!-- element -->
<div class="${className} analysis-selector wdk-tooltip"
  role="${className eq 'inactive' ? '' : 'link'}"
  tabindex="${className eq 'inactive' ? '' : '0'}"
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
