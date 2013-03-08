<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="refer" 
                          type="java.lang.String"
                          required="false" 
                          description="Page calling this tag"
%>


<c:set var="model" value="${applicationScope.wdkModel}" />

  <c:forEach items="${model.websiteRootCategories}" var="item">
    <c:set var="searchCategoryNode" value="${item.value}" scope="request" />
    <c:import url="/WEB-INF/includes/searchCategoryNode.jsp"/>
  </c:forEach>

