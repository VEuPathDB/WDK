<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${sessionScope.wdkRecord}" var="wdkRecord"/>

<c:set value="${wdkRecord.recordClass.fullName}" var="recordName"/>
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
      <table border="1" cellspacing="0" cellpadding="2">
        <!-- table header -->
        <tr class="headerRow">
          <c:forEach var="hCol" items="${resLst.columns}">
            <th>${hCol.displayName}</th>
          </c:forEach>
        </tr>

        <!-- table rows -->
        <c:set var="i" value="0"/>
        <c:forEach var="row" items="${resLst.rows}">

          <c:choose>
            <c:when test="${i % 2 == 0}"><tr class="rowLight"></c:when>
            <c:otherwise><tr class="rowDark"></c:otherwise>
          </c:choose>

            <c:forEach var="rCol" items="${row}">
              <td>${rCol.value}</td>
            </c:forEach>
          </tr>
        <c:set var="i" value="${i +  1}"/>
        </c:forEach>
      </table>
      <!-- close resultList -->
      <c:set var="junk" value="close"/>
    </td>
  </tr>
</c:forEach>

</table>

<site:footer/>
