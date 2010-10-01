<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="wdkQuestion" value="${requestScope.question}"/>
<c:set var="spanOnly" value="false"/>
<c:set var="checked" value=""/>
<c:set var="buttonVal" value="Get Answer"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>



<c:if test="${wdkQuestion.recordClass.fullName != wdkStep.dataType}">
	<c:set var="checked" value="checked=''"/>
	<c:set var="buttonVal" value="Continue"/>
	<c:set var="spanOnly" value="true"/>
</c:if>

<c:set var="wizard" value="${requestScope.wizard}"/>
<c:set var="stage" value="${requestScope.stage}"/>
<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do"  onsubmit="callWizard('wizard.do?',this,null,null,'submit')">
<%-- the following sections are copied from <question.tag>, need to refactor into a separate tag --%>

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
      <wdk:questionParams />
</div> <%-- end of params div --%>		
<%--<c:set target="${helps}" property="${fromAnchorQ}" value="${helpQ}"/>--%>
<%-- set the weight --%>
<div align="center">
  Assign Weight: <html:text property="weight" maxlength="9" />
</div>
<%-- end of the copied content --%>

<div id="transform_button" class="filter-button"><html:submit property="questionSubmit" value="${buttonVal}"/></div>
</html:form>
