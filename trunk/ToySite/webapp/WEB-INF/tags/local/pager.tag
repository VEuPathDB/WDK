<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<pg:index>

  <pg:first>
    <a href="${pageUrl}">First</a>
  </pg:first>

  <pg:prev>
    <a href="${pageUrl}">Previous</a>
  </pg:prev>

  <pg:pages>
    <c:if test="${pageNumber < 10}">&nbsp;</c:if>
    <c:choose>
      <c:when test="${pageNumber==currentPageNumber}">
        <b>${pageNumber}</b>
      </c:when>
      <c:otherwise>
        <a href="${pageUrl}">${pageNumber}</a>
      </c:otherwise>
    </c:choose>
  </pg:pages>

  <pg:next>
    <a href="${pageUrl}">Next</a>
  </pg:next>

  <pg:last>
    <a href="${pageUrl}">Last</a>
  </pg:last>

</pg:index>
  