<%-- 
Provides form input element for a given DatasetParam.
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.DatasetParamBean"
              required="true"
              description="parameter name"
%>

<script type="text/javascript" lang="JavaScript 1.2">
<!-- //

var IE = document.all?true:false

if (!IE) {
    document.captureEvents(Event.CLICK);   
}

function chooseType(paramName, type) {
    var inputType = document.getElementById(paramName + '_type');
    inputType.value = type;
    // disable inputs accordingly
    if (type == "DATA") {
        var inputData = document.getElementById(paramName + '_data');
        inputData.disabled = false;
        var inputFile = document.getElementById(paramName + '_file');
        inputFile.disabled = true;
    } else if (type == "FILE") {
        var inputData = document.getElementById(paramName + '_data');
        inputData.disabled = true;
        var inputFile = document.getElementById(paramName + '_file');
        inputFile.disabled = false;
    }
}

// -->
</script>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
<c:set var="dataset" value="${requestScope[pNam]}" />  

<input type="hidden" id="${pNam}_type" name="${pNam}_type" value="DATA" />

<table border="0" bgcolor="#EEEEEE" cellspacing="0" cellpadding="0">
    
    <!-- display an input box for user to enter data -->
    <tr>
        <td align="left" valign="top" nowrap>
            <input type="radio" name="${pNam}_radio" checked
                   onclick="chooseType('${pNam}', 'DATA');" />
            Enter list:&nbsp;
        </td>
        <c:set var="datasetValues">
            <c:choose>
                <c:when test="${dataset != null}">
                    ${dataset.value}
                </c:when>
                <c:otherwise>
                    ${qP.default}
                </c:otherwise>
            </c:choose>
        </c:set>
        <td align="left">
            <textarea id="${pNam}_data" name="${pNam}_data" rows="5" cols="30">${datasetValues}</textarea>
        </td>
    </tr>
    
    <!-- display an input box and upload file button -->
    <c:if test="${dataset != null && fn:length(dataset.uploadFile) > 0}">
        <tr>
            <td colspan="2" align="right">
                <i>Data was uploaded from: ${dataset.uploadFile}</i>
            </td>
        </tr>
    </c:if>
    <tr>
        <td align="left" valign="top" nowrap>
            <input type="radio" name="${pNam}_radio"  
                   onclick="chooseType('${pNam}', 'FILE');" />
            Upload from file:&nbsp;
        </td>
        <td align="left">
            <html:file styleId="${pNam}_file" property="myPropObject(${pNam}_file)" disabled="true"/>
        </td>
    </tr>
</table>
