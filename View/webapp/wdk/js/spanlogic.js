/*	var c = null;
	var cxt = null;
	var scale = null; // Scale is an integer for number of nucleotides per 1px.
	var feature = null; // deafult length of the feature.
	var region = null;
	var center = null;
	var draw = false;
	var singlepoint = false;
*/	
	var A = null;
	var B = null;
	var region_color = ["rgba(255,0,0,0.3)","rgba(0,0,255,0.3)"];
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
		attachHandlers();
		prepCanvas();
		prepSentence();
//		prepDynamicSpans();
	}
	function attachHandlers(){
		$("select[name*='_a'], input[name*='_a']").change(function(){
			redraw(true,"A");
		});
		$("select[name*='_b'], input[name*='_b']").change(function(){
			redraw(true,"B");
		});
		$("input[type*='radio']").click(function(){
			drawSentence();
		});
	}
	function prepCanvas(){
		A = new Diagram("A",document.getElementById('scaleA'));
		prepDynamicSpans(A, 0);
		B = new Diagram("B",document.getElementById('scaleB'));
		prepDynamicSpans(B, 1);
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
				"border-width":"2px",
				"border-color":b,
				"border-style":"solid"
			});
		}else{
			$(rect).css({
				"border-top":"2px solid #000000",
				"border-bottom":"none"
			});
			if(c.right){
				$(rect).css({
					"border-right":"2px solid #000000"
				});
			}
			if(c.left){
				$(rect).css({
					"border-left":"2px solid #000000"
				});
			}
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
		drawRect(cxt,feat.loc.x,feat.loc.y,feat.width,feat.height,"rgba(255,255,255,1.0)", false, has_sides);
		//drawFeatureText(dia);
	}
	function drawFeatureText(dia){
		dia.cxt.fillText("Feature", center - 20, dia.feature.loc.y+15);
	}
	function drawRegionText(dia){
		var i = (dia.name == "A") ? 0 : 1;
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
		feature.height = 20;
		var dx1 = center - feature.width/2;
		var dy1 = dia.height - (botPad + feature.height);
		feature.loc.x = dx1;
		feature.loc.y = dy1;
		//if(feature.loc.x + feature.width > dia.width) feature.width = dia.width - feature.loc.x;
	}
	function drawRegion(dia){
		i = (dia.name == "A") ? 0 : 1; 
		cxt = dia.cxt;
		region = dia.region;
		drawRect(cxt,region.start.x,region.start.y,region.width,region.height,region_color[i],region_color[i]);
		//drawLine(cxt,region.start.x, region.start.y, 0, region.height, "rgba(0,0,0,1)");
		//drawLine(cxt,region.end.x, region.end.y, 0, region.height, "rgba(0,0,0,1)");
		//drawLine(cxt,region.start.x, region.start.y + region.height/2, region.width, 0, "rgba(0,0,0,1)");
		//drawRegionText(dia);
	}
	function setRegion(dia,i){
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
		region.height = 45;
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
		i = (dia.name == "A") ? 0 : 1;
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
		setRegion(dia, i);
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
 ---------------------------------------------------------------------*/

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
function drawSentence(){
	if (sentence == null){
		sentence = "Please selected options for all parameters, then a Summary sentance will be displayed.";
	}else{
		s = "";
		op = getIndex("span_operation");
		strand = getIndex("span_strand");
		type = getIndex("span_output");
		typeA = null;
		typeB = null;
		if(type == 0){
			typeA = recordTypes[0] + "s in set A";
			typeB = recordTypes[1] + "s in set B"
		}else{
			typeA = recordTypes[1] + "s in set B";
			typeB = recordTypes[0] + "s in set A"
		}
		if(op > 0 && type == 1){
			op = (op == 1) ? 2 : 1;
		}
		s = "Find <b>" + typeA + "</b> whose selected interval <b>" + operations[op] + "</b> the <b>" + typeB + "&apos;s</b> selected interval in <b>" + strands[strand] + "</b>.";
		sentence = s;
	}
	$("div#sentence").html(sentence);
}


	