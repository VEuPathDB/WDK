<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="attribute"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AttributeFieldBean"
%>

<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="plugins" value="${attribute.attributePlugins}" />
<c:set var="divId" value="${attribute.name}_${step.stepId}_div"/>

<%-- If only one plugin, then give simple tooltip and have button click launch plugin --%>
<c:if test="${fn:length(plugins) eq 1}">
  <script type="text/javascript">
    $(function() {
   	  $(".jqbutton").button();
      // not working for some reason, but ok; probably better that the native
      //   tooltip looks different than the "multiple-plugin" qtip tooltip 
   	  //assignTooltips('#'+'${divId}', { tipPos: 'top-right', targetPos: 'bottom-right' });
    });
  </script>
  <%-- even though we know there's only one element, can only access 'first' element of a map in a loop --%>
  <c:forEach items="${plugins}" var="item">
    <c:set var="plugin" value="${item.value}" />
    <div>
      <input type="image" id="${divId}" class="jqbutton" src="${assetsUrl}/wdk/images/plugin.png"
             plugin="${plugin.name}" plugintitle="${plugin.display}"
             title="Analyze/Graph the contents of this column by ${fn:toLowerCase(plugin.display)}"
             onclick="wdk.resultsPage.invokeAttributePlugin(this, '${step.stepId}', '${attribute.name}')" />
    </div>
  </c:forEach>
</c:if>

<%-- If >1 plugin, then tooltip will be "dropdown" of  --%>
<c:if test="${fn:length(plugins) > 1}">
	<script type="text/javascript">
	  $(function() {
	    $(".jqbutton").button();
	    wdk.tooltips.assignStickyTooltipByTitle('#'+'${divId}', { tipPos: 'top-right', targetPos: 'bottom-right' });
	  });
	</script>
  <div>
    <c:set var="tipContents">
		  <div>
		    <div>
		      <h4 style="white-space:nowrap; margin-left:0px; margin-bottom:6px">Analyze/Graph the contents of this column</h4>
		    </div>
		    Graph by:<br/>
		    <ul style="margin-top: 4px; margin-left: 6px">
		      <c:forEach items="${plugins}" var="item">
		        <c:set var="plugin" value="${item.value}" />
		        <li>- 
		          <a href="javascript:void(0)" onclick="wdk.resultsPage.invokeAttributePlugin(this, '${step.stepId}', '${attribute.name}')"
		             plugin="${plugin.name}" plugintitle="${plugin.display}" title="${plugin.description}">
		            ${plugin.display}
		          </a>
		        </li>
		      </c:forEach>
		    </ul>
		  </div>    
    </c:set>
    <input type="image" id="${divId}" class="jqbutton" title="${fn:escapeXml(tipContents)}"
           src="${assetsUrl}/wdk/images/plugin.png" x-title="Analyze/graph the contents of this column"/>
  </div>
</c:if>

