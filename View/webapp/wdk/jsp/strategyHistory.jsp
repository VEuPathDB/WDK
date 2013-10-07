<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />
<c:set var="wdkUser" value="${sessionScope.wdkUser}" />

<c:choose>
  <c:when test="${param.recordType ne null}">
    <imp:strategyHistoryTab model="${wdkModel}" user="${wdkUser}" recordType="${param.recordType}"/>
  </c:when>
  <c:otherwise>
    <imp:strategyHistory model="${wdkModel}" user="${wdkUser}" />
  </c:otherwise>
</c:choose>
