<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>


<%@ attribute name="answerValue"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              required="true"
              description="The current answer value"
%>

<%@ attribute name="instanceName"
              type="string"
              required="true"
              description="the name of the filter instance"
%>

<c:set var="recordClass" value="${answerValue.recordClass}" />
<c:set var="instance" value="recordClass.filterMap[instanceName]" />

<c:set var="current">
    <c:set var="currentFilter" value="${answerValue.Filter}" />
    <c:choose>
        <c:when test="${currentFilter != null}">${instance.name == currentFilter.name}</c:when>
        <c:otherwise>false</c:otherwise>
    </c:choose>
</c:set>

<div class="filter-instance">
    <c:if test="${current}"><div class="current"></c:if>
        <a class="link-url" href="<c:url value="/processSummary.do?strategy=${strategy_id}&step=${step_id}&command=filter&filter=${instance.name}" />">
            <c:choose>
                <c:when test="${current}">${answerValue.resultSize}</c:when>
                <c:otherwise>--</c:otherwise>
            </c:choose>
        </a>
        <span class="count-url">
            <c:url value="/showSummary.do?strategy=${strategy_id}&step=${step_id}&command=filter&filter=${instance.name}" />
        </span>
        <div class="instance-detail">
            <div class="display">${instance.displayName}</div>
            <div class="description">${instance.description}</div>
        </div>
    <c:if test="${current}"></div></c:if>
</div>

