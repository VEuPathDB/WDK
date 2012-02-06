<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%-- Take recursive variable and set locally to maintain call stack state --%>
<c:set var="node" value="${recurse_term_node}"/>

<a href="#">${node.displayName}</a>
<ul>
  <c:forEach var="category" items="${node.subCategories}">
    <li>
      <c:set var="recurse_term_node" value="${category}" scope="request"/>
      <c:import url="/WEB-INF/includes/attributeCategoryNode.jsp" />
    </li>
  </c:forEach>
  <c:forEach var="attribute" items="${node.fields}">
    <li class="jstree-leaf" rel="file">
      <c:set var="inputProps" value=""/>
			<c:forEach items="${attributeTreeWdkAnswer.summaryAttributes}" var="summary">
			  <c:if test="${attribute.name eq summary.name}">
			    <c:set var="inputProps" value="checked" />
			    <c:if test="${attribute.removable == false}">
			      <c:set var="inputProps" value="${inputProps} disabled" />
			    </c:if>
			  </c:if>
			</c:forEach>
      <input id="${attribute.name}" type="checkbox" name="${attributeTreeCheckboxName}" value="${attribute.name}" title="${attribute.help}" ${inputProps} />
      <label for="${attribute.name}" title="${attribute.help}">${attribute.displayName}</label>
    </li>
  </c:forEach>
</ul>
