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

<%-- display param term --%>
<div id="${pNam}-info" class="term-node">
    <c:choose>
        <c:when test="${fn:length(children) == 0}">
            <img src="images/spacer.gif" width="19" height="19" />
        </c:when>
        <c:otherwise>
            <img class="switch plus" src="images/plus.gif" width="19" height="19" onclick="toggleChildren(this)"/>
        </c:otherwise>
    </c:choose>
    
    <html:multibox property="myMultiProp(${pNam})" value="${node.term}" styleId="${pNam}" onclick="toggleChildrenCheck(this)" />
    
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
<div id="${pNam}-child" class="term-children">
    <c:forEach items="${children}" var="child">
        <c:set var="recurse_term_node" value="${child}" scope="request"/>
        <c:import url="/WEB-INF/includes/enumParamInputNode.jsp" />
    </c:forEach>  
</div>
</c:if>
</div>