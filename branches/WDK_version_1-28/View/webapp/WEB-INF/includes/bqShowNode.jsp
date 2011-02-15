<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<dir>

  <!-- is node if current position of wdkAnswer is boolean -->
  <nested:define id="isNode" property="isCombined"/>
  <nested:define id="isBoolean" property="isBoolean"/>
  <c:choose>
    <c:when test="${isNode}">
    <div>
       <c:choose>
         <c:when test="${isBoolean}">
           <nested:write property="booleanOperation"/><br>
         </c:when>
         <c:otherwise>
           <nested:write property="question.displayName"/><br/>
         </c:otherwise>
       </c:choose>
       <nested:nest property="firstChildAnswer">
          <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
       </nested:nest>
    </div>
    <c:if test="${isBoolean}">
    <div>
       <nested:nest property="secondChildAnswer">
          <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
       </nested:nest>
    </div>
    </c:if>
    </c:when>	
    <c:otherwise>
         <nested:define id="currentAnswer" property="this/"/>
         <wdk:showParams wdkAnswer="${currentAnswer}" />
    </c:otherwise>
  </c:choose>

</dir>
