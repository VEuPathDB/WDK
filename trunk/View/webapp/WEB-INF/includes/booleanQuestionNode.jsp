<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>

<dir>

  <nested:define id="rootClassName" property="class.name"/>
  <c:choose>
    <c:when test="${rootClassName eq 'org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean'}">
    <nested:write property="operation"/><br>
    <nested:nest property="firstChild">
      <jsp:include page="/WEB-INF/includes/booleanQuestionNode.jsp"/>
    </nested:nest>

    <nested:nest property="secondChild">
      <jsp:include page="/WEB-INF/includes/booleanQuestionNode.jsp"/>
    </nested:nest>

    </c:when>	
    <c:otherwise>

         <table border="1">
            <!-- Print out question -->
            <nested:define id="wdkQ" property="question"/>
            <nested:define id="leafPref" property="leafId"/>

            <c:set value="${leafPref}_" var="leafPrefix"/>
            <c:set value="${wdkQ.params}" var="qParams"/>

            <!-- display description -->
            <tr><td colspan="2">
                <b><jsp:getProperty name="wdkQ" property="description"/></b></td></tr>

            <!-- display params -->
            <c:forEach items="${qParams}" var="qP">
               <!-- an individual param (can not use fullName, w/ '.', for mapped props) -->
               <c:set value="${qP.name}" var="pNam"/>
               <tr><td align="right"><b><jsp:getProperty name="qP" property="prompt"/></b></td>

               <!-- choose between flatVocabParam and straight text or number param -->
               <c:choose>
                  <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.FlatVocabParamBean'}">
                     <td>
                        <c:set var="mp" value="0"/>
                        <c:if test="${qP.multiPick}"><c:set var="mp" value="1"/></c:if>
                        <c:set var="opt" value="0"/>
                        <html:select  property="myProp(${leafPrefix}${pNam})" multiple="${mp}">
                           <c:set var="opt" value="${opt+1}"/>
                           <c:set var="sel" value=""/>
                           <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
                           <html:options property="values(${leafPrefix}${pNam})" labelProperty="labels(${leafPrefix}${pNam})"/>
                        </html:select>
                     </td>
                  </c:when>
                  <c:otherwise>
                     <td>
                        <html:text property="myProp(${leafPrefix}${pNam})"/>
                     </td>
                  </c:otherwise>
                </c:choose>
                </tr>
            </c:forEach>

            <!-- display boolean stuff -->
            <tr>
               <td align="right">
               <!-- get possible questions to boolean with and display them -->
               <html:select property="nextQuestionOperand">
                     <c:set var="recordClass" value="${wdkQ.recordClass}"/>
                     <c:set var="questions" value="${recordClass.questions}"/>

                     <c:forEach items="${questions}" var="q">
                        <c:set value="${q.fullName}" var="qFullName"/>
                        <c:set value="${q.displayName}" var="qDispName"/>
                        <html:option value="${qFullName}">${qDispName}</html:option>
                     </c:forEach>
                  </html:select>
               </td>
                <!-- get boolean operations and display in select box -->
               <td>
                  <html:select property="nextBooleanOperation">
                     <c:set value="booleanOps" var="booleanName"/>
                     <html:options property="values(${booleanName})"/>
                  </html:select>
                  <html:submit property="process_boolean_question" value="Grow Boolean (${leafPref})"/>
               </td></tr>
         </table>

    </c:otherwise>
  </c:choose>

</dir>
