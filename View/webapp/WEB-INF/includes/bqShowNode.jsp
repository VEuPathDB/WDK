<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<dir>

  <!-- is node if current position of wdkAnswer is boolean -->
  <nested:define id="isNode" property="isBoolean"/> 
  <c:choose>
    <c:when test="${isNode}">
    <div>
       <nested:write property="booleanOperation"/><br>
       <nested:nest property="firstChildAnswer">
          <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
       </nested:nest>
    </div>
    <p>
       <nested:nest property="secondChildAnswer">
          <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
       </nested:nest>
    </p>
    </c:when>	
    <c:otherwise>

         <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <!-- Print out question -->
            <nested:define id="wdkQ" property="question"/>
            <nested:define id="answerParams" property="params"/>

            <!-- display description -->
            <tr><td colspan="2">
                <b><jsp:getProperty name="wdkQ" property="displayName"/></b>
            </td></tr>

            <!-- display params -->
            <tr><td>
               <table border="0" cellpadding="0" cellspacing="0">
                 <c:forEach items="${answerParams}" var="aP">
                   <tr>
                      <td align="right" width="200"><i>${aP.key}</i></td>
                      <td>&nbsp;=&nbsp;</td>
                      <td>${aP.value}</td>
                   </tr>
                 </c:forEach>
               </table>
            </td></tr>
         </table>

    </c:otherwise>
  </c:choose>

</dir>
