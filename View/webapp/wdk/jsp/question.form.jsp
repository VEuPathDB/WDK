<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="customForm" value="${requestScope.customForm}" />

<c:choose>
  <c:when test="${customForm != null}">
    <p>Fetching JSP from ${customForm}</p>
    <jsp:include page="${customForm}" />
  </c:when>
  <c:otherwise>
    <wdk:question/>
  </c:otherwise>
</c:choose>
