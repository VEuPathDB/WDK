<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- get wdkRecord from proper scope -->
<c:set value="${requestScope.wdkRecord}" var="wdkRecord"/>



<table width="100%" class="wdk-record-table">

  <c:forEach items="${wdkRecord.summaryAttributes}" var="attr">
    <c:set var="fieldVal" value="${attr.value}"/>
    <c:if test="${fieldVal.attributeField.internal == false}">
      <tr>
        <td class="label">${fieldVal.displayName}: </td>
        <td>
          <!-- need to know if fieldVal should be hot linked -->
          <c:choose>
            <c:when test="${fieldVal.class.name eq 'org.gusdb.wdk.model.record.attribute.LinkAttributeValue'}">
              <a href="${fieldVal.url}">${fieldVal.displayText}</a>
            </c:when>
            <c:otherwise>
              <font class="fixed"><w:wrap size="60">${fieldVal.value}</w:wrap></font>
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:if>
  </c:forEach>
</table>

<!-- show all tables for record -->
<c:forEach items="${wdkRecord.tables}"  var="tblEntry">
  <c:set var="wdkTable" value="${tblEntry.value}" />
  <c:if test="${wdkTable.tableField.internal == false}">
    <div> </div>
    <imp:wdkTable tblName="${tblEntry.key}" isOpen="true"/>
  </c:if>
</c:forEach>
