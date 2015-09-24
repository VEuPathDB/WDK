<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%-- Take recursive variable and set locally to maintain call stack state --%>
<c:set var="node" value="${recurseTermNode}"/>

<a href="javascript:void(0)" style="cursor:default"><span style="cursor:text">${node.contents.displayName}</span></a>
<ul>

  <!-- Pushes leaves to the 'bottom' at each level of the tree -->
  <c:if test="${segregateLeavesParam}">
	  <c:forEach var="nonLeafNode" items="${node.nonLeafNodes}">
	    <li id="${nonLeafNode.contents.name}">
	      <c:set var="recurseTermNode" value="${nonLeafNode}" scope="request"/>
	      <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp"/>
	    </li>
	  </c:forEach>
	  <c:forEach var="leafNode" items="${node.leafNodes}">
	    <li class="jstree-leaf" rel="leaf" id="${leafNode.contents.name}">
	      <a href="javascript:void(0)" style="cursor:default"><c:if test="${useHelpParam}"><label for="${leafNode.contents.name}" title="${leafNode.contents.help}"></c:if><span style="cursor:text">${leafNode.contents.displayName}</span><c:if test="${useHelpParam}"></label></c:if></a>
	    </li>
	  </c:forEach>
	</c:if>
	
	<!-- Uses straight ordering of nodes (potentially mixing leaf and non-leaf nodes together) -->
	<c:if test="${not segregateLeavesParam}">
    <c:forEach var="childNode" items="${node.childNodes}">
      <c:if test="${childNode.leaf}">
        <li class="jstree-leaf" rel="leaf" id="${childNode.contents.name}">
          <a href="javascript:void(0)" style="cursor:default"><c:if test="${useHelpParam}"><label for="${childNode.contents.name}" title="${childNode.contents.help}"></c:if><span style="cursor:text">${childNode.contents.displayName}</span><c:if test="${useHelpParam}"></label></c:if></a>
        </li>
      </c:if>
      <c:if test="${not childNode.leaf}">
        <li id="${childNode.contents.name}">
          <c:set var="recurseTermNode" value="${childNode}" scope="request"/>
          <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp"/>
        </li>
      </c:if>
    </c:forEach>
	</c:if>
</ul>
