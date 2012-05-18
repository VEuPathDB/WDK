<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="logic" uri="http://jakarta.apache.org/struts/tags-logic" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>

<%@ attribute name="showStackTrace" required="false" %>

<c:set var="err" scope="request" value="${requestScope['org.apache.struts.action.ERROR']}"/>
<c:set var="exp" scope="request" value="${requestScope['org.apache.struts.action.EXCEPTION']}"/>
<c:set var="pex" scope="request" value="${pageContext.exception}"/>

<c:if test="${err != null || exp != null || pex != null }"><br></c:if>

<!-- html:errors/ -->

<logic:messagesPresent> 
<EM><b>Please correct the following error(s): </b></EM><br>

    <UL>
        <html:messages id="error" message="false">
            <LI><bean:write name="error"/></LI>
        </html:messages>
    </UL>

</logic:messagesPresent>

<c:set var="site" value="${initParam.wdkDevelopmentSite}" />
<c:if test="${(site eq 'Yes' || site eq 'yes' || site eq 'YES' || 
     showStackTrace eq 'true') && showStackTrace ne 'false'}">
  
  <c:if test="${pex != null}">
    <b>${pex.message}</b><br>
     Stacktrace: <br>
     <c:forEach items="${pex.stackTrace}" var="st" >
       ${st}<br>
     </c:forEach>
    <br><br>
  </c:if>
  
  <c:if test="${exp != null}">
       <b>${exp}</b><br>
       Stacktrace: <br>
       <c:forEach items="${exp.stackTrace}" var="st">
          ${st} <br>
       </c:forEach>
    <br><br>
  </c:if>

</c:if>
