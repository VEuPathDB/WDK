
<c:choose>
<c:when test="${fn:length(qP.vocab) > 2}">
	<a class="small" href="javascript:void(0)" onclick="chooseAll(1, $(this).parents('form').get(0), 'array(${pNam})' )">Select All</a>
 	| <a class="small" href="javascript:void(0)" onclick="chooseAll(0, $(this).parents('form').get(0), 'array(${pNam})' )">Clear All</a>
</c:when>
</c:choose>
