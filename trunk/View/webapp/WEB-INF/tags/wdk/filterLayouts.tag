<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>


<%@ attribute name="answerValue"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              required="true"
              description="The current answer value"
%>

<c:set var="answer_value" value="${answerValue}" scope="request"/>
<c:set var="recordClass" value="${answerValue.recordClass}" />

<c:forEach items="${recordClass.filterLayouts}" var="layout">
    <div class="filter-layout" id="${layout.name}">
        <c:set var="image" value="plus.gif" />
        <c:set var="show" value="none" />
        <c:if test="${layout.visible}">
            <c:set var="image" value="minus.gif" />
            <c:set var="show" value="block" />
        </c:if>
        
        <div class="layout-info">
            <img class="handle" src="<c:url value="/images/${image}" />" />
            <span class="display">${layout.displayName}</span>
        </div>
        <div class="layout-detail">
            <div class="description">${layout.description}</div>
        
            <c:set var="filter_layout" value="${layout}" scope="request"/>
            <jsp:include page="/WEB-INF/includes/${layout.fileName}"/>
            <c:remove var="filter_layout" scope="request"/>
        </div>
    </div>
</c:forEach>

<c:remove var="answer_value" scope="request"/>
