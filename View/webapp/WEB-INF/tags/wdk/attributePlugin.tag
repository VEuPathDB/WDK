<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="attribute"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AttributeFieldBean"
%>

<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="plugins" value="${attribute.attributePlugins}" />
<c:if test="${fn:length(plugins) > 0}">
	<script type="text/javascript">
	  $(function() {
	    $(".jqbutton").button();
	    assignStickyTooltipByTitle('.attribPluginTip', { tipPos: 'top-right', targetPos: 'bottom-right' });
	  });
	</script>
  <div>
    <c:set var="tipContents">
		  <div>
		    <div>
		      <h4 style="white-space:nowrap; margin-left:0">Analyze/Graph the contents of this column</h4>
		    </div>
		    <ul style="margin-top: 4px;">
		      <c:forEach items="${plugins}" var="item">
		        <c:set var="plugin" value="${item.value}" />
		        <li>- 
		          <a style="color:#8F0165; cursor:pointer" plugin="${plugin.name}" title="${plugin.description}"
		            onclick="invokeAttributePlugin(this, '${step.stepId}', '${attribute.name}')">
		            ${plugin.display}
		          </a>
		        </li>
		      </c:forEach>
		    </ul>
		  </div>    
    </c:set>
    <input type="image" class="jqbutton attribPluginTip" title="${fn:escapeXml(tipContents)}"
           src="wdk/images/plugin.png" title="Analyze/graph the contents of this column"/>
  </div>
</c:if>
