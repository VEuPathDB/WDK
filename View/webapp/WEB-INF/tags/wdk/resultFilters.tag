<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="The current step"
%>

<div class="wdk-result-filters">
  <c:forEach items="${step.question.filters}" var="entity">
    <c:set var="filter" value="${entity.value}" />
    <div class="filter" id="${filter.name}">
      <div class="display">${filter.display}</div>
      <div class="description">${filter.description}</div>
    </div>
  </c:forEach>
</div>
