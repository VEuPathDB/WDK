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
    if (type == "DATASET") {
        var inputData = document.getElementById(paramName + '_data');
        inputData.disabled = true;
        var inputFile = document.getElementById(paramName + '_file');
        inputFile.disabled = true;
    } else if (type == "DATA") {
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

<input type="hidden" id="${pNam}_type" name="${pNam}_type" 
       value="${(dataset != null)? 'DATASET' : 'DATA'}" />
<input type="hidden" name="${pNam}" 
       value="${wdkUser.signature}:${dataset.datasetId}" />

<table border="0" bgcolor="#EEEEEE" cellspacing="0" cellpadding="0">
    
    <!-- display the sumamry of the dataset, if have -->
    <c:if test="${dataset != null}">
        <tr>
            <td align="left" valign="top" nowrap>
                <input type="radio" name="${pNam}_radio" checked 
                       onclick="chooseType('${pNam}', 'DATASET');"/>
                Choose previous list:&nbsp;
            </td>
            <td align="left">
                "${dataset.summary}"
                <c:if test='${dataset.uploadFile != null && dataset.uploadFile != ""}'>
                    from file &lt;${dataset.uploadFile}&gt;
                </c:if>
            </td>
        </tr>
    </c:if>
    
    <!-- display an input box for user to enter data -->
    <tr>
        <td align="left" valign="top" nowrap>
            <input type="radio" name="${pNam}_radio" 
                   ${(dataset == null)? "checked" : ""}
                   onclick="chooseType('${pNam}', 'DATA');" />
            Enter list:&nbsp;
        </td>
        <td align="left">
            <textarea id="${pNam}_data" name="${pNam}_data" 
                      ${(dataset != null)? "disabled" : ""}
                      rows="5" cols="30">${dataset.value}</textarea>
        </td>
    </tr>
    
    <!-- display an input box and upload file button -->
    <tr>
        <td align="left" valign="top" nowrap>
            <input type="radio" name="${pNam}_radio"  
                   onclick="chooseType('${pNam}', 'FILE');" />
            Upload from file:&nbsp;
        </td>
        <td align="left">
            <html:file styleId="${pNam}_file" property="myPropObject(${pNam}_file)" value="${dataset.uploadFile}" disabled="true"/>
        </td>
    </tr>
</table>
