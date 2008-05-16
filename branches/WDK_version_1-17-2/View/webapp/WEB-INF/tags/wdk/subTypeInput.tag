<%-- 
Provides form input element for a subType.
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<%@ attribute name="question"
              type="org.gusdb.wdk.model.jspwrap.QuestionBean"
              required="true"
              description="Question"
%>

<%@ attribute name="helpQ"
              type="java.util.LinkedHashMap"
              required="false"
              description="help map"
%>

<c:set var="recordClass" value="${wdkQuestion.recordClass}"/>

<c:if test="${recordClass.hasSubType && !question.ignoreSubType}">
    <c:set var="subTypeParam" value="${recordClass.subType.subTypeParam}"/>
    <div>&nbsp;</div>
    <table border="0" width="100%">
        <tr>
            <td align="right" valign="top"><b>${subTypeParam.prompt}</b></td>
            <td align="left" valign="top">
                <wdk:enumParamInput qp="${subTypeParam}" />
            </td>
            <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
            <td valign="top" width="50" nowrap>
                <c:if test="${helpQ != null}">
                    <c:set var="anchorQp" value="HELP_${fromAnchorQ}_${subTypeParam.name}"/>
                    <c:set target="${helpQ}" property="${anchorQp}" value="${subTypeParam}"/>
                    <a href="#${anchorQp}">
                    <img src='<c:url value="/images/toHelp.jpg"/>' border="0" alt="Help!"></a>
                </c:if>
            </td>
        </tr>
    </table>
</c:if>
