/*	var c = null;
	var cxt = null;
	var scale = null; // Scale is an integer for number of nucleotides per 1px.
	var feature = null; // deafult length of the feature.
	var region = null;
	var center = null;
	var draw = false;
	var singlepoint = false;
*/	
	var a = null;
	var b = null;
	var region_color = ["rgba(100,100,200,0.5)","rgba(0,128,0,0.5)"];
	function Diagram(name,ele){
		this.name = name;
		this.c = ele;
		this.cxt = $(this.c);//this.c.getContext('2d');
		this.scale = null; // Scale is an integer for number of nucleotides per 1px.
		this.feature = null; // deafult length of the feature.
		this.region = null;
		this.center = null;
		this.draw = false;
		this.singlepoint = false;
	}
	
	function initWindow(){ 
		$("input[type=radio][name^=region_][value=exact]").click();
		attachHandlers();
		//Should find a way to eliminate this call.
		updateStepNumberReferences(); //This gets called again later, by the wizard mechanism
		initOutputOptions();
		prepCanvas();
	//	prepSentence();
//		prepDynamicSpans();
	}

	function attachHandlers(){
		$("#submitButton").click(function(){
			// Enable params before the form action (callWizard()) is hit, so that
			// they will be included in the serialization performed in parseInputs()
			$(".offsetOptions input, .offsetOptions select").removeAttr("disabled");
		});
		$("select[name*='_a'], input[name*='_a']").change(function(){
			redraw(true,"a");
		});
		$("select[name*='_b'], input[name*='_b']").change(function(){
			redraw(true,"b");
			$(this).keypress();
		});
		$("input[name='upstream_region_a'], input[name*='downstream_region_a'], input[name='upstream_region_b'], input[name*='downstream_region_b']").change(function(){
			var group = $(this).attr('name');
			group = group.substring(group.indexOf("region_")+7);
			if ($(this).attr("name").indexOf('upstream') >=0) {
				$("#span_begin_offset_" + group).val($(this).val()).change();
			}
			else if ($(this).attr("name").indexOf('downstream') >= 0) {
				$("#span_end_offset_" + group).val($(this).val()).change();
			}
			else {
				// TODO: Error case
			}
		});
		$("#spanLogicParams input[type='text']").keydown(function(event){
			if(event.keyCode == 13 && event.currentTarget.id != 'submitButton') {
				event.preventDefault();
				$(this).change();
			}
		});
		$("#span_output").change(function(){
			$(".span_output").text($("option:selected",this).text());
			updateStepReferences();
		});
		$("#span_operation").change(function(){
			var selectedOperation = $(this).val();
			$(".operation-help .operation").attr("style","opacity:0.3;filters:alpha(opacity=30);");
			$(".operation-help .operation." + selectedOperation).removeAttr("style");
			$(".span_operation").text($("option:selected",this).text());
		});
		$("#span_strand").change(function(){
			$(".span_strand").text($("option:selected",this).text());
		});
	}
	function initOutputOptions(){
		$("#span_output option[value='a']").text(
			$("#span_a_type").val() + " from Step " + $("#span_a_num").text());
		$("#span_output option[value='b']").text(
			$("#span_b_type").val() + " from Step " + $("#span_b_num").text());
		$("#span_output").change();
		$("#span_operation").change();
		$("#span_strand").change();
	}
	function updateStepReferences(){
		var output = $("#span_output").val();
		var outputType = $("#span_" + output + "_type").val();
		var outputNum = $("#span_" + output + "_num").text();
		if (outputType) {
			var comparison = $("#span_output option[value!='" + output + "']").val();
			var comparisonType = $("#span_" + comparison + "_type").val();
			var comparisonNum = $("#span_" + comparison + "_num").text();
			$(".comparison_type").text(comparisonType);
			$(".comparison_num").text(comparisonNum);
			// Swap the output and comparison groups if needed
			if ($("#outputGroup #group_" + output).length === 0) {
				$(".outputRegion").removeClass("region_"+comparison).addClass("region_"+output);
				$(".comparisonRegion").removeClass("region_"+output).addClass("region_"+comparison);
				var comparisonGroup = $("#outputGroup .regionParams");
				$("#outputGroup").html($("#comparisonGroup .regionParams"));
				$("#comparisonGroup").html(comparisonGroup);
				updateRegionLabels();
				var contains = $("#span_operation option[value='" + output + "_contain_" + comparison + "']");
				var contained = $("#span_operation option[value='" + comparison + "_contain_" + output + "']");
				var containsVal = contains.val();
				var containedVal = contained.val();
 				contains.val(containedVal);
				contained.val(containsVal);
				$("#span_operation").change();
				attachHandlers(); // Switching contents seems to disable the handlers, need to reattach them
			}
		}
		else {
			alert("There was an error updating the span logic form.  Please notify us using the 'Contact Us' form.");
		}
	}
	function updateRegionParams(ele){
		var button = $(ele);
		var group = button.attr('name');
		group = group.substring(group.indexOf("_")+1);
		var offsetOptions = $("#set_" + group + "Fields .offsetOptions");
		if (button.val() === 'exact') {
			$("select, input", offsetOptions).attr("disabled","true");
			$("input[name='upstream_region_" + group + "']").attr("disabled","true");
			$("input[name='downstream_region_" + group + "']").attr("disabled","true");
			$("#span_begin_" + group).val("start");
			$("#span_begin_offset_" + group).val("0");
			$("#span_end_" + group).val("stop");
			$("#span_end_offset_" + group).val("0");
		}
		else if (button.val() === 'upstream') {
			$("select, input", offsetOptions).attr("disabled","true");
			$("input[name='upstream_region_" + group + "']").removeAttr("disabled").change();
			$("input[name='downstream_region_" + group + "']").attr("disabled","true");
			$("#span_begin_" + group).val("start");
			$("#span_begin_direction_" + group).val("-");
			$("#span_end_" + group).val("start");
			$("#span_end_offset_" + group).val("0");
		}
		else if (button.val() === 'downstream') {
			$("select, input", offsetOptions).attr("disabled","true");
			$("input[name='upstream_region_" + group + "']").attr("disabled","true");
			$("input[name='downstream_region_" + group + "']").removeAttr("disabled").change();
			$("#span_begin_" + group).val("stop");
			$("#span_begin_offset_" + group).val("0");
			$("#span_end_" + group).val("stop");
			$("#span_end_direction_" + group).val("+");
		}
		else if (button.val() === 'custom') {
			$("select, input", offsetOptions).removeAttr("disabled");
			$("input[name='upstream_region_" + group + "']").attr("disabled","true");
			$("input[name='downstream_region_" + group + "']").attr("disabled","true");
		}
		else {
			// TODO: Error case
		}
		updateRegionLabels();
	}
	function updateRegionLabels() {
		var outputRegion = $("#outputGroup input[type=radio][name^='region_']:checked").val();
		$(".outputRegion").text(outputRegion + " region");
		var comparisonRegion = $("#comparisonGroup input[type=radio][name^='region_']:checked").val();
		$(".comparisonRegion").text(comparisonRegion + " region");
	}
	function prepCanvas(){
		a = new Diagram("a",document.getElementById('scale_a'));
		prepDynamicSpans(a, 0);
		b = new Diagram("b",document.getElementById('scale_b'));
		prepDynamicSpans(b, 1);
	}
	function drawRect(cxt,x1,y1,x2,y2,a,b,c){
		//cxt.fillStyle = a;
		//cxt.fillRect(x1,y1,x2,y2);
	/*	right = left = null;
		if(x1 < 0){
			x1 = 20;
			left = document.createElement("div");
			$(left).css({
				"position":"relative",
				"top":y1,
				"left":0,
				"font-size":"0px",
				"line-height":"0%",
				"width":"0px",
				"border-top": "10px solid #f6f6f6",
				"border-right": "20px solid rbga(255,0,0,1)",
				"border-bottom": "10px solid #f6f6f6"
			
			});
		}
		if(x1 + x2 > 400){
			x2 = x1 + x2 - 20;
			if(x2 > 400) x2 = 380;
			right = document.createElement("div");
			$(right).css({
				"position":"relative",
				"top":y1,
				"right":0,
				"font-size":"0px",
				"line-height":"0%",
				"width":"0px",
				"border-top": "10px solid #f6f6f6",
				"border-left": "20px solid rbga(255,0,0,1)",
				"border-bottom": "10px solid #f6f6f6"
			
			});
		}
		
	*/	
		rect = document.createElement("div");
		$(rect).css({
			"position":"relative",
			"top":y1,
			"left":x1,
			"width":x2,
			"height":y2,
			"background-color":a,
		});
		if(b){
			$(rect).css({
			/*	"border-width":"2px",
				"border-color":b,
				"border-style":"solid" */
			});
		}else{
			$(rect).css({
			/*	"border-top":"2px solid #000000",
				"border-bottom":"none"  */
			});
			if(c.right){
				$(rect).css({
				//	"border-right":"2px solid #000000"
				});
			}
			if(c.left){
				$(rect).css({
				//	"border-left":"2px solid #000000"
				});
			}
		}
		
		if(x2 > 30){
			start = document.createElement("div");
			stop = document.createElement("div");
			$(start).css({
				"display":"inline",
				"position":"relative",
				"bottom":"-12px",
				"left":"-12px"
				});
			$(stop).css({
				"display":"inline",
				"position":"relative",
				"bottom":"-12px",
				"left":(x2 - 35)
				});
			if(b){
				$(start).html("begin").css({"color":"white", "bottom":"-49px"});
				$(stop).html("end").css({"color":"white", "bottom":"-49px"});
			}else{
				$(start).html("start");
				$(stop).html("stop");
			}
			
			$(rect).append(start).append(stop);  
		}
		cxt.append(rect);
		
	//	if(left != null) cxt.append(left);
	//	if(right != null) cxt.append(right);
	}
	/*function drawLine(cxt,x1,y1,x2,y2,a, o){
		cxt.strokeStyle = a;
		cxt.lineWidth = "1";
		cxt.beginPath();
		cxt.moveTo(x1,y1);
		cxt.lineTo(x1+x2,y1+y2);
		cxt.closePath();
		cxt.stroke();
	
	}*/
    // unit: base pairs
	function prepDynamicSpans(dia, i){
		w = dia.cxt.css("width");
		h = dia.cxt.css("height");
		dia.width = parseInt(w.substring(0, w.length-2));
		dia.height = parseInt(h.substring(0, h.length-2));
		dia.center = dia.width / 2;//dia.c.width / 2;
		dia.scale = 10;
		dia.feature = new Object();
		dia.feature.length = 2000;
		dia.feature.loc = new Object();
		dia.region = new Object();
		setFeature(dia);
		drawFeature(dia);
		setRegion(dia, i);
		drawRegion(dia);
	}
	function drawFeature(dia){
		feat = dia.feature;
		cxt = dia.cxt;
		has_sides = new Object();
		has_sides.left = true;
		has_sides.right = true;
		if(feat.loc.x < 0) {feat.loc.x = 0; has_sides.left = false;}
		if(feat.loc.x + feat.width > dia.width) {feat.width = dia.width - feat.loc.x - 1;has_sides.right = false;}
		drawRect(cxt,feat.loc.x,feat.loc.y,feat.width,feat.height,"rgba(100,100,100,1.0)", false, has_sides);
		//drawFeatureText(dia);
	}
	function drawFeatureText(dia){
		dia.cxt.fillText("Feature", center - 20, dia.feature.loc.y+15);
	}
	function drawRegionText(dia){
		var i = (dia.name == "a") ? 0 : 1;
		var ba = document.getElementsByName('upstreamAnchor')[i].value;
		var bs = document.getElementsByName('upstreamSign')[i].value;
		var bo = parseInt(document.getElementsByName('upstreamOffset')[i].value);
		var ea = document.getElementsByName('downstreamAnchor')[i].value;
		var es = document.getElementsByName('downstreamSign')[i].value;
		var eo = parseInt(document.getElementsByName('downstreamOffset')[i].value);
		var vs = (ba == "Start") ? feature.loc.x : feature.loc.x + region.width;
		var ve = (ea == "Start") ? feature.loc.x : feature.loc.x + region.width;
		vs = (bs == 'plus') ? vs + (bo) : vs - (bo); 
		ve = (es == 'plus') ? ve + (eo) : ve - (eo);
		printlength = Math.abs(ve - vs);
		region = dia.region;
		scale = dia.scale;
		cxt = dia.cxt;
		t = ve + " - " + vs + " = " + printlength + "bp";
		if(t == '0bp') t = '1bp';
		if(region.start.x < region.end.x)
			cxt.fillText(t, (region.start.x + region.width / 2) - 20, region.start.y-5);
		else
			cxt.fillText(t, (region.end.x + region.width / 2) - 20, region.start.y-5);
	}
	function setFeature(dia){
		s = dia.scale;
		l = dia.feature.length;
		feature = dia.feature;
		center = dia.center;
		
		var botPad = 25;
		feature.width = l / s;
	//	feature.height = 20;
		var dx1 = center - feature.width/2;
		var dy1 = dia.height - (botPad + 10);  //feature.height);
		feature.loc.x = dx1;
		feature.loc.y = dy1;
		//if(feature.loc.x + feature.width > dia.width) feature.width = dia.width - feature.loc.x;
	}
	function drawRegion(dia){
		i = (dia.name == "a") ? 0 : 1; 
		cxt = dia.cxt;
		region = dia.region;
		drawRect(cxt,region.start.x,region.start.y,region.width,region.height,region_color[i],region_color[i]);
		//drawLine(cxt,region.start.x, region.start.y, 0, region.height, "rgba(0,0,0,1)");
		//drawLine(cxt,region.end.x, region.end.y, 0, region.height, "rgba(0,0,0,1)");
		//drawLine(cxt,region.start.x, region.start.y + region.height/2, region.width, 0, "rgba(0,0,0,1)");
		//drawRegionText(dia);
	}
	function setRegion(dia){
		i = 0;
		region = dia.region;
		feature = dia.feature;
		scale = dia.scale;
		dn = dia.name.toLowerCase();
		var ba = $("select[name*='span_begin_"+dn+"']")[i].value;//document.getElementsByName('upstreamAnchor')[i].value;
		var bs = $("select[name*='span_begin_direction_"+dn+"']")[i].value;//document.getElementsByName('upstreamSign')[i].value;
		var bo = parseInt($("input[name*='span_begin_offset_"+dn+"']")[i].value);//parseInt(document.getElementsByName('upstreamOffset')[i].value);
		var ea = $("select[name*='span_end_"+dn+"']")[i].value;//document.getElementsByName('downstreamAnchor')[i].value;
		var es = $("select[name*='span_end_direction_"+dn+"']")[i].value;//document.getElementsByName('downstreamSign')[i].value;
		var eo = parseInt($("input[name*='span_end_offset_"+dn+"']")[i].value);//parseInt(document.getElementsByName('downstreamOffset')[i].value);
		dia.singlepoint = Single(dia,ba,bs,bo,ea,es,eo);
		region.height = 3;
		region.width = feature.length / scale;
		var vs = (ba == "start") ? feature.loc.x : feature.loc.x + region.width;
		var ve = (ea == "start") ? feature.loc.x : feature.loc.x + region.width;
		vs = (bs == '+') ? vs + (bo/scale) : vs - (bo/scale); 
		ve = (es == '+') ? ve + (eo/scale) : ve - (eo/scale);
		region.width = Math.round(ve - vs);
		if(region.width < 0){
			region.width = Math.abs(region.width);
			ve = vs;
			vs = vs - region.width;
		}
		region.start = new Object();
		region.start.x = Math.round(vs);
		region.start.y = feature.loc.y - 40;
		region.end = new Object();
		region.end.x = Math.round(ve);
		region.end.y = region.start.y;
	}
	function Single(dia,ba,bs,bo,ea,es,eo){
		if(ba == ea && bs == es && bo == eo) return true;
		if(ba == ea && bo == 0 && eo == 0) return true;
		if(ba != ea && bs != es && eo == dia.feature.length / 2 && bo == dia.feature.length / 2) return true;
		if((es == 'minus' && eo == dia.feature.length && bo == 0) || (bs == 'plus' && bo == dia.feature.length && eo == 0)) return true;
		return false;
	}
	function checkMargins(dia){
		singlepoint = dia.singlepoint;
		dia.draw = false;
		
		rs_fe = Math.abs(dia.region.start.x - (dia.feature.loc.x + dia.feature.width));
		fs_re = Math.abs(dia.feature.loc.x - (dia.region.start.x + dia.region.width));
		maxWidth = Math.max(rs_fe,fs_re,Math.abs(dia.feature.width),Math.abs(dia.region.width));
		
		if(maxWidth >= dia.width){ // Zoom out
			dia.scale = dia.scale * 5;
			redraw(false,dia);
			return;
		}
		if(dia.region.start.x < 0 || dia.region.end.x < 0){ // move right
			if(dia.region.start.x < 0 ) dif = Math.abs(dia.region.start.x);
			else if(dia.region.end.x < 0 ) dif = Math.abs(dia.region.end.x);
			dia.center = dia.center + dif + 10;
			redraw(false,dia);
			return;
		}else if(dia.region.start.x > dia.width || dia.region.end.x > dia.width){ // move left
			if(dia.region.end.x > dia.width) dif = Math.abs(dia.region.end.x - dia.width);
			else if(dia.region.start.x > dia.width) dif = Math.abs(dia.region.start.x - dia.width);
			dia.center = dia.center - dif - 10;
			redraw(false,dia);
			return;
		}	
		dia.draw = true;
	}
	function redraw(fromPage, dia){
		if(dia.name == undefined){
			dia = eval("("+dia+")");
		}
		i = (dia.name == "a") ? 0 : 1;
		cxt = dia.cxt;
		center = dia.center;
		scale = dia.scale;
		feature = dia.feature;
		c = dia.c;
		if(fromPage){
			dia.center = center = dia.width / 2;
			dia.scale = scale = 10;
		}
		setFeature(dia);
		setRegion(dia);
		checkMargins(dia);
		//dia.draw = true;
		singlepoint = false;
		if(dia.draw){
			dia.cxt.html("");
			//c.width = c.width;
			drawFeature(dia);
			drawRegion(dia);
			dia.draw = false;
		}
	}
/*	function manualScale(){
		scale = parseInt(document.getElementsByName('scale')[0].value);
		redraw();
	}	
*/	
/*-------------------------------------------------------------------
 *Summary Sentence Code

var recordTypes = new Array();
var operations = ["overlaps with","contains","is contained within"];
var strands = ["either strand","the same strand","the opposite strand"];
//var sentence = null;
var sentence = "";
var op = null;
var strand = null;
var type = null;
function prepSentence(){
	recordTypes = [$("#span_a_type").val(),$("#span_b_type").val()];
	drawSentence();
}
function getIndex(n){
	var i = 0;
	var v = null;
	$("input[name*='"+n+"']").each(function(){
		if(this.checked == true) v = i;
		i++;
		return; 
	});
	return v;
}
var current_words = new Object();
function animateChanges(w){
		if(w.output != current_words.output) flashWords("output_section");
		if(w.operation != current_words.operation) flashWords("operation_section");
		if(w.comparison != current_words.comparison) flashWords("comparison_section");
		if(w.strand != current_words.strand) flashWords("strand_section");
		current_words = w;
}
function flashWords(id){
	$("b#"+id).css({
			"color":"red",
			"font-size":"15",
			"vertical-align":"top",
		}).animate({
				"color":"black",
				"font-size":"10",
		},3000);
}
function drawSentence(){
	if (sentence == null){
		sentence = "Please selected options for all parameters, then a Summary sentance will be displayed.";
	}else{
		s = "";
		op = $("#span_operation").attr("selectedIndex");
		strand = $("#span_strand").attr("selectedIndex");
		type = $("#span_output").attr("selectedIndex");
		words = new Object();
		if(type == 0){
			words.output = recordTypes[0] + "s in curent result";
			words.comparison = recordTypes[1] + "s in new step"
		}else{
			words.output = recordTypes[1] + "s in new step";
			words.comparison = recordTypes[0] + "s in current result"
		}
		if(op > 0 && type == 1){
            // operation is not overlap, and if the output is b, then we flip the operation between contains & contained by.
			op = (op == 1) ? 2 : 1;
		}
		words.operation = operations[op];
		words.strand = strands[strand];
		s = "\"Find <b id='output_section'>" + words.output + "</b> whose selected region <b id='operation_section'>" + words.operation + "</b> the <b id='comparison_section'>" + words.comparison + "&apos;s</b> selected region in <b id='strand_section'>" + words.strand + "\"</b>.";
		sentence = s;
	}

	$("div#sentence").html("<hr>" + sentence);	
	animateChanges(words);
}
 ---------------------------------------------------------------------*/
