<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">
  $(function() { wdk.tooltips.assignParamTooltips('.help-link'); });
</script>

<c:set var="nameHelp" value="Give this search strategy a custom name. The name will appear in the first step box (truncated to 15 characters)." />
<c:set var="weightHelp" value="Give this search a weight (for example 10, 200, -50, integer only). It will show in a column in your result. In a search strategy, unions and intersects will sum the weights, giving higher scores to items found in multiple searches. Default weight is 10." />

<div style="text-align:center;padding-top:0.5em;">
  <imp:image class="help-link" style="cursor:pointer" title="${nameHelp}" src="wdk/images/question.png"  />

	<input style="width:16em" 
         title="${nameHelp}"
         type="text"  
         placeholder="Give this search a name (optional)"  
         name="customName"  
         value="${customName}" 
	/>
  <br>
  <imp:image class="help-link" style="cursor:pointer" title="${weightHelp}" src="wdk/images/question.png"  />
  <input style="width:16em" 
         title="${weightHelp}"
         type="text"  
         name="weight"
         placeholder="Give this search a weight (optional)"   
	/>
</div>


