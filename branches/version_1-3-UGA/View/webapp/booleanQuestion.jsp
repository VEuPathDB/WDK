<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- display page header -->
<site:header banner="Expanding Question" />

<!-- show error messages, if any -->
<wdk:errors/>

<jsp:useBean scope="request" id="helps" class="java.util.HashMap"/>

<nested:form method="get" action="/processBooleanQuestion.do">
  <nested:root name="currentBooleanRoot">
    <jsp:include page="/WEB-INF/includes/booleanQuestionNode.jsp"/>
  </nested:root>

  <hr>

  <table width="100%">
  <tr><td colspan="2" align="center">
      <em><b>Please do not use the browser's "back" button. Start over from a simple question instead.</b></em><br><br>
      <b>After finished expanding the question and setting parameters:</b></td></tr>
  <tr><td align="right"><html:submit property="process_boolean_question" value="Retrieve Answer"/></td>
      <td align="left"><html:reset property="reset_all_param" value="Reset All Parameters"/></td></tr>
  </table>
</nested:form>

<site:footer/>
