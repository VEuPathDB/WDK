function ChangeBasket(url, noUpdate) {
	jQuery("body").block();
	jQuery.ajax({
		url: url,
		dataType: "html",
		success: function(data){
			jQuery("body").unblock();  //Gets blocked again by the next line anyway
			if (!noUpdate) { // For things like moving columns, don't need to refresh
				showBasket();
			}
		},
		error : function(data, msg, e){
			  alert("ERROR \n "+ msg + "\n" + e
                                      + ". \nReloading this page might solve the problem. \nOtherwise, please contact site support.");
		}
	});
}

//Shopping basket on clickFunction
function updateBasket(ele, type, pk, pid,recordType) {
	var i = jQuery("img",ele);
	var a = new Array();
	var action = null;
	var da = null;
	if(type != 'recordPage') var currentDiv = getCurrentBasketWrapper();
	var count = 0;
	if(type == "recordPage"){
		var o = new Object();
		var pkDiv = jQuery("div.primaryKey");
		jQuery("span", pkDiv).each(function(){
			o[jQuery(this).attr("key")] = jQuery(this).text();
		});
		a.push(o);
		da = jQuery.json.serialize(a);
		action = (i.attr("value") == '0') ? "add" : "remove";
	}else if(type == "single"){
		var o = new Object();
		var pkDiv = jQuery("#" + currentDiv + " #Results_Pane div.primaryKey[fvalue='" + pk + "']");
		jQuery("span", pkDiv).each(function(){
			o[jQuery(this).attr("key")] = jQuery(this).text();
		});
		a.push(o);
		da = jQuery.json.serialize(a);
		action = (i.attr("value") == '0') ? "add" : "remove";
	}else if(type == "page"){
		jQuery("#" + currentDiv + " #Results_Pane div.primaryKey").each(function(){
			var o = new Object();
			jQuery("span",this).each(function(){;
				o[jQuery(this).attr("key")] = jQuery(this).text();
			});
			a.push(o);
		});
		action = (i.attr("value") == '0') ? "add" : "remove";
		da = jQuery.json.serialize(a);
	}else if(type == "clear"){
		action = "clear";
	}else{
		da = type;
		action = "add-all";//(i.attr("value") == '0') ? "add-all" : "remove-all";
	}
	
	var d = "action="+action+"&type="+recordType+"&data="+da;
		jQuery.ajax({
			url: "processBasket.do",
			type: "post",
			data: d,
			dataType: "json",
			beforeSend: function(){
				if (action == 'add-all' || type == 'page') {
					$("#" + currentDiv + " #Results_Pane").block();
				}
				//jQuery("body").block();
			},
			success: function(data){
				//jQuery("body").unblock();
				if (action == 'add-all' || type == 'page') {
					$("#" + currentDiv + " #Results_Pane").unblock();
				}
				if(type == "single" || type == "recordPage"){
					if(action == "add") {
						i.attr("src","wdk/images/basket_color.png");
						i.attr("value", "1");
						i.attr("title","Click to remove this item from the basket.");
					}else{
						i.attr("src","wdk/images/basket_gray.png");
						i.attr("value", "0");
						i.attr("title","Click to add this item to the basket.");
					}
				}else if(type == "clear"){
					showBasket();
				}else{
					if(action == "add-all" || action == "add") {
						jQuery("div#" + currentDiv + " div#Results_Div img.basket").attr("src","wdk/images/basket_color.png");
						jQuery("div#" + currentDiv + " div#Results_Div img.basket").attr("title","Click to remove this item from the basket.");
						jQuery("div#" + currentDiv + " div#Results_Div img.basket").attr("value", "1");
					}else{
						jQuery("div#" + currentDiv + " div#Results_Div img.basket").attr("src","wdk/images/basket_gray.png");
						jQuery("div#" + currentDiv + " div#Results_Div img.basket").attr("title","Click to add this item to the basket.");
						jQuery("div#" + currentDiv + " div#Results_Div img.basket").attr("value", "0");
					}
				}
					updateBasketCount(data.count);
				if(type != 'recordPage'){
					checkPageBasket();
				}
				if (currentDiv == 'basket') {
					//Using cookie to determine that the results need to be updated when the 'Opened' tab is selected
					jQuery.cookie("refresh_results", "true", { path : '/' });
					// If any results are showing (and we're not already on the results page) update them.
					/*var currentStep = jQuery("#Strategies div.selected");
					if (currentStep.length == 0) currentStep = jQuery("#Strategies div.selectedarrow");
					if (currentStep.length == 0) currentStep = jQuery("#Strategies div.selectedtransform");
					var active_link = jQuery("a.results_link", currentStep);
					if(active_link.length == 0) active_link = jQuery(".resultCount a.operation", currentStep);
					active_link.click();*/
				}
			},
			error: function(){
				//jQuery("body").unblock();
				alert("Error adding Gene to basket!");
			}
		});
}


function updateBasketCount(c){
		jQuery("#menu a#mybasket span.subscriptCount var").text(c)
}

function checkPageBasket(){
	var current = getCurrentBasketWrapper();
	if (guestUser == 'true') {
		jQuery("div#" + current + " div#Results_Div img.head.basket").attr("src","wdk/images/basket_gray.png");
		jQuery("div#" + current + " div#Results_Div img.head.basket").attr("title","Please log in to use the basket.");
	}
	else {
		allIn = true;
		jQuery("div#" + current + " div#Results_Div img.basket").each(function(){
			if(!(jQuery(this).hasClass("head"))){
				if(jQuery(this).attr("value") == 0){
					allIn = false;
				}
			}
		});
		if(allIn){
			jQuery("div#" + current + " div#Results_Div img.head.basket").attr("src","wdk/images/basket_color.png");
			jQuery("div#" + current + " div#Results_Div img.head.basket").attr("title","Click to remove the items in this page from the basket.");
			jQuery("div#" + current + " div#Results_Div img.head.basket").attr("value", "1");
		}else{
			jQuery("div#" + current + " div#Results_Div img.head.basket").attr("src","wdk/images/basket_gray.png");
			jQuery("div#" + current + " div#Results_Div img.head.basket").attr("title","Click to add the items in this page to the basket.");
			jQuery("div#" + current + " div#Results_Div img.head.basket").attr("value", "0");
		}
	}
}

function getCurrentBasketWrapper() {"strategy_results";
	if (jQuery("#strategy_results").css('display') == 'none') {
		return "basket";
	}
	return "strategy_results";
}
