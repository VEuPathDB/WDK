<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>

<misc:table>

  <misc:tr>
    <c:forEach var="row" items="${rivl.columnNames}">
      <c:forEach var="column" items="${row}">
	    <th><b>${rivl.displayName[column]}</b></th>
	  </c:forEach>
    </c:forEach>
  </misc:tr>

  <c:forEach var="row" items="${rivl}">
    <misc:tr>
      <c:forEach var="columnName" items="${rivl.columnNames}">
        <c:choose>
          <c:when test="${columnName == \"primary_key\"}">
         	<td><misc:primaryKey primaryKey="${row[columnName]}" url="${wdk_record_url}" /></td>
          </c:when>
          <c:otherwise>
          	<td><misc:multiType value="${row[columnName]}" /></td>
          </c:otherwise>
        </c:choose>
	  </c:forEach>
    </misc:tr>
  </c:forEach>
</misc:table>

