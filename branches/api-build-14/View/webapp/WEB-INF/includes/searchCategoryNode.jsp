<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="category" value="${requestScope.searchCategoryNode}"/>


<li><a class="parent"><span>${category.displayName}</span></a>
 
  <c:set var="categories" value="${category.websiteChildren}" />
  <c:set var="questions" value="${category.websiteQuestions}" />
  <c:if test="${fn:length(categories) != 0 || fn:length(questions) != 0}">
      <ul>
        <%-- display sub categories --%>
        <c:forEach items="${categories}" var="item">
          <c:set var="searchCategoryNode" value="${item.value}" scope="request" />
          <c:import url="/WEB-INF/includes/searchCategoryNode.jsp"/>
        </c:forEach>

        <%-- display sub questions --%>
        <c:forEach items="${questions}" var="question">
          <li>
            <c:url var="questionUrl" value="/showQuestion.do?questionFullName=${question.fullName}"/>
            <a href="${questionUrl}"><span>${question.displayName}</span></a>
            <imp:questionFeature question="${question}" />
          </li>
        </c:forEach>
      </ul>
  </c:if>
</li>
