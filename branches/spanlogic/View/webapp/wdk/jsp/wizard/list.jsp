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
<c:set var="strategyId" value="${requestScope.strategy}" />
<c:set var="action" value="${requestScope.action}" />
<%-- the type is of the previous step, that is the input type of the new step  
<c:set var="recordClass" value="${model.recordClassMap[rcName]}" />--%>

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
                        <li class="category" onclick="callWizard(null,this,'sl_transforms',2)">Convert results</li>
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
<script type="text/javascript">
   callWizard(null,this,'sl_recordclasses',2);
   callWizard(null,this,'sl_GeneRecordClasses_GeneRecordClass',3);
</script>


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
                        <li>
                            <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&stage=basket&recordClass=${rcName}',null,null,null,'next')">
${rcDisplay} basket</a>
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
                <c:set var="cls" value=""/>
                <c:set var="cats" value=""/>
                <c:set var="count_s" value=""/>
                <c:forEach items="${model.websiteRootCategories}" var="rcs">
                    <c:set var="count_s" value="${count_s + fn:length(user.savedStrategiesByCategory[rcs.value.name])}"/>
                </c:forEach>
                <c:if test="${count_s > 0}">
                    <c:set var="cls" value="showNewSection(this,'sl_saved',3)"/>
                    <c:set var="cats" value="category"/>
                </c:if>
                <li class="${cats}" onclick="${cls}">Saved Strategies</li>
                <c:set var="clr" value=""/>
                <c:set var="catr" value=""/>
                <c:set var="count_r" value=""/>
                <c:forEach items="${model.websiteRootCategories}" var="rcs">
                    <c:set var="count_r" value="${count_r + fn:length(user.recentStrategiesByCategory[rcs.value.name])}"/>
                </c:forEach>
                <c:if test="${count_r > 0}">
                    <c:set var="clr" value="showNewSection(this,'sl_recent',3)"/>
                    <c:set var="catr" value="category"/>
                </c:if>
                <li class="${catr}" onclick="${clr}">Recent Strategies</li>
            </ul>
        </div>


        <div class="original" id="sl_open" style="display:none">
                    <ul class="menu_section">
                        <c:forEach items="${user.activeStrategies}" var="storedStrategy">
                                <c:set var="displayName" value="${storedStrategy.name}" />
                                <c:if test="${fn:length(displayName) > 30}">
                                            <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                                </c:if>
                                <li>
                                  <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${storedStrategy.strategyId}&stage=strategy',null,null,null,'next')">
                                    ${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if>
                                  </a>
                                </li>
                        </c:forEach>
                    </ul>
                </div>
                <!-- Display the Saved Strategies -->
                
            <div class="original" id="sl_saved" style="display:none">
                    <ul class="menu_section">
                        <c:forEach items="${model.websiteRootCategories}" var="rcs">
                            <c:forEach items="${user.savedStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
                                <c:set var="displayName" value="${storedStrategy.name}" />
                                <c:if test="${fn:length(displayName) > 30}">
                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                                </c:if>
                                <li>
                                  <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${storedStrategy.strategyId}&stage=strategy',null,null,null,'next')">
                                    ${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if>
                                  </a>
                                </li>
                            </c:forEach>
                        </c:forEach>
                    </ul>
                </li>
                    <!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->

                <div class="original" id="sl_recent" style="display:none">
                    <ul class="menu_section">
                        <c:forEach items="${model.websiteRootCategories}" var="rcs">
                            <c:forEach items="${user.recentStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
                                <c:set var="displayName" value="${storedStrategy.name}" />
                                <c:if test="${fn:length(displayName) > 30}">
                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                                </c:if>
                                <li>
                                  <a href="javascript:void(0)" onclick="callWizard('${partialUrl}&insertStrategy=${storedStrategy.strategyId}&stage=strategy',null,null,null,'next')">
                                    ${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if>
                                  </a>
                                </li>
                            </c:forEach>
                        </c:forEach>
                    </ul>
                </div>
        

        <div id="sl_recordclasses" class="original">
            <ul class="menu_section">
            <c:forEach var="rcs" items="${model.websiteRootCategories}">
                <c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
<c:if test="${fn:containsIgnoreCase(rcs.value.displayName, 'gene') || 
        fn:containsIgnoreCase(rcs.value.displayName, 'orf') || 
        fn:containsIgnoreCase(rcs.value.displayName, 'snp') ||
        fn:containsIgnoreCase(rcs.value.displayName, 'isolate')}">
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
        
    </div>

 
