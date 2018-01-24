<%--
Provides form input element for a given StringParam.

--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.StringParamBean"
              required="true"
              description="parameter name"
%>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="length" value="${qP.length}"/>

<c:set var="size" value="${qP.number ? 15 : 45}"/>

<div class="param stringParam" name="${pNam}">

<script type="x-wdk/validation"><![CDATA[ ${qP.regex} ]]></script>

<c:choose>
  <c:when test="${qP.isVisible == false}">
    <html:hidden property="value(${pNam})"/>
  </c:when>
  <c:when test="${qP.isReadonly}">
    <html:text styleId="${pNam}" property="value(${pNam})" size="${size}" readonly="true" />
  </c:when>
  <c:when test="${length > 50}">
    <html:textarea styleId="${pNam}" property="value(${pNam})" cols="${size}" rows="2" />
  </c:when>
  <c:otherwise>
    <html:text styleId="${pNam}" property="value(${pNam})" size="${size}" />
  </c:otherwise>
</c:choose>

</div>

