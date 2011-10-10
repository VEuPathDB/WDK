<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

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

