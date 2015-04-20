<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="step" value="${requestScope.step}" />
<c:set var="summary" value="${requestScope.summary}" />

<div class="strategy-filter">

  <div class="description">
    Select an existing strategy to be used as filter on the current step.
  </div>

  <c:forEach items="${summary.strategies}" var="strategy">
    <div class="strategy">
      <input type="radio" name="strategy" value="${strategy.strategyId}" />
      <span class="name">${strategy.name}</span>
      <c:set var="size" value="${strategy.estimateSize}" />
      <c:set var="record" value="${strategy.recordClass}" />
      <c:set var="recordName">
        <c:choose>
          <c:when test="${size > 1}">${record.displayNamePlural}</c:when>
          <c:otherwise>${record.displayName}</c:otherwise>
        </c:choose>
      </c:set>
      <span class="count">: ${size} ${recordName}</span> 
    </div>
  </c:forEach>
  
</div>

