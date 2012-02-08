<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="customForm" value="${requestScope.customForm}" />

<c:choose>
  <c:when test="${customForm != null}">
    <jsp:include page="${customForm}" />
  </c:when>
  <c:otherwise>
    <imp:question/>
  </c:otherwise>
</c:choose>
