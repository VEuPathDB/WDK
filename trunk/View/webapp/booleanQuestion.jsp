<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- display page header with seedQuestion displayName as banner -->
<site:header banner="Boolean Question" />

<nested:form method="get" action="/processBooleanQuestion.do">
  <nested:root name="currentBooleanRoot">
    <jsp:include page="/WEB-INF/includes/booleanQuestionNode.jsp"/>
  </nested:root>

  <hr>

  <table width="100%">
  <tr><td colspan="2" align="center">
      <b>After finished growing the boolean question and setting parameters:</b></td></tr>
  <tr><td align="right"><html:submit property="process_boolean_question" value="Get Boolean Answer"/></td>
      <td align="left"><html:reset property="reset_all_param" value="Reset All Parameters"/></td></tr>
  </table>
</nested:form>

<site:footer/>
