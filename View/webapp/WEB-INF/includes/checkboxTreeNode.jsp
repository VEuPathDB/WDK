<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%-- Take recursive variable and set locally to maintain call stack state --%>
<c:set var="node" value="${recurse_term_node}"/>

<a href="#">${node.displayName}</a>
<ul>
  <c:forEach var="childNode" items="${node.childNodes}">
    <li>
      <c:set var="recurse_term_node" value="${childNode}" scope="request"/>
      <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp"/>
    </li>
  </c:forEach>
  <c:forEach var="leaf" items="${node.leafNodes}">
    <li class="jstree-leaf" rel="leaf" id="${leaf.name}">
      <a href="#">
        <label for="${leaf.name}" title="${leaf.help}">${leaf.displayName}</label>
      </a>
    </li>
  </c:forEach>
</ul>
