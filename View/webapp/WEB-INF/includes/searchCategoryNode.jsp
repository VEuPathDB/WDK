<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <!-- initially websiteRootCategories-->
  <c:set var="category" value="${requestScope.searchCategoryNode}"/>
  <c:set var="categories" value="${category.websiteChildren}" />
  <c:set var="questions" value="${category.websiteQuestions}" />

  <c:set var="children">
    <!-- display sub categories -->
    <c:forEach items="${categories}" var="item">
      <c:set var="searchCategoryNode" value="${item.value}" scope="request" />
      <c:import url="/WEB-INF/includes/searchCategoryNode.jsp"/>
    </c:forEach>

    <!-- display sub questions -->
    <c:forEach items="${questions}" var="question">
      <li>
        <c:url var="questionUrl" value="/showQuestion.do?questionFullName=${question.fullName}"/>
        <a href="${questionUrl}"><span>${question.displayName}</span></a>
        <imp:questionFeature question="${question}" />
      </li>
    </c:forEach>
  </c:set>

  <c:choose>
    <c:when test="${category.flattenInMenu}">${children}</c:when>

    <c:otherwise>
      <li><a class="parent category"><span>${category.displayName}</span></a>
        <ul>${children}</ul>
      </li>
    </c:otherwise>
  </c:choose>
</jsp:root>
