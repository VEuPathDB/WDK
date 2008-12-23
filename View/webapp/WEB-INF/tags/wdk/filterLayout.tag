<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>


<%@ attribute name="answerValue"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              required="true"
              description="The current answer value"
%>

<%@ attribute name="layoutName"
              type="String"
              required="true"
              description="the filter layout name"
%>


<c:set var="recordClass" value="${answerValue.recordClass}" />
<c:set var="layout" value="${recordClass.filterLayoutMap[layoutName]}" />
<c:set var="currentInstance">
    <c:if test="${answerValue.filter != null}">${answerValue.filter.name}</c:if>
</c:set>

<%-- transform layout text --%>
<c:set var="layoutText" value="${layout.layout}" />
<c:forEach items="${layout.instances}" var="instance">
    <c:set var="instanceName" value="$$${instance.name}$$" />
    <c:set var="instanceValue">
        <div class="instance">
            <c:if test="${instance.name == currentInstance}">
                <div class="current">
            </c:if>
                    <span class="pending">--</span>
                    <a class="link" href="<c:url value="/processSummary.do?strategy=${strategy_id}&step=${step_id}&command=filter&filter=${instance.name}" />"></a>
                    <span class="count-url"><c:url value="/showSummary.do?strategy=${strategy_id}&step=${step_id}&command=filter&filter=${instance.name}" /></span>
            <c:if test="${instance.name == currentInstance}">
                </div>
            </c:if>
        </div>
    </c:set>
    <c:set var="layoutText" value="${fn:replace(layoutText, instanceName, )}" />
</c:forEach>

<div class="filter-layout">
    <div class="layout-name">
        <c:set var="imgUrl">
            <c:choose>
                <c:when test="layout.visible"><c:url value="/images/minus.gif" /></c:when>
                <c:otherwise><c:url value="/images/plus.gif" /></c:otherwise>
            </c:choose>
        </c:set>
        <img class="layout-handle" src="${imgUrl}" />
        ${layout.displayName}
    </div>
    <c:set var="contentStyle">
        <c:choose>
            <c:when test="layout.visible">style="display: block;"</c:when>
            <c:otherwise>style="display: none;"</c:otherwise>
        </c:choose>
    </c:set>
    <div class="layout-content" ${contentStyle}>
        <div>${layout.description}</div>
        <div>${layoutText}</div>
    </div>
</div>

