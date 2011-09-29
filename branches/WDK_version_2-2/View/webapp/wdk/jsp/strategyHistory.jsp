<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />
<c:set var="wdkUser" value="${sessionScope.wdkUser}" />

<script type="text/javascript">
$(document).ready(function() {
    $("#search_history table.datatables").dataTable( {
        "bJQueryUI": true,
        "bScrollCollapse": true,
        "aoColumns": [ { "bSortable": false }, 
                       null, 
                       null,
                       { "bSortable": false },
                       { "bSortable": false },
                       null, 
                       null, 
                       null, 
                       null, 
                       { "bSortable": false } ],
        "aaSorting": [[ 5, "desc" ]],
    } );
} );
</script>

<wdk:strategyHistory model="${wdkModel}" user="${wdkUser}" />
