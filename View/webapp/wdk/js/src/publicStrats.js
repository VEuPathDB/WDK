wdk.util.namespace("window.wdk.publicStrats", function(ns, $) {
  "use strict";
  
  var publicStratDescriptionWarning = "Before making your strategy public, you " +
      "must add a description so others know how it can be used.";
  
  function showPublicStrats() {
    $("body").block();
    $.ajax({
      url: "showPublicStrats.do",
      dataType: "html",
      success: function(data) {
        
        // put retrieved html in the correct div and configure data table
        $("#public_strat").html(data);
        configurePublicStratTable();
        
        // update the number of public strategies
        $("#strategy_tabs li a#tab_public_strat font.subscriptCount")
            .html("(" + $("#public_strat span#publicStrategyCount").html() + ")");

        // unblock the UI
        $("body").unblock();
      },
      error: function(data, msg, e) {
        $("body").unblock();
        alert("ERROR\n"+ msg + "\n" + e +
            "\nReloading this page might solve the problem.\n" +
            "Otherwise, please contact site support.");
      }
    });
  }

  function configurePublicStratTable() {
    $("#public_strat table.datatables").dataTable({
        "bAutoWidth": false,
        "bJQueryUI": true,
        "bScrollCollapse": true,
        "aoColumns": [ null,
                       null,
                       { "bSortable": false },
                       null,
                       null,
                       null],
        "aaSorting": [[ 5, "desc" ]]
    });
  }
  
  function togglePublic(checkbox, stratId) {
    var isPublic = $(checkbox).prop('checked');
    var description = $(checkbox).parent().parent().find('.strategy_description div').html();
    if (description == "Click to add a description" || description.trim() == "") {
      alert(publicStratDescriptionWarning);
      $(checkbox).prop('checked', !isPublic);
      return;
    }
    $(checkbox).parent().find('img').css('display','inline-block');
    jQuery.ajax({
      type : "POST",
      url : "processPublicStratStatus.do",
      data : { "stratId" : stratId, "isPublic" : isPublic },
      dataType : "json",
      success : function(data, textStatus, jqXHR ) {
        // set data on row to updated value
        $(checkbox).parents(".strategy-data").data("isPublic", ($(checkbox).prop('checked') ? "true" : "false"));
        // remove spinner; operation complete
        $(checkbox).parent().find('img').css('display','none');
        // do nothing else to inform user
        //var publicStatus = isPublic ? "public" : "private";
        //alert("Successfully set strat with ID " + stratId + " to " + publicStatus + ".");
      },
      error : function(jqXHR, textStatus, errorThrown) {
        $(checkbox).parent().find('img').css('display','none');
        alert("We are unable to change the status of this strategy at this time.  " +
              "Please try again later.  If the problem persists, please use the " +
              "'Contact Us' link above to inform us.");
        $(checkbox).prop('checked', !isPublic);
      }
    });
  }
  
  function goToPublicStrats() {
    // first set cookie to show public strats tab
    wdk.stratTabCookie.setCurrentTabCookie('application', 'public_strat');
    // then move location to strategies workspace
    window.location = "showApplication.do";
  }
  
  // make the following methods "public" (i.e. available in the namespace)
  ns.showPublicStrats = showPublicStrats;
  ns.configurePublicStratTable = configurePublicStratTable;
  ns.togglePublic = togglePublic;
  ns.goToPublicStrats = goToPublicStrats;
  ns.publicStratDescriptionWarning = publicStratDescriptionWarning;
  
});
