<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
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

<c:set var="answer_value" value="${answerValue}" scope="request"/>
<c:set var="strategy_id" value="${strategyId}" scope="request"/>
<c:set var="step_id" value="${stepId}" scope="request"/>

<c:set var="recordClass" value="${answerValue.recordClass}" />

<!-- already in includes.tag
<link rel="stylesheet" type="text/css" href="<c:url value='/wdk/css/wdkFilter.css' />">
-->

<c:forEach items="${recordClass.filterLayouts}" var="layout">
    <div class="filter-layout" id="${layout.name}">
        <c:set var="image" value="plus.gif" />
        <c:set var="show" value="none" />
        <c:if test="${layout.visible}">
            <c:set var="image" value="minus.gif" />
            <c:set var="show" value="block" />
        </c:if>
        
<!-- ========== open/close icon, filter icon, filter TITLE, explanation =============== -->
        <div title="Results hidden by the filter will not be combined into the next step." class="layout-info">
            <imp:image class="handle" src="/wdk/images/${image}" />
            <span class="display">
              <imp:image height="14px" src="/wdk/images/filter-short.png"/> 
              <b>${layout.displayName}</b>&nbsp;&nbsp;
            </span>
        </div>

<!-- ========== TABLE defined in jsp   =============== -->
        <div class="layout-detail" style="display: ${show}">
    <%--        <div class="description">${layout.description}</div>  --%>
            <c:set var="filter_layout" value="${layout}" scope="request"/>
            <c:set var="fileName" value="${layout.fileName}" />
            <c:if test="${fn:length(fileName) == 0}">
                <c:set var="fileName" value="filterTableLayout.jsp" />
            </c:if>
            <jsp:include page="/WEB-INF/includes/${fileName}"/>
            <c:remove var="filter_layout" scope="request"/>
        </div>
    </div>
</c:forEach>

<c:remove var="answer_value" scope="request"/>
<c:remove var="strategy_id" scope="request"/>
<c:remove var="step_id" scope="request"/>

