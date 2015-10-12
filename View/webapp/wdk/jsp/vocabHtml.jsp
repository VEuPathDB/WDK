<%--

HTML for Ajax dependent params

--%>
<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="qP" value="${requestScope.vocabParam}" />
<c:set var="useHtmlFormWrapper" value="${not qP.multiPick || qP.displayType ne 'treeBox'}"/>

<c:if test="${useHtmlFormWrapper}">
  <html:form method="post" enctype='multipart/form-data' action="/processQuestion.do">
    <imp:enumParamInput qp="${qP}" />
  </html:form>
</c:if>
<c:if test="${not useHtmlFormWrapper}">
  <imp:enumParamInput qp="${qP}" />
</c:if>
