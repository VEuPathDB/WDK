// =============================================================================
// the common functions provided by wdk
// =============================================================================

function WDK() {

    // -------------------------------------------------------------------------
    // cookie handling methods
    // -------------------------------------------------------------------------
    this.createCookie = function(name,value,days) {
    	if (days) {
    		var date = new Date();
    		date.setTime(date.getTime()+(days*24*60*60*1000));
    		var expires = "; expires="+date.toGMTString();
    	}
    	else var expires = "";
    	document.cookie = name+"="+value+expires+"; path=/";
    };

    this.readCookie = function(name) {
    	var nameEQ = name + "=";
    	var ca = document.cookie.split(';');
    	for(var i=0;i < ca.length;i++) {
    		var c = ca[i];
    		while (c.charAt(0)==' ') c = c.substring(1,c.length);
    		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    	}
    	return null;
    };

    this.eraseCookie = function(name) {
    	createCookie(name,"",-1);
    }

}



function uncheckFields(notFirst) {
    var form = document.downloadConfigForm;
    var cb = form.selectedFields;
    if (notFirst) {
        for (var i=1; i<cb.length; i++) {
            if (cb[i].disabled) continue;
            cb[i].checked = null;
        }
    } else {
        cb[0].checked = null;
    }
}

function checkFields(all) {
    var form = document.downloadConfigForm;
    var cb = form.selectedFields;
    cb[0].checked = (all > 0 ? null : 'checked');
    for (var i=1; i<cb.length; i++) {
        if (cb[i].disabled) continue;
        cb[i].checked = (all > 0 ? 'checked' : null);
    }
}

function chooseAll(bool, form, node) {
    if (form[node].type == 'select-multiple') {
      multiSelectAll(bool, form, node);
    } else {
      checkAll(bool, form, node);
    }
}

//function checkAll(bool, form, node) {
//    var cb = form[node];
//    cb[0].checked = (bool ? null : 'checked');
//    for (var i=0; i<cb.length; i++) {
//        cb[i].checked = (bool ? 'checked' : null);
//   }
//}

function checkAll(bool, form, node) {
    var cb = form[node];//document.getElementsByName(node);
    for (var i=0; i<cb.length; i++) {
         if (cb[i].disabled) continue;
	 if(bool && cb[i].checked == false) cb[i].click();
         if(!bool && cb[i].checked == true) cb[i].click();
    }
}
