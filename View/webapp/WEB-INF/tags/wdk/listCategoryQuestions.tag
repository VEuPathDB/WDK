<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="category"
             type="org.gusdb.wdk.model.jspwrap.CategoryBean"
              required="true"
              description="The category to display"
%>

<c:forEach items="${category.websiteChildren}" var="catEntry">
  <c:set var="cat" value="${catEntry.value}" />
  <c:if test="${category.multiCategory}">
    <li><a class="category" href="javascript:void(0)">${cat.displayName}</a>
      <ul>
  </c:if>
  <c:forEach items="${cat.websiteQuestions}" var="q">
    <li><a href="javascript:getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">${q.displayName}</a></li>
  </c:forEach>
  <c:if test="${category.multiCategory}">
      </ul>
    </li>
  </c:if>
</c:forEach>
