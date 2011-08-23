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
    <image class="handle" onclick="openAttributePlugins(this)" 
           src="wdk/images/plugin.png" title="Graph this column" />
  </c:if>
  <div class="plugins">
    <div class="title">
      <image class="handle close" onclick="closeAttributePlugins(this)" src="wdk/images/close.gif" />
      <h3>Graph this Column</h3>
    </div>
    <ul>
      <c:forEach items="${plugins}" var="item">
        <c:set var="plugin" value="${item.value}" />
        <li class="plugin" plugin="${plugin.name}" title="${plugin.description}"
            onclick="invokeAttributePlugin(this, '${step.stepId}', '${attribute.name}')">
          ${plugin.display}
        </li>
      </c:forEach>
    </ul>
  </div>
</div>
