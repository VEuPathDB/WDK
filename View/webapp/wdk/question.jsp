<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="partial" value="${requestScope.partial}" />

<c:choose>
  	<c:when test="${partial}">
		<wdk:question/>
  	</c:when>
  	<c:otherwise>
    		<site:header refer="question" />
		<wdk:question/>
    		<site:footer />
  	</c:otherwise>
</c:choose>
