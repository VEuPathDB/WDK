<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="tree"
              required="true"
              type="org.gusdb.wdk.model.FieldTree"
              description="the root of the tree to render" %>

<%@ attribute name="id"
              required="true"
              type="java.lang.String"
              description="unique name of this checkbox tree within the page" %>

<%@ attribute name="checkboxName"
              required="true"
              type="java.lang.String"
              description="name of the checkboxes indicating attribute inclusion" %>

<%@ attribute name="onchange"
              required="false"
              type="java.lang.String"
              description="value for onchange event on to be set on individual checkboxes" %>

<%@ attribute name="onload"
              required="false"
              type="java.lang.String"
              description="value for onload event to run after tree is loaded and configured" %>

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
  <c:set var="initiallySetList" value="${tree.selectedAsList}"/>
</c:if>
<c:if test="${not showResetCurrent}">
  <c:set var="initiallySetList" value="${tree.defaultAsList}"/>
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

<%@ attribute name="leafImage"
              required="false"
              type="java.lang.String"
              description="image used for leaf nodes within the tree" %>
              
<c:if test="${empty leafImage}" >
  <c:set var="leafImage" value="/wdk/lib/jstree/graphFileIcon-16x16.gif"/>
  <%--<c:set var="leafImage" value="/wdk/lib/jstree/clear2x16.gif"/>--%>
</c:if>

<%@ attribute name="segregateLeaves"
              required="false"
              type="java.lang.Boolean"
              description="if true, push leaf nodes to the 'bottom' at each level of the tree; else (default) maintain TreeNode ordering"%>
    
<c:if test="${empty segregateLeaves}">
  <c:set var="segregateLeaves" value="false"/>
</c:if>

<%-- Must set the following as request scope vars so included jsp has access (unset at bottom) --%>

<c:set var="recurseTermNode" value="${tree.root}" scope="request"/>
<c:set var="useHelpParam" value="${useHelp}" scope="request"/>
<c:set var="segregateLeavesParam" value="${segregateLeaves}" scope="request"/>

<!-- JSTree/Checkbox configuration -->
<div style="display:none" data-controller="wdk.checkboxTree.setUpCheckboxTree"
    data-id="${id}" data-name="${checkboxName}" data-useicons="${useIcons}"
    data-isallselected="${tree.allLeavesSelected}" data-leafimage="${leafImage}"
    data-selectednodes="[${tree.selectedAsList}]" data-defaultnodes="[${tree.defaultAsList}]"
    data-initialnodes="[${initiallySetList}]" data-onload="${onload};"
    data-onchange="setTimeout(function() { ${onchange}; }, 0);"><jsp:text/></div>

<div id="treeLinks-top" class="formButtonPanel" style="text-align:${buttonAlignment}">
  <c:if test="${showSelectAll}">
    <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.checkAll('${id}');">select all</a> |
  </c:if>
  <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.uncheckAll('${id}');">clear all</a> |
  <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.expandAll('${id}');">expand all</a> |
  <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.collapseAll('${id}');">collapse all</a>
  <c:if test="${showResetCurrent}">
    <br/><a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.selectCurrentNodes('${id}');">reset to current</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.selectDefaultNodes('${id}');">reset to default</a>
</div>
<div class="checkbox-tree" id="${id}" style="display:none; text-align:left">
  <c:import url="/WEB-INF/includes/checkboxTreeNode.jsp" />
</div>
<div id="treeLinks-bottom" class="formButtonPanel" style="text-align:${buttonAlignment}">
  <c:if test="${showSelectAll}">
    <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.checkAll('${id}');">select all</a> |
  </c:if>
  <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.uncheckAll('${id}');">clear all</a> |
  <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.expandAll('${id}');">expand all</a> |
  <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.collapseAll('${id}');">collapse all</a>
  <c:if test="${showResetCurrent}">
    <br/><a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.selectCurrentNodes('${id}');">reset to current</a>
  </c:if>
  | <a class="small" href="javascript:void(0)" onclick="wdk.checkboxTree.selectDefaultNodes('${id}');">reset to default</a>
</div>

<c:remove var="recurseTermNode"/>
<c:remove var="useHelpParam"/>
<c:remove var="segregateLeavesParam"/>
