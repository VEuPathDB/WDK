<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>


<misc:table>
  <c:forEach var="row" items="${rivl}">
    <misc:tr>
	  <td>
	  <b>CDS: <a href="/sampleWDK/ViewFullRecord?recordReference=RNARecords.PSUCDSRecordByName&primaryKey=${row.name}">${row.name}</a></b>
	  <br>${row.product}
	  <br>Number of exons: ${row.number_of_exons}</td>
    </misc:tr>
  </c:forEach>
</misc:table>

