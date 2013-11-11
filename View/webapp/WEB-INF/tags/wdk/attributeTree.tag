<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="wdkAnswer"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              description="the AnswerValueBean for this attribute list" %>

<%@ attribute name="treeObject"
              required="true"
              type="org.gusdb.wdk.model.record.attribute.AttributeCategoryTree"
              description="the AttributeCategoryTree to render" %>

<%@ attribute name="checkboxName"
              required="true"
              description="name of the checkboxes indicating attribute inclusion" %>
              
<c:set var="attributeTree" value="${treeObject}"/>
<c:set var="attributeTreeWdkAnswer" value="${wdkAnswer}" scope="request"/>
<c:set var="attributeTreeCheckboxName" value="${checkboxName}" scope="request"/>

<script type="text/javascript">
    $(function() {
      $('.catTree')
        .jstree({
          "core" : { "initially_open" : [ "root" ] },
          "plugins" : [ "themes", "html_data", "types" ],
          "themes" : { "theme" : "classic" },
          "types" : { "types" : { "file" : { "icon" : { "image" : "<c:url value='/wdk/lib/jstree/graphFileIcon-16x16.gif'/>" }}}}
      });
    });
</script>    

<div class="catTree">
  <ul>
    <c:forEach var="node" items="${attributeTree.topLevelCategories}">
      <li>
        <c:set var="recurse_term_node" value="${node}" scope="request"/>
        <c:import url="/WEB-INF/includes/attributeCategoryNode.jsp" />
      </li>
    </c:forEach>
  </ul>
</div>
