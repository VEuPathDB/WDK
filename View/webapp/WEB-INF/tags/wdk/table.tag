<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="table"
              required="true"
              type="org.gusdb.wdk.model.TableValue"
              description="The WDK Table to be rendered"
%>


<c:catch var="tableError">

  <c:set var="size" value="${fn:length(table)}" />

  <c:choose>
    <c:when test="${size == 0}">
      <p>No Data available</p>
    </c:when>
    <c:otherwise>

<table class="wdk-table">

  <thead>
    <tr class="headerrow">
        <c:forEach var="hCol" items="${table.tableField.attributeFields}">
           <c:if test="${hCol.internal == false}">
             <th nowrap>${hCol.displayName}</th>
           </c:if>
        </c:forEach>
    </tr>
  </thead>

  <tbody>
    <%-- table rows --%>
    <c:set var="i" value="0"/>
    <c:forEach var="row" items="${table}">
        <c:choose>
            <c:when test="${i % 2 == 0}"><tr class="rowLight"></c:when>
            <c:otherwise><tr class="rowMedium"></c:otherwise>
        </c:choose>
        <c:forEach var="rColEntry" items="${row}">
          <c:set var="rCol" value="${rColEntry.value}"/>
          <c:if test="${rCol.attributeField.internal == false}">
            <%-- need to know if value should be hot linked --%>
            <c:set var="align" value="align='${rCol.attributeField.align}'" />
            <c:set var="nowrap">
                <c:if test="${rCol.attributeField.nowrap}">nowrap</c:if>
            </c:set>
        
            <td ${align} ${nowrap}>
                <c:choose>
                    <c:when test="${rCol.class.name eq 'org.gusdb.wdk.model.LinkAttributeValue'}">
                        <a href="${rCol.url}">${rCol.displayText}</a>
                    </c:when>
                    <c:otherwise>
                        ${rCol.value}
                    </c:otherwise>
                </c:choose>
            </td>
          </c:if>
        </c:forEach>
      </tr>
      <c:set var="i" value="${i +  1}"/>
    </c:forEach>
  </tbody>

  <c:if test="${size >= 20}">
  <tfoot>
    <tr class="footerrow">
        <c:forEach var="hCol" items="${table.tableField.attributeFields}">
           <c:if test="${hCol.internal == false}">
             <th nowrap>${hCol.displayName}</th>
           </c:if>
        </c:forEach>
    </tr>
  </tfoot>
  </c:if>

</table>

  </c:otherwise><%-- table has rows --%>
</c:choose>


<%-- display the description --%>
<div class="table-description">${tbl.tableField.description}</div>



</c:catch>
<c:if test="${tableError != null}">
    <c:set var="exception" value="${tableError}" scope="request"/>
    <i>Error. Data is temporarily unavailable</i>
</c:if>

