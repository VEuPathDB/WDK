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

<c:forEach items="${wdkRecord.tables}"  var="tbl">
  <tr>
    <td><b>${tbl.value.name}</b></td>
    <td>
      <font corlor="red"> how to show ${tbl.value.value}? </font>
    </td>
  </tr>
</c:forEach>

</table>

<site:footer/>
