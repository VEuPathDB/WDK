
<c:choose>
<c:when test="${fn:length(qP.vocab) > 2}">
	<a class="small" href="javascript:void(0)" onclick="wdk.chooseAll(1, $(this).parents('form').get(0), 'array(${pNam})' )">select all</a>
 	| <a class="small" href="javascript:void(0)" onclick="wdk.chooseAll(0, $(this).parents('form').get(0), 'array(${pNam})' )">clear all</a>
</c:when>
</c:choose>
