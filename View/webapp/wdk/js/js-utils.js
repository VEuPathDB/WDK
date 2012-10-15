function getStrategyJSON(backId){
	var strategyJSON = null;
	jQuery.ajax({
		async: false,
		url:"showStrategy.do?strategy=" + backId + "&open=false",
		type: "POST",
		dataType: "json",
		data:"pstate=" + p_state,
		success: function(data){
			for(var s in data.strategies){
				if(s != "length") {
					data.strategies[s].checksum = s;
					strategyJSON = data.strategies[s];
				}
					
			}
		}
	});
	return strategyJSON;
}

function getStrategyOBJ(backId){
	if(getStrategyFromBackId(backId) != false){
		return getStrategyFromBackId(backId);
	}else{
		var json = getStrategyJSON(backId);
		var s = new Strategy(strats.length, json.id, false);
		s.checksum = json.checksum;
		s.JSON = json;
		s.name = json.name;
    s.description = json.description;
		return s;
	}
}

//show the loading icon in the upper right corner of the strategy that is being operated on
function showLoading(divId){
	var d = null;
	var l = 0;
	var t = 0;
	if(divId == undefined){
		d = jQuery("#Strategies");
		le = "10px";
		t = "15px";
		l_gif = "loading.gif";
		sz = "35";
	}else if(jQuery("#diagram_" + divId).length > 0){
		d = jQuery("#diagram_" + divId);
		le = "10px";
		t = "12px";
		l_gif = "loading.gif";
		sz = "35";
	} else {
		d = jQuery("#" + divId);
		le = "405px";
		t = "160px";
		l_gif = "loading.gif";
		sz = "50";
	}
	var l = document.createElement('span');
	jQuery(l).attr("id","loadingGIF");
	var i = document.createElement('img');
	jQuery(i).attr("src","wdk/images/" + l_gif);
	jQuery(i).attr("height",sz);
	jQuery(i).attr("width",sz);
	jQuery(l).prepend(i);
	jQuery(l).css({
		"text-align": "center",
		position: "absolute",
		left: le,
		top: t
	});
	jQuery(d).append(l);
}

// remove the loading icon for the given strategy
function removeLoading(divId){
	if(divId == undefined)
		jQuery("#Strategies span#loadingGIF").remove();
	else
		jQuery("#diagram_" + divId + " span#loadingGIF").remove();
}

// parses the inputs of the question form to be sent via ajax call
function parseInputs(){
        // has to use find in two steps, IE7 cannot find the form using jQuery("#query_form form#form_question")
	var quesForm = jQuery("#query_form").find("form#form_question");
        
        // if the questionForm is popupped by other ways, get it from the opened popup under body.
        if (quesForm.length == 0)
            quesForm = jQuery("body").children("div.crumb_details").find("form#form_question");

        // Jerric - use ajax to serialize the form data
	var d = quesForm.serialize();
        return d;
}

function checkEnter(ele,evt){
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	if(charCode == 13) jQuery(ele).blur();
}

function parseUrlUtil(name,url){
 	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
 	var regexS = "[\\?&]"+name+"=([^&#]*)";
 	var regex = new RegExp( regexS,"g" );
 	var res = new Array();
 	var results = regex.exec( url );
 	if( results != null )
 		res.push(results[1]);
  	if(res.length == 0)
 		return "";
 	else
 		return res;
}


function getDisplayType(type, number){
	if(sz == 1) {
		return type;
	} else if (type.charAt(type.length-1) === 'y') {
		return type.replace(/y$/,'ies');
	} else {
		return type + 's';
	}
}

function initShowHide(details){
	jQuery(".param-group[type='ShowHide']",details).each(function() {
        // register the click event
        var name = jQuery(this).attr("name") + "_details";
        var expire = 365;   // in days
        jQuery(this).find(".group-handle").unbind('click').click(function() {
            var handle = this;
            var path = handle.src.substr(0, handle.src.lastIndexOf("/"));
            var detail = jQuery(this).parents(".param-group").children(".group-detail");
            detail.toggle();
            if (detail.css("display") == "none") {
                handle.src = path + "/plus.gif";
                wdk.createCookie(name, "hide", expire);
            } else {
                handle.src = path + "/minus.gif";
                wdk.createCookie(name, "show", expire);
            }
        });

		// decide whether need to change the display or not
        var showFlag = wdk.readCookie(name);
        if (showFlag == null) return;
        
        var status = jQuery(this).children(".group-detail").css("display");
        if ((showFlag == "show") && (status == "none")) {   
            // should show, but it is hidden
            jQuery(this).find(".group-handle").trigger("click");
        } else if ((showFlag == "hide") && (status != "none")) {
            // should hide, bit it is shown
            jQuery(this).find(".group-handle").trigger("click");
        }
	});
}

/* No longer used... but may put back.
function setFrontAction(action, strat, step) {
	jQuery("#loginForm form[name=loginForm]").append("<input type='hidden' name='action' value='" + action + "'/>");
	jQuery("#loginForm form[name=loginForm]").append("<input type='hidden' name='actionStrat' value='" + strat + "'/>");
	jQuery("#loginForm form[name=loginForm]").append("<input type='hidden' name='actionStep' value='" + step + "'/>");
}
*/

function setDraggable(e, handle){
  var tlimit,
      rlimit,
      blimit;
	rlimit = jQuery("div#contentwrapper").width() - e.width() - 18;
	if(rlimit < 0) rlimit = 525;
	blimit = jQuery("body").height();
  tlimit = $("#contentwrapper").offset().top;
	jQuery(e).draggable({
		handle: handle,
		containment: [0,tlimit,rlimit,blimit]
	});
}
