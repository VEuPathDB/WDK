<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="nodePath" description="location in tree of current node" %>
<%@ attribute name="currentIndent" description="Current indent" %>

<c:set var="booleanQuestionNode" value="${sessionScope.currentRecursiveRoot}"/>

<!-- if current input is a node, recursive calls to tag on left and right children -->	

booleanQuestionNode is here: ${booleanQuestionNode}
<br>
<c:choose>
   <c:when test="${booleanQuestionNode.class.name eq 'org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean'}">
      <table>
        <tr><td><jsp.getProperty name="booleanQuestionNode" property="operation"/></td>
	<c:set value="${booleanQuestionNode.firstChild}" var="currentRecursiveRoot" scope="session"/> 
	display current recursive root: ${currentRecursiveRoot}
	<br>
	display input nodepath: ${nodePath}
	 <tr><td><wdk:booleanDisplay nodePath="${nodePath}0" currentIndent="${currentIndent+5}"/></td>
	 
	 <c:set value="${booleanQuestionNode.firstChild}" var="currentRecursiveRoot" scope="session"/> 		
         <tr><td><wdk:booleanDisplay nodePath="${nodePath}1" currentIndent="${currentIndent+5}"/></td>
      </table>
   </c:when>
   <c:otherwise>


         <table>
            <!-- Print out question -->
	    <c:set var="wdkQuestion" value="${booleanQuestionNode.question}"/>
	    <c:set var="leafPrefix" value="${booleanQuestionNode.leafId}_"/>
	    <c:set value="${wdkQuestion.params}" var="qParams"/>

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
	    </c:forEach>
	    <tr>
	       <td>
	       <!-- get possible questions to boolean with and display them -->
               <html:select property="nextQuestionOperand">
		     <c:set var="recordClass" value="${wdkQuestion.recordClass}"/>
		     <c:set var="questions" value="${recordClass.questions}"/>

                     <c:forEach items="${questions}" var="q">
		        <c:set value="${q.name}" var="qName"/>
        		<c:set value="${q.displayName}" var="qDispName"/>
        	        <html:option value="${qName}">${qDispName}</html:option>
       		     </c:forEach>
		  </html:select>	
	       </td>
		<!-- get boolean operations and display in select box -->
	       <td>
	          <html:select property="nextBooleanOperation">
		     <c:set value="booleanOps" var="booleanName"/>
		     <html:options property="values(${booleanName})"/>
		  </html:select>
	       </td>
	       <tr>	
	       <td><html:submit property="grow" value="${nodePath}"/></td>
	 </table>
   </c:otherwise>
</c:choose>


