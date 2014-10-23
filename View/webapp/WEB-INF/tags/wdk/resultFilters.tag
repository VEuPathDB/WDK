<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="The current step"
%>

<div class="wdk-filters" data-step="${step.stepId}">
  <h3>Filters<h3>
  <div class="filter-list">
    <c:forEach items="${step.question.filters}" var="entity">
      <c:set var="filter" value="${entity.value}" />
      <div class="filter" id="${filter.key}">
        <div class="display">${filter.display}</div>
        <div class="description">${filter.description}</div>
      </div>
    </c:forEach>
  </div>

  <div class="filter-detail">
    <div class="description"> </div>
    <form class="filter-form">
      <div class="filter-summary"> </div>
      <div class="filter-controls">
        <input type="button" value="Apply Filter">
      </div>
    </form>
  </div>
</div>

