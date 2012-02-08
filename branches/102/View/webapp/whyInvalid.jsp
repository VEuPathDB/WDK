<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>


<%-- we need the header only for the css and js --%>
<imp:header title="${site}.org :: Support"
                 banner="Support"
                 parentDivision="${site}"
                 parentUrl="/home.jsp"
                 divisionName="Generic"
                 division="help"/>


<style type="text/css">
#contentcolumn2 {
   min-width:0;
}
</style>


<div style="font-size:20px;font-family:Arial;text-align:right">
<a href="javascript:window.close()">Close (X)</a>  
</div>

<div style="font-size:14px;font-family:Arial">
<br><br>
About every 2 months we release new data in our databases and this might affect your existing strategies.

<br><br>Some of the searches in your strategies might have been updated: either a parameter was modified or removed or we added a new parameter in the search. 
<br><br>When this happens, we will mark your strategy with the red icon, and you will need to (1) open the strategy, and (2) revise the step(s) whose search has changed.

<br><br>Independently, the result of any step in your strategies (a set of IDs) might change due to the new data. Please be aware that we do not warn you when this happens, and that we cannot recover your old result.
</div>



<imp:footer/>


