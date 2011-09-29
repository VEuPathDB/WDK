<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%-- Take recursive variable and set locally to maintain call stack state --%>
<c:set var="node" value="${recurse_term_node}"/>

<a href="#" style="cursor:default"><span style="cursor:text">${node.displayName}</span></a>
<ul>
  <c:forEach var="childNode" items="${node.childNodes}">
    <li id="${childNode.name}">
      <c:set var="recurse_term_node" value="${childNode}" scope="request"/>
      <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp"/>
    </li>
  </c:forEach>
  <c:forEach var="leaf" items="${node.leafNodes}">
    <li class="jstree-leaf" rel="leaf" id="${leaf.name}">
      <a href="#" style="cursor:default"><label for="${leaf.name}" title="${leaf.help}"><span style="cursor:text">${leaf.displayName}</span></label></a>
    </li>
  </c:forEach>
</ul>
