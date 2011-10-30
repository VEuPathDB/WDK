<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="rootNode"
              required="true"
              type="org.gusdb.wdk.model.TreeNode"
              description="the root of the tree to render" %>

<%@ attribute name="id"
              required="true"
              type="java.lang.String"
              description="unique name of this checkbox tree within the page" %>
              
<%@ attribute name="checkboxName"
              required="true"
              type="java.lang.String"
              description="name of the checkboxes indicating attribute inclusion" %>

<%@ attribute name="showSelectAll"
              required="false"
              type="java.lang.Boolean"
              description="whether or not to show a 'select all' link" %>
              
<c:if test="${empty showSelectAll}">
  <c:set var="showSelectAll" value="true"/>
</c:if>

<%@ attribute name="showResetCurrent"
              required="false"
              type="java.lang.Boolean"
              description="whether or not to show a 'reset to current' link" %>
              
<c:if test="${empty showResetCurrent}">
  <c:set var="showResetCurrent" value="false"/>
</c:if>

<%-- if 'show current' link is activated, then check current nodes on init; else check default --%>
<c:if test="${showResetCurrent}">
  <c:set var="initiallySetList" value="${rootNode.selectedAsList}"/>
</c:if>
<c:if test="${not showResetCurrent}">
  <c:set var="initiallySetList" value="${rootNode.defaultAsList}"/>
</c:if>

<%@ attribute name="buttonAlignment"
              required="false"
              type="java.lang.String"
              description="tells how manipulation buttons should be aligned" %>
     
<c:if test="${empty buttonAlignment}">
  <c:set var="buttonAlignment" value="center"/>
</c:if>

<%@ attribute name="useIcons"
              required="false"
              type="java.lang.Boolean"
              description="whether or not to display icons as part of the tree structure" %>
              
<c:if test="${empty useIcons}">
  <c:set var="useIcons" value="false"/>
</c:if>

<%@ attribute name="useHelp"
              required="false"
              type="java.lang.Boolean"
              description="whether or not to display node.help as a tooltip on tree leaves" %>
              
<c:if test="${empty useHelp}">
  <c:set var="useHelp" value="false"/>
</c:if>
<%-- must set as a request scope var so included jsp has access --%>
<c:set var="useHelpParam" value="${useHelp}" scope="request"/>

<%@ attribute name="leafImage"
              required="false"
              type="java.lang.String"
              description="image used for leaf nodes within the tree" %>
              
<c:if test="${empty leafImage}" >
  <c:set var="leafImage" value="/wdk/js/lib/jstree/graphFileIcon-16x16.gif"/>
  <%--<c:set var="leafImage" value="/wdk/js/lib/jstree/clear2x16.gif"/>--%>
</c:if>

<!-- JSTree/Checkbox configuration -->
<script type="text/javascript">
  // configure the tree
  addTreeToPage("${id}", "${checkboxName}", ${useIcons}, ${rootNode.isAllSelected}, "<c:url value='${leafImage}'/>", [${rootNode.selectedAsList}], [${rootNode.defaultAsList}], [${initiallySetList}]);
  $(function() {
	  configureCheckboxTree("${id}");
  });
</script>    

<div class="formButtonPanel" style="text-align:${buttonAlignment}">
  <c:if test="${showSelectAll}">
    <a class="small" href="javascript:void(0)" onclick="cbt_checkAll('${id}');">select all</a> |
  </c:if>
  <a class="small" href="javascript:void(0)" onclick="cbt_uncheckAll('${id}');">clear all</a> |
  <a class="small" href="javascript:void(0)" onclick="cbt_expandAll('${id}');">expand all</a> |
  <a class="small" href="javascript:void(0)" onclick="cbt_collapseAll('${id}');">collapse all</a>
  <c:if test="${showResetCurrent}">
    <br/><a class="small" href="javascript:void(0)" onclick="cbt_selectCurrentNodes('${id}');">reset to current</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="cbt_selectDefaultNodes('${id}');">reset to default</a>
</div>
<div class="checkbox-tree" id="${id}" style="display:none">
  <c:set var="recurse_term_node" value="${rootNode}" scope="request"/>
  <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp" />
</div>
<div class="formButtonPanel" style="text-align:${buttonAlignment}">
  <c:if test="${showSelectAll}">
    <a class="small" href="javascript:void(0)" onclick="cbt_checkAll('${id}');">select all</a> |
  </c:if>
  <a class="small" href="javascript:void(0)" onclick="cbt_uncheckAll('${id}');">clear all</a> |
  <a class="small" href="javascript:void(0)" onclick="cbt_expandAll('${id}');">expand all</a> |
  <a class="small" href="javascript:void(0)" onclick="cbt_collapseAll('${id}');">collapse all</a>
  <c:if test="${showResetCurrent}">
    <br/><a class="small" href="javascript:void(0)" onclick="cbt_selectCurrentNodes('${id}');">reset to current</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="cbt_selectDefaultNodes('${id}');">reset to default</a>
</div>

<c:remove var="recurse_term_node"/>
