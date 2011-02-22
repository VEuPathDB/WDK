<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="dType"><%= request.getParameter("dataType") %></c:set>
<c:set var="stepNum"><%= request.getParameter("prevStepNum") %></c:set>
<c:set var="add"><%= request.getParameter("isAdd") %></c:set>

<wdk:addStepPopup_new model="${applicationScope.wdkModel}" rcName="${dType}" prevStepNum="${stepNum}" isAdd="${add}"/>
