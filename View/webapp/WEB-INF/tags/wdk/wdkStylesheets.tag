<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>
  <jsp:directive.attribute name="debug" required="false" description="Use unminified files"/>

  <c:choose>
    <c:when test="${debug eq true}">
      <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdk.libs.css"/>
      <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdk.css"/>
    </c:when>
    <c:otherwise>
      <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdk.libs.min.css"/>
      <imp:stylesheet rel="stylesheet" type="text/css" href="/wdk/css/wdk.min.css"/>
    </c:otherwise>
  </c:choose>
</jsp:root>
