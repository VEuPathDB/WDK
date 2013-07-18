<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false"
      description="Page calling this tag"/>

  <c:set var="model" value="${applicationScope.wdkModel}" />

  <c:forEach items="${model.websiteRootCategories}" var="item">
    <c:set var="searchCategoryNode" value="${item.value}" scope="request" />
    <c:import url="/WEB-INF/includes/searchCategoryNode.jsp"/>
  </c:forEach>

</jsp:root>
