<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="attribute"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AttributeFieldBean"
%>

<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="plugins" value="${attribute.attributePlugins}" />
<div class="attribute-plugins">
  <c:if test="${fn:length(plugins) > 0}">
    <div class="open-handle" onclick="showAttributePlugins(this)">
      <image src="wdk/images/dropdown_active.gif" />
    </div>
  </c:if>
  <div class="plugins>
    <div class="close-handle" onclick="hideAttributePlugins(this)">
      <image src="wdk/images/close.gif" />
    </div>
    <c:forEach items=${plugins} var="item">
      <c:set var="plugin" value="${item.value}" />
       <div class="plugin" onclick="invokeAttributePlugin('${step.stepId}', '${attribute.name}', '${plugin.name}')">
        ${plugin.display}
      </div>
    </c:forEach>
  </div>
</div>
