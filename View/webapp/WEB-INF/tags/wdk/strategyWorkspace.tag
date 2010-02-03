<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%@ attribute name="includeDYK"
              required="true"
%>

  <div id="contentwrapper">
    <div id="contentcolumn2">
      <div class="innertube">

<c:if test="${includeDYK}">
  <wdk:dyk />
</c:if>

<%--
<div id="strategy_workspace" class="h2center">
My Search Strategies Workspace
</div>
--%>

<ul id="strategy_tabs">
<%-- showPanel() is in filter_menu.js --%>

   <li><a id="tab_strategy_new" title="START a NEW strategy: CLICK to access the page with all available searches"   
	href="javascript:showPanel('strategy_new')" ><span style="font-weight:bold;color:black">Searched Strategies:</span> New</a></li>
   <li><a id="tab_strategy_results" title="Graphical display of your opened strategies. To close a strategy click on the right top corner X." 
	onclick="this.blur()" href="javascript:showPanel('strategy_results')">Opened</a></li>
   <li><a id="tab_search_history" title="Summary of all your strategies. From here you can open/close strategies on the 'Run Strategies' tab, our graphical display." 
	onclick="this.blur()" href="javascript:showPanel('search_history')">All</a></li>

<%--
   <li><a style="padding-left:5px;" id="tab_basket" title="Where to store your GENES of interest. YOU NEED TO LOGIN to use the basket. In the future you will be able to have baskets for other feature types such as ESTs, SNPs, genomic sequences, etc." onclick="this.blur()" href="javascript:showPanel('basket')"><img class="basket" src="<c:url value='/wdk/images/basket_gray.png'/>" width="15" height="15"/>&nbsp;My Basket</a></li>
--%>

   <li><a id="tab_sample_strat"  onclick="this.blur()" title="View some examples of linear and non-linear strategies." 
	href="javascript:showPanel('sample_strat')">Examples</a></li>
   <li><a id="tab_help" href="javascript:showPanel('help')"  title="List of hints on how to use our website, also available in the Did You Know popup">Help</a></li>

</ul>





<%--------------- REST OF PAGE ---------------%>

<c:set var="newStrategy" value="${requestScope.newStrategy}" />
<c:set var="newStrat"><c:if test="${newStrategy != null && newStrategy == true}">newStrategy="true"</c:if></c:set>

<div id="strategy_results">
	<div id="Strategies" ${newStrat}>
	</div>

	<br/>

	<div class="Workspace">&nbsp;
	</div> 

</div>

<div id="search_history">
</div>

<div id="basket">
  <wdk:basketControls />
  <div class="Workspace">
  </div>
</div>

<div id="sample_strat">
</div>


<div id="help" style="display:none">
</div>

<div id="strategy_new" style="display:none">
</div>

      </div>
    </div>
  </div>
