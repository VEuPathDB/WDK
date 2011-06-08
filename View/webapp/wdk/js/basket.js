
function showBasket(recordClass, type){	
	var url = "showBasket.do";
	var d = new Object();
	if (recordClass) {
		 d.recordClass = recordClass;
		$("#basket .menubar .selected_type").removeClass("selected_type");
		$(".basket_panel").hide();
		$("#basket .menubar li").each(function() {
			var id = $("a", this).attr("id");
			if (id == 'tab_' + type) {
				$(this).addClass("selected_type");
			}
		});
		$("div#basket_" + type).show();
	}
	$.ajax({
		url: url,
		data: d,
		type: "post",
		dataType: "html",
		beforeSend:function(){
			$("body").block();
		},
		success: function(data){
			if (recordClass) {
				setCurrentTabCookie('basket', type);
				$("div#basket_" + type + " > div.Workspace").html(data);
				if ($("div#basket_" + type).find("table").length > 0) {
					$("input#empty-basket-button").attr("disabled",false);
					$("input#make-strategy-from-basket-button").attr("disabled",false);
					// create multi select control for adding columns
					checkPageBasket();
					createFlexigridFromTable($("#basket_" + type).find("#Results_Table"));
					try {
						customBasketPage();
					}
					catch(err) {
						//Do nothing
					}
				}else{
					$("input#empty-basket-button").attr("disabled",true);
					$("input#make-strategy-from-basket-button").attr("disabled",true);
				}
			}
			else {
				$("div#basket").html(data);
				var current = getCurrentTabCookie('basket');
				if (current && $("div#basket ul.menubar a#tab_" + current).length > 0) {
					$("div#basket ul.menubar a#tab_" + current).click();
				} else {
					$("div#basket ul.menubar a:first").click();
				}
			}
			$("body").unblock();
		},
		error: function(data,msg,e){
			alert("Error occured in showBasket() function!!");
			$("body").unblock();
		}
	});
}

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

function emptyBasket() {
	var recordClass = jQuery("div#" + getCurrentBasketWrapper()).attr("recordClass");
	var display = jQuery("div#" + getCurrentBasketWrapper()).attr("displayName");
	var message = jQuery("#basketConfirmation");
	$("#basketName", message).text(display);
	$("form", message).submit(function() {
		updateBasket(this,'clear',0,0,recordClass);
	});		
	jQuery.blockUI({message : message});
}

function saveBasket() {
	var recordClass = jQuery("div#" + getCurrentBasketWrapper()).attr("recordClass");
	recordClass = recordClass.replace('.', '_');	
	window.location='processQuestion.do?questionFullName=InternalQuestions.' + recordClass +
		'BySnapshotBasket&' + recordClass + 'Dataset_type=basket&questionSubmit=Run+Step';
}

//Shopping basket on clickFunction
function updateBasket(ele, type, pk, pid,recordType) {
	var i = jQuery(ele);
	if(ele.tagName != "IMG")
		i = jQuery("img",ele);

    // show processing icon, will remove it when the process is completed.
    var oldImage = i.attr("src");
    i.attr("src","wdk/images/loading.gif");

	var a = new Array();
	var action = null;
	var da = null;
	if(type != 'recordPage') var currentDiv = getCurrentBasketWrapper();
	var count = 0;
	if(type == "recordPage"){
		var o = new Object();
		var pkDiv = jQuery(i).parents(".wdk-record").find("span.primaryKey");
		jQuery("span", pkDiv).each(function(){
			o[jQuery(this).attr("key")] = jQuery(this).text();
		});
		a.push(o);
		da = jQuery.json.serialize(a);
		action = (i.attr("value") == '0') ? "add" : "remove";
	}else if(type == "single"){
		var o = new Object();

		//var pkDiv = jQuery("#" + currentDiv + " .Results_Pane div.primaryKey[fvalue='" + pk + "']");
		var pkDiv = jQuery(ele).parents("tr").find("div.primaryKey");

		jQuery("span", pkDiv).each(function(){
			o[jQuery(this).attr("key")] = jQuery(this).text();
		});
		a.push(o);
		da = jQuery.json.serialize(a);
		action = (i.attr("value") == '0') ? "add" : "remove";
	}else if(type == "page"){
		jQuery("#" + currentDiv + " .Results_Pane div.primaryKey").each(function(){
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
					$("#" + currentDiv + " .Results_Pane").block();
				}
				//jQuery("body").block();
			},
			success: function(data){
				//jQuery("body").unblock();
				if (action == 'add-all' || type == 'page') {
					$("#" + currentDiv + " .Results_Pane").unblock();
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
					if(type == "recordPage"){
						if(action == "add")
							i.parent().prev().html("Remove from Basket");
						else
							i.parent().prev().html("Add to Basket");
					}
                                        // the image has been updated, no need to restore it again.
                                        oldImage = null;
				}else if(type == "clear"){
					showBasket();
				}else{
					if(action == "add-all" || action == "add") {
						jQuery("div#" + currentDiv + " div.Results_Div img.basket").attr("src","wdk/images/basket_color.png");
						jQuery("div#" + currentDiv + " div.Results_Div img.basket").attr("title","Click to remove this item from the basket.");
						jQuery("div#" + currentDiv + " div.Results_Div img.basket").attr("value", "1");
					}else{
						jQuery("div#" + currentDiv + " div.Results_Div img.basket").attr("src","wdk/images/basket_gray.png");
						jQuery("div#" + currentDiv + " div.Results_Div img.basket").attr("title","Click to add this item to the basket.");
						jQuery("div#" + currentDiv + " div.Results_Div img.basket").attr("value", "0");
					}
				}
					updateBasketCount(data.count);
				if(type != 'recordPage'){
					checkPageBasket();
                                        // the image has been updated, no need to restore it again.
                                        oldImage = null;
				}
				if (currentDiv.match(/basket/)) {
					//Using cookie to determine that the results need to be updated when the 'Opened' tab is selected
					jQuery.cookie("refresh_results", "true", { path : '/' });
				}
                                if (oldImage != null) i.attr("src", oldImage);
			},
			error: function(){
				//jQuery("body").unblock();
				alert("Error adding item to basket!");
                                i.attr("src", oldImage);
			}
		});
}


function updateBasketCount(c){
		jQuery("#menu a#mybasket span.subscriptCount").text("(" + c + ")");
}

function checkPageBasket(){
	var current = getCurrentBasketWrapper();
	if (guestUser == 'true') {
		jQuery("div#" + current + " div.Results_Div img.head.basket").attr("src","wdk/images/basket_gray.png");
		jQuery("div#" + current + " div.Results_Div img.head.basket").attr("title","Please log in to use the basket.");
	}
	else {
		allIn = true;
		jQuery("div#" + current + " div.Results_Div img.basket").each(function(){
			if(!(jQuery(this).hasClass("head"))){
				if(jQuery(this).attr("value") == 0){
					allIn = false;
				}
			}
		});
		if(allIn){
			jQuery("div#" + current + " div.Results_Div img.head.basket").attr("src","wdk/images/basket_color.png");
			jQuery("div#" + current + " div.Results_Div img.head.basket").attr("title","Click to remove the items in this page from the basket.");
			jQuery("div#" + current + " div.Results_Div img.head.basket").attr("value", "1");
		}else{
			jQuery("div#" + current + " div.Results_Div img.head.basket").attr("src","wdk/images/basket_gray.png");
			jQuery("div#" + current + " div.Results_Div img.head.basket").attr("title","Click to add the items in this page to the basket.");
			jQuery("div#" + current + " div.Results_Div img.head.basket").attr("value", "0");
		}
	}
}

function getCurrentBasketWrapper() {
	if (jQuery("#strategy_results").css('display') == 'none') {
		return "basket_" + getCurrentTabCookie('basket');
	}
	return "strategy_results";
}
