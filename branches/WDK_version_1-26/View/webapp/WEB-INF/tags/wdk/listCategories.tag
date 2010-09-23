<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%@ attribute name="category"
              required="false"
              description="If a category name is provided, only display that category."
%>

<c:set var="model" value="${applicationScope.wdkModel}" />
<c:set var="categories" value="${model.websiteRootCategories}" />

<ul class="top_nav">
  <c:choose>
    <c:when test="${category != null}">
      <wdk:listCategoryQuestions category="${categories[category]}" />
    </c:when>
    <c:otherwise>
      <c:forEach items="${categories}" var="category">
        <li>${category.value.displayName}
          <ul>
           <wdk:listCategoryQuestions category="${category.value}" />
          </ul>
        </li>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</ul>
