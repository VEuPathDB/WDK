<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${sessionScope.wdkRecord}" var="wdkRecord"/>

<c:set value="${wdkRecord.record.fullName}" var="recordName"/>
<c:set value="${wdkRecord.primaryKey}" var="recordId"/>
<site:header banner="${recordName} ${recordId}"/>

<table>
<c:forEach items="${wdkRecord.attributes}" var="attr">
  <tr>
    <td><b>${attr.value.displayName}</b></td>
    <td>${attr.value.value}</td>
  </tr>
</c:forEach>

<!-- show all tables for record -->
<c:forEach items="${wdkRecord.tables}"  var="tbl">
  <tr>
    <td valign="top"><b>${tbl.value.name}</b></td>
    <td>
      <c:set var="resLst" value="${tbl.value.value}"/>

      <!-- show one table -->
      <table border="1">
        <!-- table header -->
        <tr>
          <c:forEach var="hCol" items="${resLst.columns}">
            <th>${hCol.displayName}</th>
          </c:forEach>
        </tr>

        <!-- table rows -->
        <c:forEach var="row" items="${resLst.rows}">
          <tr>
            <c:forEach var="rCol" items="${row}">
              <td>${rCol.value}</td>
            </c:forEach>
          </tr>
        </c:forEach>
      </table>
      <!-- close resultList -->
      <c:set var="junk" value="close"/>
    </td>
  </tr>
</c:forEach>

</table>

<site:footer/>
