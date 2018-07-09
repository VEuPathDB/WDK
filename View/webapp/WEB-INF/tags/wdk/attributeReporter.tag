<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="attribute"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AttributeFieldBean"
%>

<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="reporters" value="${attribute.attributeReporters}" />

<%-- If only one plugin, then give simple tooltip and have button click launch plugin --%>
<c:if test="${fn:length(reporters) eq 1}">
  <%-- even though we know there's only one element, can only access 'first' element of a map in a loop --%>
  <c:forEach items="${reporters}" var="item">
    <c:set var="reporter" value="${item.value}" />
    <c:set var="props">
      {
        "stepId": ${step.stepId},
        "recordClassName": "${step.recordClass.fullName}",
        "reporterName": "${reporter.name}"
      }
    </c:set>
    <div
      data-controller="wdk.clientAdapter"
      data-name="AttributeAnalysisButtonController"
      data-props="${fn:escapeXml(props)}"
    >
      <jsp:text/>
    </div>
  </c:forEach>
</c:if>
