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

$(document).ready(function() {
	var param = $("#${qp.name}");
	var paramName = param.attr("id");
	param.find("#" + paramName + "_type").each(function() {
		var disable = ($(this).attr("checked") == "checked") ? "false" : "true";
		$(this).parents("tr").find(".input").attr("disabled", disable);
	});
});

var IE = document.all?true:false

if (!IE) {
    document.captureEvents(Event.CLICK);   
}

function chooseType(paramName, type) {
    var inputType = document.getElementById(paramName + '_type');
    inputType.value = type;
    // disable inputs accordingly
    var inputData = document.getElementById(paramName + '_data');
    var inputFile = document.getElementById(paramName + '_file');
    if (type == "data") {
        if (inputFile) inputData.disabled = false;
        if (inputFile) inputFile.disabled = true;
    } else if (type == "file") {
        if (inputFile) inputData.disabled = true;
        if (inputFile) inputFile.disabled = false;
    } else if (type == "basket") {
        if (inputFile) inputData.disabled = true;
        if (inputFile) inputFile.disabled = true;
    }
}

// -->
</script>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>
<c:set var="dsName" value="${pNam}_dataset"/>
<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>
<c:set var="dataset" value="${requestScope[dsName]}" />  
<c:set var="recordType" value="${qp.recordClass.type}" />
<c:set var="defaultType" value="${qp.defaultType}" />
<c:set var="dataChecked"><c:if test="${defaultType == 'data'}">checked</c:if></c:set>
<c:set var="fileChecked"><c:if test="${defaultType == 'file'}">checked</c:if></c:set>
<c:set var="basketChecked"><c:if test="${defaultType == 'basket'}">checked</c:if></c:set>
<c:set var="noAction" value="${requestScope.action == null || requestScope.action == ''}" />

<input type="hidden" id="${pNam}_type" name="${pNam}_type" value="${defaultType}" />

<table id="${qp.name}">

  <c:if test="${dataset != null || defaultType != 'basket'}"> 
    <!-- display an input box for user to enter data -->
    <tr>
        <td align="left" valign="top" nowrap>
            <input type="radio" name="${pNam}_radio" ${dataChecked}
                   onclick="chooseType('${pNam}', 'data');" />
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
            <textarea id="${pNam}_data" class="input" name="${pNam}_data" rows="5" cols="30">${datasetValues}</textarea>
        </td>
    </tr>
  </c:if>

    <c:if test="${qp.recordClass.useBasket}">	
    <!-- display option to use basket snapshot -->
    <tr>
        <c:set var="basketCount" value="${0}" />
        <c:forEach items="${wdkUser.basketCounts}" var="item">
          <c:if test="${item.key.fullName eq qp.recordClass.fullName}">
            <c:set var="basketCount" value="${item.value}" />
          </c:if>
        </c:forEach>
        <c:set var="disabled">
            <c:if test="${basketCount == 0}">disabled</c:if>
        </c:set>
        <td colspan="2" align="left" valign="top" nowrap>
            <input type="radio" name="${pNam}_radio" ${basketChecked} ${disabled}
                   onclick="chooseType('${pNam}', 'basket');" />
            Copy ${recordType}s from My Basket (${basketCount} ${recordType}s)&nbsp;
        </td>
    </tr>
    </c:if>
    
    <!-- display an existing info -->
    <c:if test="${dataset != null && fn:length(dataset.uploadFile) > 0}">
        <tr>
            <td colspan="2" align="right">
                <i>Data was uploaded from: ${dataset.uploadFile}</i>
            </td>
        </tr>
    </c:if>

    <c:if test="${defaultType != 'basket' && noAction}">
        <!-- display an input box and upload file button -->
        <tr class="dataset-file">
            <td align="left" valign="top">
                <input type="radio" name="${pNam}_radio" ${fileChecked}
                       onclick="chooseType('${pNam}', 'file');" />
               Upload from a <i>text</i> file:&nbsp;
            </td>
            <td align="left">
                <html:file styleId="${pNam}_file" styleClass="input" property="value(${pNam}_file)" disabled="true"/>
            </td>
        </tr>
    </c:if>
</table>
