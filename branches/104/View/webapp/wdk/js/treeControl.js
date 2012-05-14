//jQuery.noConflict();
$(document).ready(function(){
	var root = $(".param-tree");
	initTreeState(root);
/*	if (root.length > 0) {
		var fNode = $(".term-node:first input");
		toggleChildrenCheck(fNode);
		var children = $(".term-children").hide();
	}*/
});

function initTreeState(rootNode){
	if (rootNode.length > 0) {
		// Need to adjust parent nodes by the checked state of their children.
		// Start from the leaf nodes
		$(rootNode).find(".term-node input[type='checkbox']").each(function() {
			// skip internal nodes, which has children div
			if ($(this).parent().children(".term-children").length > 0) return;
			
			toggleChildrenCheck(this);
		});

		// expand the first branch then collapse it?? why do we want to do that?
		var a = $("a", rootNode)[0];
		expandCollapseAll(a, true);
		expandCollapseAll(a, false);

                // if there is only 1 top node, expand it by default
                var topNodes = $(rootNode).children(".term-node");
                if (topNodes.length == 1) {
                    topNodes.each(function () {
                        $(this).children("img.plus-minus").click();
                    });
                }
	}
}

function toggleChildren(ele){
	if($(ele).hasClass("plus")){
		$(ele).attr("src","images/minus.gif");
		$(ele).removeClass("plus");
		$(ele).siblings(".term-children").show();
	}else{
		$(ele).attr("src","images/plus.gif");
		$(ele).addClass("plus");
		$(ele).siblings(".term-children").hide();
	}
}

function toggleChildrenCheck(ele){
	if($(ele).attr("checked")){
		check(ele);
		checkBranch(ele);
		if($(ele).parent().children("div.term-children").length > 0 && $(ele).prev().hasClass('plus'))
			toggleChildren($(ele).prev());
	}else{
		uncheck(ele);
		checkBranch(ele);
	}
}

function checkBranch(ele){
	if(ele == undefined || $(ele).parent().parent().hasClass("param")) return;
	var any = false;
	var all = true;

   // if a node is disabled, it is partial selected, that is, some of its children are
   // selected, but not all are selected. The same state should apply to all its parents.
   if (ele.disabled) {
        any = true;
        all = false;
        // no need to check the siblings
   } else {
	// a node is not disabled, apply the check state
	if(ele.checked) 
		any = true;  // at least one node (itself) is selectd
	else
		all = false;  // at least one node (itself) is not selected

        // check siblings
	$(ele).parent().siblings("div.term-node").children("input").each(function(t){
            // avoid further processing of siblings, since the state won't change any more.
            if (any && !all) return;

            if (this.disabled) {
                // a sibling is partial selected, apply this state to the parent.
                any = true;
                all = false;
            } else {
		if(this.checked){
			any = true;
		}else{ 
			all = false;
		}
            }
	});

    }
    var checked = any;
    var disabled = (any && !all);

    // mark the state of the parent.
    $(ele).parent().parent().parent().children("input").attr("disabled", disabled).attr("checked", checked);

    // recursively check the parent
    checkBranch($(ele).parent().parent().parent().children("input")[0]);
}

function uncheck(ele){
	$(ele).attr("checked",false);
	var childDiv = $(ele).siblings(".term-children");
	if(childDiv.length > 0){
		var kids = $(childDiv).children(".term-node");
		for(var i=0;i<kids.length;i++){
			uncheck($(kids[i]).children("input")[0]);
		}
	}
}

function check(ele){
	$(ele).attr("checked",true);
	var childDiv = $(ele).siblings(".term-children");
	if(childDiv.length > 0){
		var kids = $(childDiv).children(".term-node");
		for(var i=0;i<kids.length;i++){
			check($(kids[i]).children("input")[0]);
		}
	}
}

function expandCollapseAll(ele, flag) {
    $(ele).parents(".param-tree").find(".term-node > img").each(function() {
        if ($(this).siblings(".term-children").length == 0) return;
        if ($(this).hasClass("plus")){
            if (flag) toggleChildren(this);
        } else {
            if (!flag) toggleChildren(this);
        }
    });
}


//alternative look on positioning select/clear/expand/collapse on multipick params
function expandCollapseAll2(ele, flag, name) {
//  $(ele).parent().next().children(".param-tree").find(".term-node > img").each(function() {
   $("#" + name + "aaa").children(".param-tree").find(".term-node > img").each(function() {
       	if ($(this).siblings(".term-children").length == 0) return;
       	if ($(this).hasClass("plus")){
      	    if (flag) toggleChildren(this);
        } else {
            if (!flag) toggleChildren(this);
        }
    });
}
