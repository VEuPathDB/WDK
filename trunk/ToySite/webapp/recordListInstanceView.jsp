<%@ taglib prefix="sample" tagdir="/WEB-INF/tags/local" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sample:header title="Results Page" banner="Results" />
<p>This is the results page.

<p>There are ${rliTotalSize} queries here.

  <misc:table border="1">

    <misc:tr>
    <c:forEach var="ri" items="${rliInstance}">
      <th align="center"><pre>${ri}</pre></th>
    </c:forEach>
    </misc:tr>

<!--
  <c:forEach var="row" items="${rli}">
    <tr>
    <c:forEach var="cell" items="${row}">
      <td><c:out value="${cell}" default="&nbsp;"/></td>
    </c:forEach>
    </tr>
  </c:forEach>
-->
  </misc:table>


  <sample:footer />
