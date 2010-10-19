<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<c:set var="wdkQuestion" value="${requestScope.question}"/>
<c:set var="checked" value=""/>
<c:set var="buttonVal" value="Get Answer"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>



<c:set var="wizard" value="${requestScope.wizard}"/>
<c:set var="stage" value="${requestScope.stage}"/>
<html:form styleId="form_question" method="post" enctype='multipart/form-data' action="/processFilter.do"  onsubmit="callWizard('wizard.do?action=${requestScope.action}&step=${wdkStep.stepId}&',this,null,null,'submit')">
<%-- the following sections are copied from <question.tag>, need to refactor into a separate tag --%>

<input type="hidden" name="questionFullName" value="${wdkQuestion.fullName}"/>
<html:hidden property="stage" styleId="stage" value="process_question"/>

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
<%-- end of the copied content --%>


<div id="transform_button" class="filter-button"><html:submit property="questionSubmit" value="${buttonVal}"/></div>
</html:form>
