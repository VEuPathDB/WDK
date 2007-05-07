<%-- 
display the parameter values for an answer.
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="wdkAnswer"
              type="org.gusdb.wdk.model.jspwrap.AnswerBean"
              required="true"
              description="Answer Object"
%>

<c:choose>
    <c:when test="${wdkAnswer.isBoolean}">
        <div>
            <%-- boolean question --%>
            <nested:root name="wdkAnswer">
                <jsp:include page="/WEB-INF/includes/bqShowNode.jsp"/>
            </nested:root>
	    </div>
    </c:when>
    <c:otherwise>
	    <div ${paddingStyle}>
            <!-- simple question -->
            <c:set value="${wdkAnswer.internalParams}" var="params"/>
            <c:set value="${wdkAnswer.question.paramsMap}" var="qParamsMap"/>
            <c:set value="${wdkAnswer.question.displayName}" var="wdkQuestionName"/>
            <table border="0" cellspacing="0" cellpadding="0">
               <tr>
                  <td align="right" valign="top"><i>Query</i></td>
                  <td valign="top">&nbsp;:&nbsp;</td>
                  <td>${wdkQuestionName}</td>
               </tr>
               <c:forEach items="${qParamsMap}" var="p">
                  <c:set var="pNam" value="${p.key}"/>
                  <c:set var="qP" value="${p.value}"/>
                  <c:set var="aP" value="${params[pNam]}"/>
                  <c:if test="${qP.isVisible}">
                     <tr>
                        <td align="right" valign="top"><i>${qP.prompt}</i></td>
                        <td>&nbsp;:&nbsp;</td>
                        <td>
                           <c:choose>
                              <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.DatasetParamBean'}">
                                 <jsp:setProperty name="qP" property="combinedId" value="${aP}" />
                                 <c:set var="dataset" value="${qP.dataset}" />  
                                 "${dataset.summary}"
                                 <c:if test='${fn:length(dataset.uploadFile) > 0}'>
                                    from file &lt;${dataset.uploadFile}&gt;
                                 </c:if>
                              </c:when>
                              <c:otherwise>
                                 <jsp:setProperty name="qP" property="paramValue" value="${aP}" />
                                 ${qP.decompressedValue}
                              </c:otherwise>
                           </c:choose>
                        </td>
                     </tr>
                  </c:if>
               </c:forEach>
            </table>
        </div>
    </c:otherwise>
</c:choose>
