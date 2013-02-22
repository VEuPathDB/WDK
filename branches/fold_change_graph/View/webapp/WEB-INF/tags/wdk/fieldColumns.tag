<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="title" required="true" type="String"/>
  <jsp:directive.attribute name="numCols" required="true" type="java.lang.Integer"/>
  <jsp:directive.attribute name="attributes" required="true" type="org.gusdb.wdk.model.jspwrap.FieldBean[]"/>

  <c:set var="numAttribs" value="${fn:length(attributes)}"/>
  <c:set var="numPerColumn" value="${(numAttribs div numCols) + (numAttribs mod numCols eq 0 ? 0 : 1)}"/>
  
  <tr>
    <th colspan="${numCols}">Attributes</th>
  </tr>
  <tr>
    <c:forEach begin="0" end="${numCols - 1}" varStatus="column">
      <td nowrap="nowrap">
        <c:set var="startIndex" value="${numPerColumn * column.index}"/>
        <c:set var="endIndex" value="${(numPerColumn * (column.index + 1)) - 1}"/>
        <c:forEach begin="${startIndex}" end="${endIndex}" varStatus="array">
          <c:if test="${endIndex lt numAttribs}">
            <c:set var="attrib" value="${attributes[array.index]}"/>
            <input type="checkbox" name="o-fields" value="${attrib.name}"/>
            ${empty attrib.displayName ? attrib.name : attrib.displayName}
            <c:if test="${attrib.name eq 'primaryKey'}">ID</c:if>
            <br/>
          </c:if>
        </c:forEach>
      </td>
    </c:forEach>
  </tr>
</jsp:root>
