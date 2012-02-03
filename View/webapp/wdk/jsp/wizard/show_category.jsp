<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%-- an category object is passed in --%>

<nested:define id="categories" property="websiteChildren"/>
<nested:define id="questions" property="websiteQuestions"/>

<div class="category">
  <nested:write property="name"/>
</div>
<ul>
    <c:forEach items="${categories}" var="catItem">
      <c:set var="category" value="${catItem.value}" />
      <li>
        <nested:nest property="firstChildAnswer">
          <jsp:include page="show_category.jsp"/>
        </nested:nest>
      </li>
    </c:forEach>
  
    <c:forEach items="questions" var="question">
      <li>
        <c:url var="questionUrl" value="/wizard.do?wizard=boolean&step=question&question=${question.fullName}" />
        <a href="javascript:callWizard('${questionUrl}')">${question.display}</a>
      </li>
    </c:forEach>
</ul>
