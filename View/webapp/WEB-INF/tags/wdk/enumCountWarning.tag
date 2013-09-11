<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">

  <jsp:directive.attribute name="enumParam" required="true"
    type="org.gusdb.wdk.model.jspwrap.EnumParamBean"
    description="Enum param to display warning for"/>

  <jsp:directive.attribute name="initialCount" required="true"
    type="java.lang.Integer"
    description="Number to display as initial count"/>

  <!-- If min or max selected value is set, display warning -->
  <c:if test="${enumParam.maxSelectedCount ge 0 or enumParam.minSelectedCount ge 0}">
    <div class="enum-param-size-warning" style="color:blue">
      <c:choose>
        <c:when test="${enumParam.maxSelectedCount ge 0 and enumParam.minSelectedCount lt 0}">
          Note: You may only select up to ${enumParam.maxSelectedCount} values for this parameter.
        </c:when>
        <c:when test="${enumParam.maxSelectedCount lt 0 and enumParam.minSelectedCount ge 0}">
          Note: You must select at least ${enumParam.minSelectedCount} values for this parameter.
        </c:when>
        <c:when test="${enumParam.maxSelectedCount ge 0 and enumParam.minSelectedCount ge 0}">
          Note: You must select between ${enumParam.minSelectedCount} and ${enumParam.maxSelectedCount} values (inclusive) for this parameter.
        </c:when>
      </c:choose>
      <br/>There are currently <span class="currentlySelectedCount">${initialCount}</span> selected values.
    </div>
  </c:if>

</jsp:root>
