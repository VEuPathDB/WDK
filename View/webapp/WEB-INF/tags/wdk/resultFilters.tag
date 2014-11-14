<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="The current step"
%>


<div class="wdk-filters" data-step="${step.stepId}">

  <div class="current-filters">
    <c:forEach items="${step.filterOptions.filterOptions}" var="entity">
      <c:set var="option" value="${entity.value}" />
      <c:set var="filter" value="${option.filter}" />
      <div class="option ${option.disabled ? 'disabled' : ''}" 
           id="${filter.key}" title="${filter.description}">
        <c:set var="checked"><c:if test="${!option.disabled}">checked="checked"</c:if></c:set>
        <input type="checkbox" ${checked}
               title="Uncheck to disable this filter"
               data-url="<c:url value='/toggleFilter.do?step=${step.stepId}&filter=${filter.key}' />" />
        <span class="name">${filter.display}</span>
        :
        <span class="value">${option.displayValue}</span>
        <a href="<c:url value='/removeFilter.do?step=${step.stepId}&filter=${filter.key}' />"
          title="Remove this filter">
          <span class="remove ui-icon ui-icon-closethick"></span>
        </a>
      </div>
    </c:forEach>
  </div>

  <div class="filters-panel">
    <h3>Choose available Filters<h3>

    <table>
      <tr><td>Filter By:</td></tr>
      <tr>
        <td class="filter-list">
          <c:forEach items="${step.question.filters}" var="entity">
            <c:set var="filter" value="${entity.value}" />
            <div class="filter" id="${filter.key}">
              <div class="display">${filter.display}</div>
              <div class="description">${filter.description}</div>
            </div>
          </c:forEach>
        </td>

        <td class="filter-detail">
          <div class="description"> </div>
          <form class="filter-form" action="<c:url value='/applyFilter.do' />">
            <input type="hidden" name="step" value="${step.stepId}" />
            <input type="hidden" name="filter" value="" />
            <div class="filter-summary"> </div>
            <div class="filter-controls">
              <input type="submit" value="Apply Filter">
            </div>
          </form>
        </td>
      </tr>
    </table>

  </div>
</div>

