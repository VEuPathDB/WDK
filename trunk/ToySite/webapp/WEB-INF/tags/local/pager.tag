<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>

<pg:index>

  <pg:first>
    <a href="${pageUrl}">First</a>
  </pg:first>

  <pg:prev>
    <a href="${pageUrl}">Previous (${pageNumber})</a>
  </pg:prev>

  <pg:pages>
    <a href="${pageUrl}">${pageNumber}</a> 
  </pg:pages>

  <pg:next>
    <a href="${pageUrl}">Next (${pageNumber})</a>
  </pg:next>

  <pg:last>
    <a href="${pageUrl}">Last</a>
  </pg:last>

</pg:index>
  