<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%--
Provides form input element for a given FilterParam.

For a multi-selectable parameter a form element is provided as either a
series of checkboxes or a multiselect menu depending on number of
parameter options. Also, if number of options is over a threshold, this tag
includes a checkAll button to select all options for the parameter.

Otherwise a standard select menu is used.
--%>

<jsp:useBean id="idgen" class="org.gusdb.wdk.model.jspwrap.NumberUtilBean" scope="application" />

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.FilterParamBean"
              required="true"
              description="parameter name"
%>

<%@ attribute name="layout"
              required="false"
              description="parameter name"
%>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>
<c:set var="displayType" value="${qP.displayType}"/>
<c:set var="dependedParams" value="${qP.dependedParamNames}"/>
<c:if test="${dependedParams != null}">
  <c:set var="dependedParam" value="${dependedParams}" />
  <c:set var="dependentClass" value="dependentParam" />
</c:if>
<%-- Setting a variable to display the items in the parameter in a horizontal layout --%>
<c:set var="v" value=""/>
<c:if test="${layout == 'horizontal'}">
  <c:set var="v" value="style='display:inline'"/>
</c:if>

<%-- display the param as an advanced filter param --%>
<div class="param filter-param" data-name="${qp.prompt}" data-data-id="filter-param-{qP.name}">
  <script type="application/json" id="filter-param-{qP.name}">
    ${qP.jsonValues}
  </script>
</div>

<%-- display invalid terms, if any. --%>
<c:set var="originalValues" value="${qP.originalValues}" />
<c:set var="invalid" value="${false}" />
<c:forEach items="${originalValues}" var="entry">
  <c:if test="${entry.value == false}">
    <c:set var="invalid" value="${true}" />
  </c:if>
</c:forEach>

<c:if test="${invalid}">
  <div class="invalid-values">
    <p>Some of the option(s) you previously selected are no longer available.</p>
    <p>Here is a list of the values you selected (unavailable options are marked in red):</p>
    <ul>
      <c:forEach items="${originalValues}" var="entry">
        <c:set var="style">
          <c:if test="${entry.value == false}">class="invalid"</c:if>
        </c:set>
        <li ${style}>${entry.key}</li>
      </c:forEach>
    </ul>
  </div>
</c:if>
