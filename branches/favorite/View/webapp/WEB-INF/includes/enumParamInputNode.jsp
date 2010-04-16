<%-- 
Provides form input element for a given term tree node of EnumParam.

--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="qP" value="${requestScope.recurse_enum_param}"/>
<c:set var="node" value="${requestScope.recurse_term_node}"/>

<c:set var="children" value="${node.children}" />
<c:set var="pNam" value="${qP.name}" />

<%-- set left margin for node (instead of using spacer gif
     for the leaves) --%>
<c:if test="${fn:length(children) == 0}">
    <c:set var="nodeMargin" value="margin-left:22px;" />
</c:if>

<%-- display param term --%>
<div id="${pNam}-info" class="term-node" style="${nodeMargin}">
    <c:choose>
        <c:when test="${fn:length(children) == 0}">
            <c:set var="nodeValue" value="${node.term}" />
        </c:when>
        <c:otherwise>
            <img class="switch plus" title="Click to expand(+) or collapse(-)" src="images/plus.gif" style="vertical-align:bottom" onclick="toggleChildren(this)"/>
            <c:set var="nodeValue" />
        </c:otherwise>
    </c:choose>

    <%-- hide parent nodes if it has children. --%>
    <html:multibox property="myMultiProp(${pNam})" value="${nodeValue}" styleId="${pNam}" onclick="toggleChildrenCheck(this)" />   
 
    <c:choose>
    <%-- test for param labels to italicize --%>
        <c:when test="${pNam == 'organism' or pNam == 'ecorganism'}">
            <i>${node.display}</i>&nbsp;
        </c:when>
        <c:otherwise> <%-- use multiselect menu --%>
            ${node.display}&nbsp;
        </c:otherwise>
    </c:choose>


<%-- recursively display children terms --%>
<c:if test="${fn:length(children) != 0}">
<div id="${pNam}-child" class="term-children" style="padding-left: 20px;">
    <c:forEach items="${children}" var="child">
        <c:set var="recurse_term_node" value="${child}" scope="request"/>
        <c:import url="/WEB-INF/includes/enumParamInputNode.jsp" />
    </c:forEach>  
</div>
</c:if>
</div>
