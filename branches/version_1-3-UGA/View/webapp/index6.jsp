<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="nested" uri="http://jakarta.apache.org/struts/tags-nested" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkModel saved in application scope -->
<c:set var="wdkModel" value="${applicationScope.wdkModel}"/>

<!-- top banner and introduction -->

<!-- get wdkModel name to display as page header -->
<c:set value="${wdkModel.displayName}" var="wdkModelDispName"/>
<site:header banner="${wdkModelDispName}" />

<!-- REMOVED INTRO-->

<!-- end top banner and introdction -->

<!-- horizontal tool bar-->
<table width="640" align="center">

  <tr>
    <td>
         <jsp:include page="WEB-INF/includes/announcements.html"/>
    </td>
  </tr>

  <tr>
    <td>
         <jsp:include page="WEB-INF/includes/toolbar.html"/>
    </td>
  </tr>
</table>
<!--end horizontal tool bar-->


<!-- main table -->
<table width="640" align="center" border="0" cellpadding="0" cellspacing="0"> 
    <tr> 
        <!-- side bar-->

      <td width="25%" valign="top">
      
      <!-- test which width is better 
     	<td width="20%" valign="top">-->
             <jsp:include page="WEB-INF/includes/sidebar.html"/><br>
         <center><img src="/images/CmurisSEM1_color_tiny.gif" /></center>
        </td>
        <!-- end side bar -->     

       <!--space between sidebar and main body-->
        <td width="1%">&nbsp;</td>
       <!-- end space -->
     
 	<!-- Start of the main body  -->
	<td width="74%" valign="top">

        <!-- test which width is better 
	<td width="80%" valign="top">-->

<!--THE SIMPLE SEARCH TABLE-->
     <table width="100%" border="0" cellspacing="0" cellpadding="0"> 
         <tr class="primary2">
        	<td colspan="3"><font size="-1"> <b>Simple Search</b></font></td>
         </tr>



    <!-- show all questionSets in model -->

    <c:set value="${wdkModel.questionSets}" var="questionSets"/>
    <c:set var="i" value="0"/>

    <!--loop 1: loop through all question sets -->
    <c:forEach items="${questionSets}" var="qSet" >
        <c:set value="${qSet.name}" var="qSetName"/>
        <c:set value="${qSet.questions}" var="questions"/>
     		  <jsp:useBean scope="request" id="helps" class="java.util.HashMap"/>


           <!--loop 2: loop through each question set-->
	   <c:forEach items="${questions}" var="q">
                  <!--SET UP HELP FOR EACH QUESTION -->
                  <!-- show all params of question, collect help info along the way -->
                  <jsp:useBean id="helpQ"  class="java.util.HashMap" />
		
  	       <html:form method="get" action="/processQuestion2.do">
                  <c:set value="${q.name}" var="qName"/>
                  <c:set value="${q.displayName}" var="qDispName"/>                


                  <!--set row color: even row=white; odd row=gray -->  
		  <c:choose>
	             <c:when test="${i % 2 == 0}">
		        <c:set var="rowColor" value="white"/>
	             </c:when>
			  
                     <c:otherwise>
                         <c:set var="rowColor" value="primary3"/>
                    </c:otherwise>
	          </c:choose>
		     
			<!--FIND THE # OF PARAMS AND DETERMINE IF ORGANISM IS NEEDED TO HELP FORMAT THE QUESTION ROW -->
		 	<c:set value="0" var="numParams"/>
		 	<c:set value="false" var="needOrg"/>
                	<c:forEach items="${q.params}" var="qP">
				<c:set value="${numParams+1}" var="numParams"/>		
		    		<c:if test="${qP.name =='organism'}">
		       			<c:set value="true" var="needOrg"/>	
				</c:if>
		 	 </c:forEach>

                  <!--display search caption. e.g. Gene by annotated keyword -->
                  <c:set var="i" value="${i+1}"/>
                      <tr class="${rowColor}">

                  <!--SET UP HELP FOR EACH QUESTION -->
                  <!-- show all params of question, collect help info along the way -->

                  <c:set value="Help for question: ${qDispName}" var="fromAnchorQ"/>
		   <c:set var="maxDispLength" value="10"/>
	 	 <!--FIND LENGTH OF QUESTION DISPLAY NAME TO DETERMINE PROPER LAYOUT -->
		 <c:set var="dispNameLength" value="${fn:length(qDispName)}"/>	

   		<!-- put an anchor here for linking back from help sections -->
                  <c:set var="anchorQp" value="HELP_${fromAnchorQ}"/>
              	  <c:set target="${helps}" property="${fromAnchorQ}" value="${q}"/>

		<!--column 1-->
		    <c:choose>
			 <c:when test="${dispNameLength >=maxDispLength}"  >
			    <td colspan="3" align="left" nowrap>
			  </c:when>
			  <c:otherwise>
			    <td  align="left" nowrap>
		          </c:otherwise>
			</c:choose>

   		  <a name="${fromAnchorQ}"></a>
                  <font size="-1"><b>${qDispName}  </b></font>
                   <a href="#${anchorQp}">
		   <font size="-2">
		 	(What's This?)
		    </font></a>

			 <c:if test="${needOrg == 'true'}"  >
				&nbsp;&nbsp;&nbsp;
			  </c:if>

			<c:set var="noSpace" value="false"/>

                 <c:set value="${qSetName}_${qName}" var="qSetNameName"/>
		

                 <!-- loop 3: loop through all params to see if organism is needed -->
                 <c:forEach items="${q.params}" var="qP">

                         <!--SET UP HELP FOR EACH PARAM-->
			  <!--c:set target="${helpQ}" property="${qName}_${qP.name}" value="${qP}"/-->

                    <!--if need to display organism,display them as radio buttons -->	
		    <c:if test="${qP.name =='organism'}">
		       <html:hidden property="needOrganism" value="true"/>
		       <c:set value="true" var="needOrg"/>

			<c:choose>
			 <c:when test="${dispNameLength >=maxDispLength}"  >
                            </td></tr>
			    <tr  class="${rowColor}">
			    <td colspan="2" align="right">
			  </c:when>
			  <c:otherwise>
			    </td><td  align="right">
		          </c:otherwise>
			</c:choose>

        		     <font size="-1">
				<html:radio property="organism"  value="Cryptosporidium hominis" /> 
				   <i>C. hominis</i>
				<html:radio property="organism"  value="Cryptosporidium parvum" /> 
				   <i>C. parvum</i>
				<html:radio property="organism"  value="Cryptosporidium hominis, Cryptosporidium parvum" /> 
				   <i>Both</i>
			     </font>
		          </td>

		 		<c:if test="${numParams==1}">
	       		            <html:hidden property="questionFullName" value="${qSetName}.${qName}" />
			  	    <c:set var="noSpace" value="true"/>
				</c:if>

	          </c:if> <!--end if need to display organism-->			
	
		  <c:if test="${noSpace=='false'}">
			</td><td></td>
			<c:set var="noSpace" value="true"/>
		  </c:if>
		
		 <!--DON'T SHOW THE ORGANISM PARAMETER BECAUSE IT IS SELECTED USING RADIO BUTTONS -->
                <c:if test="${qP.name != 'organism'}"  >
                        </tr><tr  class="${rowColor}">
                          
                        <!--column 1-->
			   <!--NEED TO ADD "GET/SETSAMPLE" TO PARAMBEAN
				THEN PUT ADDITIONAL EXAMPLES IN THE MODEL,  "SAMPLE=..." FOR EACH NON-FLATVOCAB PARAM-->

			      <c:choose>
                              <c:when test="${qP.class.name ne 'org.gusdb.wdk.model.jspwrap.FlatVocabParamBean'}">
				<td align="left"  width="180" >
			         <font size="-2" color="#660000">
				   &nbsp;&nbsp;${qP.sample}
			          </font>
				</td>
                       <!--column 2-->
				<td align="right" nowrap >			
                              		<!--text input prompt. e.g. Gene id:-->
                              		<font size="-1">
                                 	  <i><jsp:getProperty name="qP" property="prompt"/></i> :  
                              		</font>
			      </c:when><c:otherwise>
				  <td colspan="2" align="right" nowrap>
                                	<!--text input prompt. e.g. Gene id:-->
                              		<font size="-1">
                                 	  <i><jsp:getProperty name="qP" property="prompt"/></i> :  
                              		</font>
				</c:otherwise>
			      </c:choose>

                              <font size="-1">		
			         <c:choose>
                                    <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.FlatVocabParamBean'}">
                                       <c:set value="${qP.name}" var="pNam"/>
                                       <c:set var="mp" value="0"/>
                                       <c:if test="${qP.multiPick}">
	                                  <c:set var="mp" value="1"/>
	                               </c:if>
                                       <c:set var="opt" value="0"/>

                                     <!--begin select -->
					<c:forEach items="${qP.vocab}" var="flatVoc">
					    <html:multibox property="myMultiProp2(${qSetNameName}_${pNam})" >
        					<c:out value="${flatVoc}" />
					    </html:multibox>
					        <c:out value="${flatVoc} "/>
					</c:forEach>

			          </c:when>
			       
                                  <c:otherwise>

			         <font>
                                     <html:text property="myProp2(${qSetNameName}_${qP.name})" value="${qP.default}" size="14"/>
                                 </font>
                           	</td>
				  </c:otherwise>
			      </c:choose>	

		
		</c:if> <!-- end if it's not organism-->
	   </c:forEach> <!--end loop through all params-->       
		<c:if test="${needOrg == 'false'}"  >
			<html:hidden property="needOrganism" value="false"/>
		</c:if>
     
            <!-- column 4-->   		   
             <td align="right">  
		&nbsp;	
                 <html:hidden property="questionSubmit" value="Get Answer"/>
                 <input name="go" value="go" type="image" src="images/go.gif" border="0" onmouseover="return true;">
             </td>
           </tr>
       </html:form>
     </c:forEach> <!--end for each question in a question set-->

  </c:forEach> <!-- end for each question set -->
    </table><!--END OF SIMPLE SEARCH TABLE -->
	
 <!-- DISPLAY BOOLEAN QUERY OPTIONS (Begin Combined search) -->
	     
	     <!--END OF COMBINED SEARCH TABLE -->

   </table> <!-- end of main body table -->
   <!--footer-->
   <table width="640" align="center" border="0">
    <tr><td><hr></td></tr>
     <tr>
         <td>
           <jsp:include page="WEB-INF/includes/footer.html"/>
         </td>
     </tr>
   </table>


<site:footer/>

