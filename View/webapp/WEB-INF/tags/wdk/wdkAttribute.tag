<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="attributeValue"
              type="org.gusdb.wdk.model.record.attribute.AttributeValue"
              required="true"
              description="the attribute value to be rendered"
%>

<%@ attribute name="truncate"
              required="false"
              description="truncate the result"
%>

<%@ attribute name="columnName"
              required="false"
              description="name of the column"
%>

<%@ attribute name="recordClass"
              type="org.gusdb.wdk.model.jspwrap.RecordClassBean"
              required="true"
              description="The full name of the record class, to be used to render primary key attribute"
%>

<c:set var="toTruncate" value="${truncate != null && truncate == 'true'}" />
<c:set var="attributeField" value="${attributeValue.attributeField}" />
<c:set var="align" value="align='${attributeField.align}'" />
<c:set var="nowrap">
  <c:if test="${attributeField.nowrap}">white-space:nowrap;</c:if>
</c:set>

<!-- truncate is set to true in wdk/resultTable.tag (default is 100)
     we use briefDisplay to access display value when available
-->
<!-- attributeValue.value is "text" in textAttributes but briefDisplay will return the display value
-->
<c:set var="displayValue1">
  <c:choose>
    <c:when test="${toTruncate}">${attributeValue.briefDisplay}</c:when>
    <c:otherwise>${attributeValue.value}</c:otherwise>
  </c:choose>
</c:set>

<!-- modifying the displayValue for a nicer UX -->
<c:set var="displayValue">
  <imp:updateDisplayValue columnName = "${columnName}" displayValue = "${displayValue1}" />
</c:set>

<td>
  <div class="attribute-summary" ${align} style="${nowrap}padding:3px 2px">
  <c:choose>

<%-- PRIMARY KEY --%>
    <c:when test="${attributeValue.class.name eq 'org.gusdb.wdk.model.record.attribute.IdAttributeValue'}">
      <!-- store the primary key pairs here - used by basket link -->
      <div class="primaryKey" fvalue="${briefValue}" style="display:none;">
        <c:forEach items="${attributeValue.primaryKey.values}" var="pkValue">
          <span key="${pkValue.key}">${pkValue.value}</span>
        </c:forEach>
      </div>

      <!-- display a link to record page, will include the line:  <a href="${recordLink}">${displayValue}</a> -->
      <imp:recordLink
        primaryKeyValue="${attributeValue.primaryKey}"
        recordClass="${recordClass}"
        displayValue = "${displayValue}"
      />
    </c:when>

<%-- LINK ATTRIBUTE --%>
    <c:when test="${attributeValue.class.name eq 'org.gusdb.wdk.model.record.attribute.LinkAttributeValue'}">
      <c:set var="target">
        <c:if test="${attributeField.newWindow}">target="_blank"</c:if>
      </c:set>
      <a ${target} href="${attributeValue.url}">${attributeValue.displayText}</a>
    </c:when>

<%-- OTHER TYPE OF ATTRIBUTE --%>
    <c:otherwise>
      ${displayValue}
    </c:otherwise>
  </c:choose>

</div>
</td>
