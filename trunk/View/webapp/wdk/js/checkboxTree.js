
var checkboxTreeConfig;
if (typeof checkboxTreeConfig == 'undefined') {
	checkboxTreeConfig = new Object();
}

// takes string, boolean, url, "current" array, "default" array, "initially set" array
function addTreeToPage(id, checkboxName, useIcons, leafImgUrl, currentList, defaultList, initiallySetList) {
	var checkboxTree = checkboxTreeConfig[id];
	if (typeof checkboxTree == 'undefined') {
		checkboxTree = new Object();
		checkboxTreeConfig[id] = checkboxTree;
	}
	checkboxTree.id = id;
	checkboxTree.checkboxName = checkboxName;
	checkboxTree.useIcons = useIcons;
	checkboxTree.leafImgUrl = leafImgUrl;
	checkboxTree.currentList = currentList;
	checkboxTree.defaultList = defaultList;
	checkboxTree.initiallySetList = initiallySetList;
}

function configureCheckboxTree(treeId) {
	var checkboxTree = checkboxTreeConfig[treeId];
	$('#'+treeId)
		.bind("loaded.jstree", function (event, data) {
			// need to check all selected nodes, but wait to ensure page is ready
			cbt_selectListOfNodes(treeId, checkboxTree.initiallySetList);
		})
		.jstree({
			"plugins" : [ "html_data", "themes", "types", "checkbox" ],
			"core" : { "initially_open" : [ "root" ] },
			"themes" : { "theme" : "classic", "icons" : checkboxTree.useIcons },
			"types" : { "types" : { "leaf" : { "icon" : { "image" : checkboxTree.leafImgUrl }}}},
			"checkbox" : {
				"real_checkboxes" : true,
				"real_checkboxes_names" : function(node) { return [checkboxTree.checkboxName, (node[0].id || "")]; }
			}
		});
}

function cbt_selectCurrentNodes(treeId) {
	cbt_selectListOfNodes(treeId, checkboxTreeConfig[treeId].currentList);
}

function cbt_selectDefaultNodes(treeId) {
	cbt_selectListOfNodes(treeId, checkboxTreeConfig[treeId].defaultList);
}

function cbt_selectListOfNodes(treeId, checkedArray) {
	cbt_uncheckAll(treeId);
	// Have to manually select nodes and compare IDs since our ID names are not jquery-selection friendly
	// Ideally would be able to do the following for each item in the checked array:
	//   $('.checkboxTree').jstree("check_node", '#'+checkedArray[i];);
	for (i = 0; i < checkedArray.length; i++) {
		$('#' + treeId + ' .jstree-leaf').each(function(index) {
			if (this.id == checkedArray[i]) {
				$('#' + treeId).jstree("check_node", $(this));
			}
		})
	}
}

function cbt_checkAll(treeId) {
	$('#' + treeId).jstree("check_all");
}

function cbt_uncheckAll(treeId) {
	$('#' + treeId).jstree("uncheck_all");
}

function cbt_expandAll(treeId) {
	$('#' + treeId).jstree("open_all", -1, true);
}

function cbt_collapseAll(treeId) {
	$('#' + treeId).jstree("close_all", -1, true);
}
