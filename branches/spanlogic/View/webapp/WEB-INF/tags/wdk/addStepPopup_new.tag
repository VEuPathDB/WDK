<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="model"
	      type="org.gusdb.wdk.model.jspwrap.WdkModelBean"
              required="true"
              description="Wdk Model Object for this site"
%>

<%@ attribute name="rcName"
	      required="true"
              description="RecordClass Object for the Answer"
%>

<%@ attribute name="prevStepNum"
	      required="false"
	      description="Step number for transform param urls"
%>

<%@ attribute name="isAdd"
	      required="false"
	      description="true = adding a step.  false = inserting a step"
%>

<c:set var="siteName" value="${applicationScope.wdkModel.name}" />
<c:set var="qSetName" value="none" />
<c:set var="qSets" value="${model.questionSetsMap}" />
<c:set var="qSet" value="${qSets[qSetName]}" />
<c:set var="user" value="${sessionScope.wdkUser}"/>
<%-- the type is of the previous step, that is the input type of the new step  --%>
<c:set var="recordClass" value="${model.recordClassMap[rcName]}" />

<c:if test="${isAdd == 'false'}">
    <%-- insert a step in between, the transform cannot change type in this case --%>
    <jsp:setProperty name="recordClass" property="changeType" value="false" />
</c:if>
<c:set var="transformQuestions" value="${recordClass.transformQuestions}" />

<div id="query_form" style="min-height:140px;">
	<span class="dragHandle">
		<div class="modal_name">
			<h1 style="font-size:130%;margin-top:4px;" id="query_form_title"></h1>
		</div>
		<a class='close_window' href='javascript:closeAll()'>
			<img src="<c:url value='/wdk/images/Close-X-box.png'/>" alt='Close'/>
		</a>
	</span>
	<div id="sections">
		<table id="sections-layout"><tr>
			<td id="section-1">
				<div class="qf_section" id="step_type">
					<ul>
						<li class="category" onclick="showNewSection(this,'boolean',2)" style="background-color:#DDDDDD; font-weight:bold">Add a Set Operation</li>
						<c:if test="${fn:length(transformQuestions) > 0}">
							<li class="category" onclick="showNewSection(this,'transforms',2)">Add a Tranform</li>
						</c:if>
						<li class="category" onclick="showNewSection(this,'span_logic',2)">Add a Span Operation</li>
					</ul>
				</div>
			</td>
			<td id="section-2">
				<div class="qf_section" id="boolean" style="">
				<ul>
					<li class="category" onclick="showNewSection(this,'boolean_searches',3)" style="background-color:#DDDDDD; font-weight:bold">Run a Search</li>
					<li class="category" onclick="showNewSection(this,'boolean_strategies',3)">Nest a Strategy</li>
					<%--<li><a href="javascript:void(0)" onclick="showNewSection(this,'boolean_basket')"></a>Basket</li>--%>
					<c:if test="${recordClass.hasBasket}">
						<c:set var="q" value="${recordClass.snapshotBasketQuestion}" />
						<li style="width:auto;z-index:40;" onclick="getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">Copy from Basket</li>
					</c:if>
				</ul>
			</div></td>
			<td id="section-3">
				<div class="qf_section" id="boolean_searches" style="background-color:#EEEEEE;">
					<ul>
						<c:set var="rootCat" value="${model.websiteRootCategories[rcName]}" />
						<c:choose>
						<c:when test="${rootCat.multiCategory}">
						<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
				    		<c:set var="cat" value="${catEntry.value}" />
							<li class="category" onclick="showNewSection(this,'${cat.name}',4)">${cat.displayName}</li>
						</c:forEach>
						</c:when>
						<c:otherwise>
						<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
				    	<c:set var="cat" value="${catEntry.value}" />
						<c:forEach items="${cat.websiteQuestions}" var="q">
							<li onclick="javascript:getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">${q.displayName}</li>
						</c:forEach>
						</c:forEach>
						</c:otherwise>
						</c:choose>
					</ul>
				</div>
			</td>
			<td id="section-4"></td>
			<td id="section-5"></td>	
		</tr></table>
		
		<div class="original" id="boolean" style="display:none">
			<ul>
				<li class="category" onclick="showNewSection(this,'boolean_searches',3)">Run a Search</li>
				<li class="category" onclick="showNewSection(this,'boolean_strategies',3)">Nest a Strategy</li>
				<%--<li><a href="javascript:void(0)" onclick="showNewSection(this,'boolean_basket')"></a>Basket</li>--%>
				<c:if test="${recordClass.hasBasket}">
					<c:set var="q" value="${recordClass.snapshotBasketQuestion}" />
					<li style="width:auto;z-index:40;" onclick="getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">Copy from Basket</li>
				</c:if>
			</ul>
		</div>
<!--TRANSFORMS SECTION-->		
		<div class="original" id="transforms" style="display:none">
			<ul>
				<c:forEach items="${transformQuestions}" var="t">
			      <jsp:setProperty name="t" property="inputType" value="${rcName}" />
			      <c:set var="tparams" value="" />
			      <c:forEach items="${t.transformParams}" var="tp">
				<c:set var="tparams" value="${tparams}&${tp.name}=${prevStepNum}" />
			      </c:forEach>
			      <li onclick="getQueryForm('showQuestion.do?questionFullName=${t.fullName}${tparams}&partial=true', true)">${t.displayName}</li>
			    </c:forEach>
			</ul>
		</div>

<!-- BOOLEAN SEARCHES SECTION -->		
		<div class="original" id="boolean_searches" style="display:none">
			<ul>
				<c:set var="rootCat" value="${model.websiteRootCategories[rcName]}" />
				<c:choose>
				<c:when test="${rootCat.multiCategory}">
				<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
		    		<c:set var="cat" value="${catEntry.value}" />
					<li class="category" onclick="showNewSection(this,'${cat.name}',4)">${cat.displayName}</li>
				</c:forEach>
				</c:when>
				<c:otherwise>
				<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
		    	<c:set var="cat" value="${catEntry.value}" />
				<c:forEach items="${cat.websiteQuestions}" var="q">
					<li onclick="javascript:getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">${q.displayName}</li>
				</c:forEach>
				</c:forEach>
				</c:otherwise>
				</c:choose>
			</ul>
		</div>
		
		<c:set var="rootCat" value="${model.websiteRootCategories[rcName]}" />
		<c:forEach items="${rootCat.websiteChildren}" var="catEntry">
			<c:set var="cat" value="${catEntry.value}" />
			<div class="original" id="${cat.name}" style="display:none">
				<ul>
					<c:forEach items="${cat.websiteQuestions}" var="q">
   						<li onclick="getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">${q.displayName}</li>
					</c:forEach>
				</ul>
			</div>
		</c:forEach>
		
		<div class="original" id="boolean_strategies" style="display:none">
			<ul>
				<li class="category" onclick="showNewSection(this,'boolean_open',4)">Open Strategies</li>
				<c:set var="cls" value=""/>
				<c:set var="cats" value=""/>
				<c:if test="${fn:length(user.savedStrategiesByCategory[rcName]) > 0}">
					<c:set var="cls" value="showNewSection(this,'boolean_saved',4)"/>
					<c:set var="cats" value="category"/>
				</c:if>
				<li class="${cats}" onclick="${cls}">Saved Strategies (${fn:length(user.savedStrategiesByCategory[rcName])})</li>
				<c:set var="clr" value=""/>
				<c:set var="catr" value=""/>
				<c:if test="${fn:length(user.recentStrategiesByCategory[rcName]) > 0}">
					<c:set var="clr" value="showNewSection(this,'boolean_recent',4)"/>
					<c:set var="catr" value="category"/>
				</c:if>
				<li class="${catr}" onclick="${clr}">Recent Strategies (${fn:length(user.recentStrategiesByCategory[rcName])})</li>
			</ul>
		</div>
		<div class="original" id="boolean_open" style="display:none">
					<ul>
						<c:forEach items="${user.activeStrategies}" var="storedStrategy">
				 			<c:if test="${storedStrategy.type == rcName}">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
		                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
		                        </c:if>
								<li><a href="javascript:void(0)" onclick="openAddStrategy(${storedStrategy.strategyId})">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
							</c:if>
						</c:forEach>
					</ul>
		</div>
				<!-- Display the Saved Strategies -->
				
		<div class="original" id="boolean_saved" style="display:none">
					<ul>
						<c:forEach items="${user.savedStrategiesByCategory[rcName]}" var="storedStrategy">
							<c:set var="displayName" value="${storedStrategy.name}" />
							<c:if test="${fn:length(displayName) > 30}">
		                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
		                    </c:if>
							<li><a href="javascript:void(0)" onclick="openAddStrategy(${storedStrategy.strategyId})">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
						</c:forEach>
					</ul>
		</div>
				<!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->
				
		<div class="original" id="boolean_recent" style="display:none">
					<ul>
						<c:forEach items="${user.recentStrategiesByCategory[rcName]}" var="storedStrategy">
							<c:set var="displayName" value="${storedStrategy.name}" />
							<c:if test="${fn:length(displayName) > 30}">
		                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
		                    </c:if>
							<li><a href="javascript:void(0)" onclick="openAddStrategy(${storedStrategy.strategyId})">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
						</c:forEach>
					</ul>
		</div>
		
<!-- SPAN LOGIC SECTION -->
		<div id="span_logic" style="display:none" class="original">
			<ul>
				<li class="category" onclick="showNewSection(this,'sl_recordclasses',3)">Run a Search</li>
				<li class="category" onclick="showNewSection(this,'sl_strategies',3)">Nest a Strategy</li>
				<li>Copy from Basket</li>
				<li>Include All Genes</li>
				<li>Include All ORFs</li>
				<li>Include All SNPs</li>
			</ul>
		</div>

		<div class="original" id="sl_strategies" style="display:none">
			<ul>
				<li class="category" onclick="showNewSection(this,'sl_open',4)">Open Strategies</li>
				<c:set var="cls" value=""/>
				<c:set var="cats" value=""/>
				<c:set var="count_s" value=""/>
				<c:forEach items="${model.websiteRootCategories}" var="rcs">
					<c:set var="count_s" value="${count_s + fn:length(user.savedStrategiesByCategory[rcs.value.name])}"/>
				</c:forEach>
				<c:if test="${count_s > 0}">
					<c:set var="cls" value="showNewSection(this,'sl_saved',4)"/>
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
					<c:set var="clr" value="showNewSection(this,'sl_recent',4)"/>
					<c:set var="catr" value="category"/>
				</c:if>
				<li class="${catr}" onclick="${clr}">Recent Strategies</li>
			</ul>
		</div>
		
		<div class="original" id="sl_open" style="display:none">
					<ul>
						<c:forEach items="${user.activeStrategies}" var="storedStrategy">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
		                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
		                        </c:if>
								<li><a href="javascript:void(0)" onclick="openAddStrategy(${storedStrategy.strategyId})">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
						</c:forEach>
					</ul>
				</div>
				<!-- Display the Saved Strategies -->
				
			<div class="original" id="sl_saved" style="display:none">
					<ul>
						<c:forEach items="${model.websiteRootCategories}" var="rcs">
							<c:forEach items="${user.savedStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
			                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
			                    </c:if>
								<li><a href="javascript:void(0)" onclick="openAddStrategy(${storedStrategy.strategyId})">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
							</c:forEach>
						</c:forEach>
					</ul>
				</li>
					<!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->

				<div class="original" id="sl_recent" style="display:none">
					<ul>
						<c:forEach items="${model.websiteRootCategories}" var="rcs">
							<c:forEach items="${user.recentStrategiesByCategory[rcs.value.name]}" var="storedStrategy">
								<c:set var="displayName" value="${storedStrategy.name}" />
								<c:if test="${fn:length(displayName) > 30}">
			                    	<c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
			                    </c:if>
								<li><a href="javascript:void(0)" onclick="openAddStrategy(${storedStrategy.strategyId})">${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></a></li>
							</c:forEach>
						</c:forEach>
					</ul>
				</div>
		

		<div id="sl_recordclasses" style="display:none" class="original">
			<ul>
			<c:forEach var="rcs" items="${model.websiteRootCategories}">
				<c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
				<li class="category" onclick="showNewSection(this,'sl_${classId}',4)">${rcs.value.displayName}</li>
			</c:forEach>
			</ul>
		</div>
		
		<c:forEach var="rcs" items="${model.websiteRootCategories}">
			<c:set var="classId" value="${fn:replace(rcs.value.name,'.','_')}"/>
			<div class="original" id="sl_${classId}" style="display:none">
				<ul>
					<c:choose>
					<c:when test="${rcs.value.multiCategory}">
					<c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
			    		<c:set var="cat" value="${catEntry.value}" />
						<li class="category" onclick="showNewSection(this,'sl_${cat.name}',5)">${cat.displayName}</li>
					</c:forEach>
					</c:when>
					<c:otherwise>
					<c:forEach items="${rcs.value.websiteChildren}" var="catEntry">
			    	<c:set var="cat" value="${catEntry.value}" />
					<c:forEach items="${cat.websiteQuestions}" var="q">
						<li onclick="getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true',true)">${q.displayName}</li>
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
					<ul>
						<c:forEach items="${cat.websiteQuestions}" var="q">
        					<li onclick="getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true',true)">${q.displayName}</li>
						</c:forEach>
					</ul>
				</div>
			</c:forEach>
		</c:forEach>
		
	</div>
	<div class="bottom-close">
		<a class='close_window' href='javascript:closeAll(false)'>Close</a>
	</div>
</div><!-- End of Query Form Div -->


