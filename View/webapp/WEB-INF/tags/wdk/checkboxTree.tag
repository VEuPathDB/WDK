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

<%@ attribute name="showResetCurrent"
              required="false"
              type="java.lang.Boolean"
              description="whether or not to show a 'reset to current' link" %>
              
<c:if test="${empty showResetCurrent}">
  <c:set var="showResetCurrent" value="false"/>
</c:if>

<%-- if 'show current' link is activated, then check current nodes on init; else check default --%>
<c:if test="${showResetCurrent}">
  <c:set var="initialSetMethod" value="selectCurrentNodes"/>
</c:if>
<c:if test="${not showResetCurrent}">
  <c:set var="initialSetMethod" value="selectDefaultNodes"/>
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
	  configureCheckboxTree();
  }
  function configureCheckboxTree() {
    $('.checkboxTree')
      .bind("loaded.jstree", function (event, data) {
        // need to check all selected nodes, but wait to ensure page is ready
        ${initialSetMethod}();
      })
      .jstree({
        "plugins" : [ "html_data", "themes", "types", "checkbox" ],
        "core" : { "initially_open" : [ "root" ] },
        "themes" : { "theme" : "classic", "icons" : ${useIcons} },
        "types" : { "types" : { "leaf" : { "icon" : { "image" : "<c:url value='${leafImage}'/>" }}}},
        "checkbox" : {
          "real_checkboxes" : true,
          "real_checkboxes_names" : function(node) { return ["${checkboxName}", (node[0].id || "")]; }
        }
      });
  });
  function selectCurrentNodes() {
    var currentNodes = [${rootNode.selectedAsList}];
    selectListOfNodes(currentNodes);
  }
  function selectDefaultNodes() {
	  var defaultNodes = [${rootNode.defaultAsList}];
	  selectListOfNodes(defaultNodes);
  }
  function selectListOfNodes(checkedArray) {
    uncheckAll();
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

<div class="formButtonPanel" style="text-align:${buttonAlignment}">
  <a class="small" href="javascript:void(0)" onclick="expandAll();">Expand All</a>
  | <a class="small" href="javascript:void(0)" onclick="collapseAll();">Collapse All</a>
  <c:if test="${showSelectAll}">
    | <a class="small" href="javascript:void(0)" onclick="checkAll();">Select All</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="uncheckAll();">Clear All</a>
  <c:if test="${showResetCurrent}">
    <br/><a class="small" href="javascript:void(0)" onclick="selectCurrentNodes();">Reset to Current</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="selectDefaultNodes();">Reset to Default</a>
</div>
<div class="checkboxTree">
  <c:set var="recurse_term_node" value="${rootNode}" scope="request"/>
  <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp" />
</div>
<div class="formButtonPanel" style="text-align:${buttonAlignment}">
  <a class="small" href="javascript:void(0)" onclick="expandAll();">Expand All</a>
  | <a class="small" href="javascript:void(0)" onclick="collapseAll();">Collapse All</a>
  <c:if test="${showSelectAll}">
    | <a class="small" href="javascript:void(0)" onclick="checkAll();">Select All</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="uncheckAll();">Clear All</a>
  <c:if test="${showResetCurrent}">
    <br/><a class="small" href="javascript:void(0)" onclick="selectCurrentNodes();">Reset to Current</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="selectDefaultNodes();">Reset to Default</a>
</div>

<c:remove var="recurse_term_node"/>
