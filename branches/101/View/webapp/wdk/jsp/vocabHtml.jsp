<%--

HTML for Ajax dependent params

--%>
<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="qP" value="${requestScope.vocabParam}" />
<c:set var="useHtmlFormWrapper" value="${qP.displayType ne 'treeBox'}"/>

<c:if test="${useHtmlFormWrapper}">
  <html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processQuestion.do">
    <wdk:enumParamInput qp="${qP}" />
  </html:form>
</c:if>
<c:if test="${not useHtmlFormWrapper}">
  <wdk:enumParamInput qp="${qP}" />
</c:if>
