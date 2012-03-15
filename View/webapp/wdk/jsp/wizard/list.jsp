<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="model" value="${applicationScope.wdkModel}" />
<c:set var="siteName" value="${model.name}" />
<c:set var="qSetName" value="none" />
<c:set var="qSets" value="${model.questionSetsMap}" />
<c:set var="qSet" value="${qSets[qSetName]}" />
<c:set var="user" value="${sessionScope.wdkUser}"/>
<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="recordClass" value="${step.question.recordClass}" />
<c:set var="strategyId" value="${requestScope.strategy}" />
<c:set var="action" value="${requestScope.action}" />
<c:set var="wdkStrategy" value="${requestScope.wdkStrategy}"/>
<c:set var="stepRcName" value="${recordClass.fullName}" />
<c:set var="stepDisplayName" value="${recordClass.displayName}" />

<%-- determine if the current step allows span operation --%>
<c:set var="allowSpan" value="${stepRcName eq 'GeneRecordClasses.GeneRecordClass' 
                                || stepRcName eq 'OrfRecordClasses.OrfRecordClass'
                                || stepRcName eq 'DynSpanRecordClasses.DynSpanRecordClass'
                                || stepRcName eq 'SnpRecordClasses.SnpRecordClass'
                                || stepRcName eq 'SageTagRecordClasses.SageTagRecordClass'}" />

<c:set var="partialUrl" value="wizard.do?strategy=${strategyId}&step=${step.stepId}&action=${action}" />

<c:if test="${isAdd == 'false'}">
    <%-- insert a step in between, the transform cannot change type in this case --%>
    <jsp:setProperty name="recordClass" property="changeType" value="false" />
</c:if>

    <imp:addStepHeader title="Add Step"/>
    <div id="sections">
        <table id="sections-layout"><tr>
            <td id="section-1">
                <div id="span_logic" class="qf_section">
                    <ul class="menu_section">

	<li class="category" onclick="callWizard(null,this,'sl_recordclasses',2)">Run a new Search for</li>
                       
	<%-- only allow transform if the step has previous step --%>
	<c:if test="${step.previousStep != null || action != 'insert'}">
	 <%--   <li class="category" onclick="callWizard(null,this,'sl_transforms',2)">Convert results</li>    --%>

		<c:set var="transforms" value="${recordClass.transformQuestions}" />
                <c:forEach items="${transforms}" var="transform">
			  <c:if test="${ fn:containsIgnoreCase(transform.displayName, 'ortholog')}">
                  <li onclick="callWizard('${partialUrl}&stage=transform&questionFullName=${transform.fullName}',null,null,null,'next')">
                      ${transform.displayName}
                  </li>
			 </c:if>
                </c:forEach>
<%-- don't display anything if there is not transform for the record type
                <c:if test="${fn:length(transforms) == 0}">
                    <li>No transform is available.</li>
                </c:if>
--%>

	</c:if>

	<li class="category" onclick="callWizard(null,this,'sl_baskets',2)">Add contents of Basket</li>
 	<li class="category" onclick="callWizard(null,this,'sl_strategies',2)">Add existing Strategy</li>

	<%-- add rest of transforms --%>

	<c:if test="${step.previousStep != null || action != 'insert'}">
                <c:forEach items="${transforms}" var="transform">
			  <c:if test="${! fn:containsIgnoreCase(transform.displayName, 'ortholog')}">
                  <li onclick="callWizard('${partialUrl}&stage=transform&questionFullName=${transform.fullName}',null,null,null,'next')">
                      ${transform.displayName}
                  </li>
			 </c:if>
                </c:forEach>
	</c:if>


                    </ul>  <%-- class menu_section --%>
                </div>
            </td>
            <td id="section-2"><div class="qf_section"></div></td>
            <td id="section-3"><div class="qf_section"></div></td>
            <td id="section-4"><div class="qf_section"></div></td>
            <!--<td id="section-5"><div class="qf_section"></div></td>-->    
        </tr></table>
        </div> <!--End Section Div-->

	<div id="sections_data"></div>

<imp:addStepFooter/>


<%-- insert/add basket section --%>
        <div class="original" id="sl_baskets" style="display:none">
            <ul class="menu_section">
                <c:set var="recordClasses" value="${wdkModel.recordClassMap}" />
                <c:set var="hasBasket" value="${false}" />
                <c:forEach items="${user.basketCounts}" var="item">
                    <c:set var="count" value="${item.value}" />
                    <c:set var="rcName" value="${item.key.fullName}" />
                    <c:if test="${count > 0 
                                  && ((stepRcName eq rcName) 
                                      || ((rcName eq 'GeneRecordClasses.GeneRecordClass' 
                                           || rcName eq 'OrfRecordClasses.OrfRecordClass' 
                                           || rcName eq 'DynSpanRecordClasses.DynSpanRecordClass' 
                                           || rcName eq 'SnpRecordClasses.SnpRecordClass'
                                           || rcName eq 'SageTagRecordClasses.SageTagRecordClass')
                                          && allowSpan
                                         )
                                     )}">
                        <c:set var="hasBasket" value="${true}" />
                        <c:set var="recordClass" value="${recordClasses[rcName]}" />
                        <c:set var="rcDisplay" value="${recordClass.displayName}" />
                        <li onclick="callWizard('${partialUrl}&stage=basket&recordClass=${rcName}',null,null,null,'next')">
${rcDisplay} basket
                        </li>
                    </c:if>
                </c:forEach>
                <c:if test="${hasBasket == false}">
                    <li>Basket is empty.</li>
                </c:if>
            </ul>
        </div>



<%-- this was used when we had "convert results" in first panel, then in second panel we had transforms and filter by weight --%>
<%-- insert/add transform section --%>
        <div class="original" id="sl_transforms" style="display:none">
            <ul class="menu_section">
                <c:set var="transforms" value="${recordClass.transformQuestions}" />
                <c:forEach items="${transforms}" var="transform">
                  <li>
                    <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&stage=transform&questionFullName=${transform.fullName}',null,null,null,'next')">
                      ${transform.displayName}
                    </a>
                  </li>
                </c:forEach>
                <c:if test="${fn:length(transforms) == 0}">
                    <li>No transform is available.</li>
                </c:if>
            </ul>
        </div>


<%-- insert/add strategy section --%>
        <c:set var="allStrats" value="${user.strategiesByCategoryActivity}" />
        <div class="original" id="sl_strategies" style="display:none">
            <ul class="menu_section">
              <c:set var="catId" value="${0}" />
              <c:forEach items="${allStrats}" var="category">
                <c:if test="${(stepDisplayName eq category.key) 
                              || ((category.key eq 'Gene' 
                                   || category.key eq 'ORF' 
                                   || category.key eq 'Genomic Segment' 
                                   || category.key eq 'SNP'
                                   || category.key eq 'SAGE Tag Alignment')
                                  && allowSpan
                                 )}">
                  <c:set var="catId" value="${catId + 1}" />
                  <li class="category" onclick="callWizard(null,this,'sl_strategies_${catId}',3)">${category.key}</li>
                </c:if>
              </c:forEach>
            </ul>
        </div>

        <%-- create strategy sections by category --%>
        <c:set var="catId" value="${0}" />
        <c:forEach items="${allStrats}" var="category">
          <c:if test="${(stepDisplayName eq category.key) 
                        || ((category.key eq 'Gene' 
                             || category.key eq 'ORF' 
                             || category.key eq 'Genomic Segment' 
                             || category.key eq 'SNP'
                             || category.key eq 'SAGE Tag Alignment')
                            && allowSpan
                           )}">
          <c:set var="catId" value="${catId + 1}" />
          <div class="original" id="sl_strategies_${catId}" style="display:none">
            <ul class="menu_section">
              <c:set var="actId" value="${0}" />
              <c:forEach items="${category.value}" var="activity">
                  <c:set var="actId" value="${actId + 1}" />
                  <li class="category" onclick="callWizard(null,this,'sl_strategies_${catId}_${actId}',4)">${activity.key}</li>
              </c:forEach>
            </ul>
          </div>


          <c:set var="actId" value="${0}" />
          <c:forEach items="${category.value}" var="activity">
            <c:set var="actId" value="${actId + 1}" />
            <div class="original" id="sl_strategies_${catId}_${actId}" style="display:none">
              <ul class="menu_section">
                <c:set var="hasStrategy" value="${false}" />
                <c:forEach items="${activity.value}" var="strategy">
                  <c:if test="${strategy.strategyId != wdkStrategy.strategyId}">
                    <c:set var="hasStrategy" value="${true}" />
                    <c:set var="displayName" value="${strategy.name}" />
                    <c:if test="${fn:length(displayName) > 30}">
                      <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                    </c:if>
                    <li>
                      <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${strategy.strategyId}&stage=strategy',null,null,null,'next')">
                        ${displayName}<c:if test="${!strategy.isSaved}">*</c:if>
                     </a>
                    </li>
                  </c:if>
                </c:forEach>
                <c:if test="${hasStrategy == false}">
                  <li>No strategy available.</li>
                </c:if>
              </ul>
            </div>
          </c:forEach>
          </c:if>
        </c:forEach>


<%-- section to render question list --%>
        <div id="sl_recordclasses" class="original" style="display:none">
            <ul class="menu_section">
            <c:forEach var="rcs" items="${model.websiteRootCategories}">
                <c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
                <c:if test="${(rcs.value.name eq stepRcName) 
                              || (allowSpan
                                  &&
                                  (rcs.value.name eq 'GeneRecordClasses.GeneRecordClass' 
                                   || rcs.value.name eq 'OrfRecordClasses.OrfRecordClass'
				   || rcs.value.name eq 'DynSpanRecordClasses.DynSpanRecordClass'
                                   || rcs.value.name eq 'SnpRecordClasses.SnpRecordClass'
                                   || rcs.value.name eq 'SageTagRecordClasses.SageTagRecordClass')
                                 )}">
                    <li class="category" onclick="callWizard(null,this,'sl_${classId}',3)">${rcs.value.displayName}</li>
                </c:if>
            </c:forEach>
            </ul>
        </div>
        
        <c:forEach var="rcs" items="${model.websiteRootCategories}">
            <c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
            <div class="original" id="sl_${classId}" style="display:none">
                <ul class="menu_section">
                    <c:choose>
                    <c:when test="${rcs.value.multiCategory}">
                    <c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
                        <c:set var="cat" value="${catEntry.value}" />
                        <li class="category" onclick="callWizard(null,this,'sl_${cat.name}',4)">${cat.displayName}</li>
                    </c:forEach>
                    </c:when>
                    <c:otherwise>
                    <c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
                    <c:set var="cat" value="${catEntry.value}" />
                    <c:forEach items="${cat.websiteQuestions}" var="q">
                        <li onclick="callWizard('${partialUrl}&questionFullName=${q.fullName}&stage=question',null,null,null,'next')">${q.displayName}</li>
                    </c:forEach>
                    </c:forEach>
                    </c:otherwise>
                    </c:choose>
                </ul>
                
                
            </div>
        </c:forEach>
        
        <c:forEach var="rcs" items="${model.websiteRootCategories}">
            <c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
                <c:set var="cat" value="${catEntry.value}" />
                <div class="original" id="sl_${cat.name}" style="display:none">
                    <ul class="menu_section">
                        <c:forEach items="${cat.websiteQuestions}" var="q">
                          <c:set var="newIcon">
                            <c:if test="${q.new}"><img src="<c:url value='/wdk/images/new-feature.png' />" /></c:if>
                          </c:set>
                          <li onclick="callWizard('${partialUrl}&questionFullName=${q.fullName}&stage=question',null,null,null,'next')">
                            ${q.displayName}
                            <c:if test="${q.new}">
                              <img src="<c:url value='/wdk/images/new-feature.png' />"
                                   title="This is a new search in the current release." />
                            </c:if>
                          </li>
                        </c:forEach>
                    </ul>
                </div>
            </c:forEach>
        </c:forEach>

 
<%-- Initialize Add Step panel --%>
<script type="text/javascript">
   rclass = "${recordClass.fullName}";
   sdName = "${recordClass.shortDisplayName}";
   //alert(rclass);
   //alert(sdName);

   ele = $("li[onclick*='sl_recordclasses']")[0];

   callWizard(null,ele,'sl_recordclasses',2);
   // $("td#section-1 ul.menu_section:first > li:first").click();

   ele = $('li.category[onclick*= "' + sdName + '" ]')[0];
	
   callWizard(null,ele,'sl_'+ sdName + 'RecordClasses_' + sdName + 'RecordClass' ,3);
   // $("td#section-2 ul.menu_section:first > li:first").click();
</script>
