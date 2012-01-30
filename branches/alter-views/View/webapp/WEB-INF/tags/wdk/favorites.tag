<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="w" uri="http://www.servletsuite.com/servlets/wraptag" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="wdkUser" value="${sessionScope.wdkUser}"/>

<c:set var="allFavorites" value="${wdkUser.favorites}" /><%-- a map of (RecordClass, List<Favorite>) --%>
<c:choose>
    <c:when test="${fn:length(allFavorites) == 0}">
        <p>You don't have any favorite records. You can create favorite record on the record page.</p>
    </c:when>
    <c:otherwise> <%-- has favorites --%>

            <ul class="menubar">
              <c:forEach var="fav_item" items="${allFavorites}">
                <c:set var="recordClass" value="${fav_item.key}" />
                <c:set var="favorites" value="${fav_item.value}" /> <%-- a list of favorites of a record type --%>
		<c:set var="idTag" value="${fn:replace(recordClass.fullName, '.', '_')}" />
                <li>
                  <a id="tab_${idTag}" href="javascript:void(0)" onclick="showFavorites('${idTag}')">${recordClass.type}s (${fn:length(favorites)})</a>
                </li>
              </c:forEach>
            </ul>

            <span style="clear:both;font-style:italic;font-size:100%;padding-left:10px;" >(For Help, place your cursor over column headings or icons)</span>
            <input class="favorite-refresh-button" title="Reload the page after you remove some IDs, or add a new project name." type="button" value="Refresh" onclick="window.location.reload();"/>
            <c:forEach var="fav_item" items="${allFavorites}">
              <c:set var="recordClass" value="${fav_item.key}" />
	      <c:set var="idTag" value="${fn:replace(recordClass.fullName, '.', '_')}" /> 
              <div id="favorites_${idTag}" class="favorites_panel">
                <c:set var="favorites" value="${fav_item.value}" /> <%-- a list of favorites of a record type --%>
                <c:set var="recordClass" value="${fav_item.key}" />
                <h3>My Favorite ${recordClass.type} records (${fn:length(favorites)} records)</h3>
                <table class="favorite-list">
                    <tr><th>&nbsp;</th><th>Record</th><th>Note</th><th>Group</th><th></th><th></th></tr>
                    <c:forEach var="favorite" items="${favorites}">
                        <c:set var="record" value="${favorite.recordInstance}" />
                        <c:set var="primaryKey" value="${record.primaryKey}"/>
                        <c:set var="pkValues" value="${primaryKey.values}" />
                        <c:set value="${pkValues['source_id']}" var="id"/>
                        <c:set value="${pkValues['project_id']}" var="pid"/>
                        <tr class="wdk-record" recordClass="${recordClass.fullName}">
                            <td>
                                <div class="primaryKey">
                                    <c:forEach var="pk_item" items="${pkValues}">
                                        <span key="${pk_item.key}">${pk_item.value}</span>
                                    </c:forEach>
                                </div>
                                <img height=20" class="clickable" src="<c:url value='/wdk/images/favorite_color.gif'/>" 
                                     title="Click to remove this item from the Favorite."
                                     onClick="updateFavorite(this, 'remove')"/>
                            </td>
                            <td>
                                <c:set var="url" value="/showRecord.do?name=${recordClass.fullName}" />
                                <c:forEach var="pk_item" items="${pkValues}">
                                    <c:set var="url" value="${url}&${pk_item.key}=${pk_item.value}" />
                                </c:forEach>
                                <a href="<c:url value='${url}' />">${primaryKey.value}</a>
                            </td>
                            <td>
                                <c:set var="noteField" value="${recordClass.favoriteNoteField}" />
                                <c:set var="product" value="${record.attributes[noteField.name]}" />
                                ${product.value}
                            </td>
                            <td>
                                <input type="button" value="Change note" onClick="updateFavoriteNote(this)" />
                            </td>
                            <td>
                                <input type="button" value="Change group" onClick="updateFavoriteGroup(this)" />
                            </td>
                        </tr>
                    </c:forEach>
                </table>
             </c:forEach>
        </div>
    </c:otherwise> <%-- END has favorites --%>
</c:choose>

