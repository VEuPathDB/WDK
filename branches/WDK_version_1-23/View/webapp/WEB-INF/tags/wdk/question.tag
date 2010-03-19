<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- get wdkQuestion; setup requestScope HashMap to collect help info for footer --%>
<c:set var="wdkQuestion" value="${requestScope.wdkQuestion}"/>
<jsp:useBean scope="request" id="helps" class="java.util.LinkedHashMap"/>
<c:set var="qForm" value="${requestScope.questionForm}"/>

<%-- display page header with wdkQuestion displayName as banner --%>
<c:set var="wdkModel" value="${applicationScope.wdkModel}"/>
<c:set var="recordType" value="${wdkQuestion.recordClass.type}"/>
<c:set var="showParams" value="${requestScope.showParams}"/>

<c:if test="${fn:endsWith(recordType,'y')}">
	<c:set var="recordType" value="${fn:substring(recordType,0,fn:length(recordType)-1)}ie" />
</c:if>

<%-- show all params of question, collect help info along the way --%>
<c:set value="Help for question: ${wdkQuestion.displayName}" var="fromAnchorQ"/>
<jsp:useBean id="helpQ" class="java.util.LinkedHashMap"/>

<c:choose>
    <c:when test="${showParams == true}">
        <%-- display params section only --%>
        <html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processQuestion.do">
            <input type="hidden" name="questionFullName" value="${wdkQuestion.fullName}"/>
            <wdk:questionParams />
        </html:form>
    </c:when>
    <c:otherwise>
        <%-- display question section --%>
<h1>Identify ${recordType}s based on ${wdkQuestion.displayName}</h1>

<table width=100%> 
<tr>
<td bgcolor=white valign=top>

<%-- put an anchor here for linking back from help sections --%>
<A name="${fromAnchorQ}"></A>

<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processQuestion.do">
<input type="hidden" name="questionFullName" value="${wdkQuestion.fullName}"/>

<!-- show error messages, if any -->
<wdk:errors/>

<%-- the js has to be included here in order to appear in the step form --%>
<script type="text/javascript" src='<c:url value="/wdk/js/wdkQuestion.js"/>'></script>
<c:if test="${showParams == null}">
            <script type="text/javascript">
              $(document).ready(function() { initParamHandlers(); });
            </script>
</c:if>

<div class="params">
   <c:if test="${showParams == null}">
      <wdk:questionParams />
   </c:if>
</div> <%-- end of params div --%>

<c:set target="${helps}" property="${fromAnchorQ}" value="${helpQ}"/>

<%-- set the weight --%>
<div align="center">
  Assign Weight: <input type="text" name="weight" value="${weight}">
</div>

<div class="filter-button"><html:submit property="questionSubmit" value="Get Answer"/></div>
</html:form>

</td>
</tr>
</table>

<hr>

<c:set var="descripId" value="query-description-section"/>

<%-- display description for wdkQuestion --%>
<div id="${descripId}"><b>Query description: </b><jsp:getProperty name="wdkQuestion" property="description"/></div>
<%--
</tr>
</table>
--%> 
    </c:otherwise> <%-- otherwise of showParams == true --%>
</c:choose>
