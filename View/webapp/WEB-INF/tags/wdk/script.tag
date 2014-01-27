<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core">

  <jsp:directive.tag body-content="empty" dynamic-attributes="dynattrs"/>

  <!-- writes an html <img/> tag with the provided attributes -->
 
  <![CDATA[<script]]>

  <c:forEach items="${dynattrs}" var="a">
    <c:choose>
      <c:when test="${a.key eq 'src'}"> ${a.key}="${applicationScope.assetsUrl}${a.value}" </c:when>
      <c:otherwise> ${a.key}="${a.value}" </c:otherwise>
    </c:choose>
  </c:forEach>

  <![CDATA[></script>]]>

</jsp:root>
