<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<c:set var="model" value="${applicationScope.wdkModel}" />
<c:set var="siteName" value="${model.name}" />
<c:set var="qSetName" value="none" />
<c:set var="qSets" value="${model.questionSetsMap}" />
<c:set var="qSet" value="${qSets[qSetName]}" />
<c:set var="user" value="${sessionScope.wdkUser}"/>
<c:set var="step" value="${requestScope.wdkStep}" />
<c:set var="recordClass" value="${step.question.recordClass}" />
<c:set var="rClass" value="${step.question.recordClass}" />   <%-- used in script below --%>
<c:set var="strategyId" value="${requestScope.strategy}" />
<c:set var="action" value="${requestScope.action}" />
<c:set var="wdkStrategy" value="${requestScope.wdkStrategy}"/>


<c:set var="partialUrl" value="wizard.do?strategy=${strategyId}&step=${step.stepId}&action=${action}" />

<c:if test="${isAdd == 'false'}">
    <%-- insert a step in between, the transform cannot change type in this case --%>
    <jsp:setProperty name="recordClass" property="changeType" value="false" />
</c:if>

    <wdk:addStepHeader title="Add Step"/>
    <div id="sections">
        <table id="sections-layout"><tr>
            <td id="section-1">
                <div id="span_logic" class="qf_section">
                    <ul class="menu_section">
                        <li class="category" onclick="callWizard(null,this,'sl_recordclasses',2)">Run a new Search</li>
                        <li class="category" onclick="callWizard(null,this,'sl_strategies',2)">Add existing Strategy</li>
                        <li class="category" onclick="callWizard(null,this,'sl_baskets',2)">Add the Basket</li>
                        <%-- only allow transform if the step has previous step --%>
                        <c:if test="${step.previousStep != null || action != 'insert'}">
<%--                        <li class="category" onclick="callWizard(null,this,'sl_transforms',2)">Convert results</li>  --%>

		<c:set var="transforms" value="${recordClass.transformQuestions}" />
                <c:forEach items="${transforms}" var="transform">
                  <li onclick="callWizard('${partialUrl}&stage=transform&questionFullName=${transform.fullName}',null,null,null,'next')">
                      ${transform.displayName}
                  </li>
                </c:forEach>
                <c:if test="${fn:length(transforms) == 0}">
                    <li>No transform is available.</li>
                </c:if>


                        </c:if>
                    </ul>
                </div>
            </td>
            <td id="section-2"><div class="qf_section"></div></td>
            <td id="section-3"><div class="qf_section"></div></td>
            <td id="section-4"><div class="qf_section"></div></td>
            <!--<td id="section-5"><div class="qf_section"></div></td>-->    
        </tr></table>
        </div> <!--End Section Div-->
        <div id="sections_data">
        </div>

    <wdk:addStepFooter/>

<%-- insert/add basket section --%>
        <div class="original" id="sl_baskets" style="display:none">
            <ul class="menu_section">
                <c:set var="recordClasses" value="${wdkModel.recordClassMap}" />
                <c:set var="hasBasket" value="${false}" />
                <c:forEach items="${user.basketCounts}" var="item">
                    <c:set var="count" value="${item.value}" />
                    <c:if test="${count > 0}">
                        <c:set var="hasBasket" value="${true}" />
                        <c:set var="rcName" value="${item.key}" />
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
        <div class="original" id="sl_strategies" style="display:none">
            <ul class="menu_section">
                <li class="category" onclick="callWizard(null,this,'sl_open',3)">Opened Strategies</li>
                <li class="category" onclick="callWizard(null,this,'sl_saved',3)">Saved Strategies</li>
                <li class="category" onclick="callWizard(null,this,'sl_recent',3)">Recent Strategies</li>
            </ul>
        </div>


        <div class="original" id="sl_open" style="display:none">
          <ul class="menu_section">
            <c:set var="hasStrategy" value="${false}" />
            <c:forEach items="${user.activeStrategies}" var="storedStrategy">
              <c:if test="${storedStrategy.strategyId != wdkStrategy.strategyId}">
                <c:set var="displayName" value="${storedStrategy.name}" />
                <c:if test="${fn:length(displayName) > 30}">
                            <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                </c:if>
                <li>
                  <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${storedStrategy.strategyId}&stage=strategy',null,null,null,'next')">
                    ${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if>
                  </a>
                </li>
                <c:set var="hasStrategy" value="${true}" />
              </c:if>
            </c:forEach>
            <c:if test="${hasStrategy == false}">
              <li>No opened strategies available.</li>
            </c:if>
          </ul>
        </div>
        
        <!-- Display the Saved Strategies -->
        <div class="original" id="sl_saved" style="display:none">
          <c:set var="savedStratCount" value="0"/>
          <ul class="menu_section">
            <c:set var="hasStrategy" value="${false}" />
            <c:forEach items="${model.websiteRootCategories}" var="rcs">
              <c:set var="savedStrategies" value="${user.savedStrategiesByCategory[rcs.value.name]}"/>
              <c:set var="savedStratCount" value="${savedStratCount + fn:length(savedStrategies)}" />
              <c:forEach items="${savedStrategies}" var="storedStrategy">
                <c:if test="${storedStrategy.strategyId != wdkStrategy.strategyId}">
                  <c:set var="displayName" value="${storedStrategy.name}" />
                  <c:if test="${fn:length(displayName) > 30}">
                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                  </c:if>
                  <li>
                    <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${storedStrategy.strategyId}&stage=strategy',null,null,null,'next')">
                      ${displayName}
                    </a>
                  </li>
                  <c:set var="hasStrategy" value="${true}" />
                </c:if>
              </c:forEach>
            </c:forEach>
            <c:if test="${hasStrategy == false}">
              <li>No saved strategies available.</li>
            </c:if>
          </ul>
        </div>
        
        <!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->
        <div class="original" id="sl_recent" style="display:none">
          <ul class="menu_section">
            <c:set var="hasStrategy" value="${false}" />
            <c:forEach items="${model.websiteRootCategories}" var="rcs">
                <c:forEach items="${user.recentStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
                  <c:if test="${storedStrategy.strategyId != wdkStrategy.strategyId}">
                    <c:set var="displayName" value="${storedStrategy.name}" />
                    <c:if test="${fn:length(displayName) > 30}">
                        <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                    </c:if>
                    <li>
                      <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${storedStrategy.strategyId}&stage=strategy',null,null,null,'next')">
                        ${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if>
                      </a>
                    </li>
                    <c:set var="hasStrategy" value="${true}" />
                  </c:if>
                </c:forEach>
            </c:forEach>
            <c:if test="${hasStrategy == false}">
              <li>No recent strategies available.</li>
            </c:if>
          </ul>
        </div>
        

        <div id="sl_recordclasses" class="original" style="display:none">
            <ul class="menu_section">
            <c:set var="type" value="${step.shortDisplayType}" />
            <c:forEach var="rcs" items="${model.websiteRootCategories}">
                <c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
                <c:if test="${fn:containsIgnoreCase(rcs.value.displayName, type) ||
                                 ((type eq 'Gene' || type eq 'Orf' || type eq 'GenSegm')
                                  &&
                                  (fn:containsIgnoreCase(rcs.value.displayName, 'gene') || 
                                   fn:containsIgnoreCase(rcs.value.displayName, 'orf') || 
				   fn:containsIgnoreCase(rcs.value.displayName, 'seg')  ))}">

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
                            <li onclick="callWizard('${partialUrl}&questionFullName=${q.fullName}&stage=question',null,null,null,'next')">${q.displayName}</li>
                        </c:forEach>
                    </ul>
                </div>
            </c:forEach>
        </c:forEach>

 
<script type="text/javascript">
   rclass = "${rClass.fullName}";
   sdName = "${rClass.shortDisplayName}";
  // alert(rclass);
  // alert(sdName);

   ele = $("li[onclick*='sl_recordclasses']")[0];
   callWizard(null,ele,'sl_recordclasses',2);
   // $("td#section-1 ul.menu_section:first > li:first").click();

   ele = $('li[onclick*= "' + sdName + '" ]')[0];	
   callWizard(null,ele,'sl_'+ sdName + 'RecordClasses_' + sdName + 'RecordClass' ,3);
   // $("td#section-2 ul.menu_section:first > li:first").click();
</script>
