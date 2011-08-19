<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="rootNode"
              required="true"
              type="org.gusdb.wdk.model.TreeNode"
              description="the root of the tree to render" %>

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

<%@ attribute name="useIcons"
              required="false"
              type="java.lang.Boolean"
              description="whether or not to display icons as part of the tree structure" %>
              
<c:if test="${empty useIcons}" >
  <c:set var="useIcons" value="false"/>
</c:if>

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
  $(function() {
    $('.checkboxTree')
      .bind("loaded.jstree", function (event, data) {
        // need to check all selected nodes, but wait to ensure page is ready
        reselectDefaultNodes();
      })
      .jstree({
        "plugins" : [ "html_data", "themes", "types", "checkbox" ],
        "core" : { "initially_open" : [ "root" ] },
        "themes" : { "theme" : "classic", "icons" : "false" },
        "types" : { "types" : { "leaf" : { "icon" : { "image" : "<c:url value='${leafImage}'/>" }}}},
        "checkbox" : {
          "real_checkboxes" : true,
          "real_checkboxes_names" : function(node) { return ["${checkboxName}", (node[0].id || "")]; }
        }
      });
  });
  function reselectDefaultNodes() {
	  uncheckAll();
    var checkedArray = [${rootNode.selectedAsList}];
    for (i = 0; i < checkedArray.length; i++) {
      var nodeId = '#' + checkedArray[i];
      $('.checkboxTree').jstree("check_node", nodeId);
    }
  }
  function checkAll() {
	  $('.checkboxTree').jstree("check_all");
	}
  function uncheckAll() {
	  $('.checkboxTree').jstree("uncheck_all");
	}
  function expandAll() {
  	$('.checkboxTree').jstree("open_all", -1, true);
  }
  function collapseAll() {
  	$('.checkboxTree').jstree("close_all", -1, true);
  }
</script>    

<div class="formButtonPanel">
  <a class="small" href="javascript:void(0)" onclick="expandAll();">Expand All</a> | 
  <a class="small" href="javascript:void(0)" onclick="collapseAll();">Collapse All</a> |
  <c:if test="${showSelectAll}">
    <a class="small" href="javascript:void(0)" onclick="checkAll();">Select All</a> |
  </c:if>
  <a class="small" href="javascript:void(0)" onclick="uncheckAll();">Clear All</a> |
  <a class="small" href="javascript:void(0)" onclick="reselectDefaultNodes();">Reset to Current</a>
</div>
<div class="checkboxTree">
  <c:set var="recurse_term_node" value="${rootNode}" scope="request"/>
  <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp" />
</div>
<div class="formButtonPanel">
  <a class="small" href="javascript:void(0)" onclick="expandAll();">Expand All</a> | 
  <a class="small" href="javascript:void(0)" onclick="collapseAll();">Collapse All</a> |
  <c:if test="${showSelectAll}">
    <a class="small" href="javascript:void(0)" onclick="checkAll();">Select All</a> |
  </c:if>
  <a class="small" href="javascript:void(0)" onclick="uncheckAll();">Clear All</a> |
  <a class="small" href="javascript:void(0)" onclick="reselectDefaultNodes();">Reset to Current</a>
</div>

<c:remove var="recurse_term_node"/>
