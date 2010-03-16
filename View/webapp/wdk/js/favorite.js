// cannot use $ for jQuery, since this script is also included in record page, 
// and might have conflict if gbrowse is included.

function showFavorites() {

}

function updateFavorite(holder, action) {
    // locate the favorite
    var record = getRecord(holder);
    var rcName = jQuery(holder).parents(".wdk-record").attr("recordClass");
    jQuery.ajax({ url: "processFavorite.do?recordClass=" + rcName + "&action=" + action + "&data=" + data,
                  dataType: "json",
                  success: function(data){
                      if (!noUpdate) { // For things like moving columns, do not need to refresh
                          show();
                      }
                  },
                  error : function(data, msg, e){
                      alert("ERROR \n "+ msg + "\n" + e
                           + ". \nReloading this page might solve the problem. \nOtherwise, please contact site support.");
                  }	
    });
}

function updateFavoriteNote(holder) {

}

function updateFavoriteGroup(holder) {

}

function loadFavoriteGroups() {

}

function getRecord(holder) {
    // locate the record
    var record = jQuery(holder).parents(".wdk-record");
    var pkValues = new Object();
    record.find(".primaryKey span").each(new function() {
        var key = jQuery(this).attr("key");
        var value = jQuery(this).text();
        pkValues[key] = value;
    });
    return jQuery.json.serialize(pkValues);
} 
