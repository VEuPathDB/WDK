<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>


<%@ attribute name="strategyId"
              required="true"
              description="The current strategy id"
%>

<%@ attribute name="stepId"
              required="true"
              description="The current stepId"
%>

<%@ attribute name="answerValue"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              required="true"
              description="The current answer value"
%>

<%@ attribute name="instanceName"
              required="true"
              description="the name of the filter instance"
%>

<c:set var="recordClass" value="${answerValue.recordClass}" />
<c:set var="instance" value="${recordClass.filterMap[instanceName]}" />

<c:set var="current">
    <c:set var="currentFilter" value="${answerValue.filter}" />
    <c:choose>
        <c:when test="${currentFilter != null}">${instance.name == currentFilter.name}</c:when>
        <c:otherwise>false</c:otherwise>
    </c:choose>
</c:set>

<div class="filter-instance">
    <c:choose>
      <c:when test="${current}"><div class="current"></c:when>
      <c:otherwise><div></c:otherwise>
    </c:choose>
        <c:url var="linkUrl" value="/processFilter.do?strategy=${strategyId}&revise=${stepId}&filter=${instance.name}" />
        <c:url var="countUrl" value="/showResultSize.do?step=${stepId}&answer=${answerValue.checksum}&filter=${instance.name}" />
        <a id="link-${instance.name}" class="link-url" href="javascript:void(0)" countref="${countUrl}" 
           strId="${strategyId}" stpId="${stpId}" linkUrl="${linkUrl}"><c:choose><c:when test="${current}">${answerValue.resultSize}</c:when><c:otherwise><img class="loading" src="<c:url value="/wdk/images/filterLoading.gif" />" /></c:otherwise></c:choose></a>
        <div class="instance-detail" style="display: none;">
            <div class="display">${instance.displayName}</div>
            <div class="description">${instance.description}</div>
        </div>
    </div>
</div>

