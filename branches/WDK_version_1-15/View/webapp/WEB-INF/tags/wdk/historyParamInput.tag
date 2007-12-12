<%-- 
Provides form input element for a given FlatVocbParam.

For a multi-selectable parameter a form element is provided as either a 
series of checkboxes or a multiselect menu depending on number of 
parameter options. Also, if number of options is over a threshhold, this tag
includes a checkAll button to select all options for the parameter.

Otherwise a standard select menu is used.
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="qp"
              type="org.gusdb.wdk.model.jspwrap.HistoryParamBean"
              required="true"
              description="parameter name"
%>

<c:set var="qP" value="${qp}"/>
<c:set var="pNam" value="${qP.name}"/>
<c:set var="opt" value="0"/>

<html:select  property="myMultiProp(${pNam})" styleId="${qP.id}">
    <c:set var="opt" value="${opt+1}"/>
    <c:set var="sel" value=""/>
    <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
    <html:options property="values(${pNam})" labelProperty="labels(${pNam})"/>
</html:select>
