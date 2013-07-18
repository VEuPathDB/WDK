
<c:choose>
<c:when test="${fn:length(qP.vocab) > 2}">
	<br>
 	<a href="javascript:void(0)" onclick="wdk.chooseAll(1, $(this).parents('form').get(0), 'array(${pNam})' )">select all</a>
 	&nbsp;|&nbsp;<a href="javascript:void(0)" onclick="wdk.chooseAll(0, $(this).parents('form').get(0), 'array(${pNam})' )">clear all</a>
</c:when>
</c:choose>

<c:if test="${qP.displayType eq 'treeBox'}">
	&nbsp;|&nbsp; <a href="javascript:void(0)" onclick="expandCollapseAll2(this, true, '${qP.name}')" >expand all</a>
	&nbsp;|&nbsp; <a href="javascript:void(0)" onclick="expandCollapseAll2(this, false, '${qP.name}')" >collapse all</a>
</c:if>
