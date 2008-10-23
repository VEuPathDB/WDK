<%-- 
Provides form input element for a given term tree node of EnumParam.

--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<%@ attribute name="param"
              type="org.gusdb.wdk.model.jspwrap.EnumParamBean"
              required="true"
              description="parameter name"
%>

<%@ attribute name="node"
              type="org.gusdb.wdk.model.EnumParamTermNode"
              required="true"
              description="Term Node"
%>

<%-- display param term --%>
<div id="${pNam}-info" class="term-node">
    <c:set var="pNam" value="${param.name}" />
    
    <c:choose>
        <c:when test="${fn:length(node.children) == 0}">
            <img src="images/spacer.gif width="19" height="19" />
        </c:when>
        <c:otherwise>
            <img src="images/minus.gif width="19" height="19" />
        </c:otherwise>
    </c:choose>
    
    <html:multibox property="myMultiProp(${pNam})" value="${node.term}" styleId="${pNam}" />
    
    <c:choose>
    <%-- test for param labels to italicize --%>
        <c:when test="${pNam == 'organism' or pNam == 'ecorganism'}">
            <i>${node.display}</i>&nbsp;
        </c:when>
        <c:otherwise> <%-- use multiselect menu --%>
            ${node.display}&nbsp;
        </c:otherwise>
    </c:choose>
</div>

<%-- recursively display children terms --%>
<div id="${pNam}-child" class="term-children">
    <c:forEach items="${node.children}" var="child">
        <wdk:enumParamInputNode param="${param}" node="${child}" />
    <c:/forEach>  
</div>
