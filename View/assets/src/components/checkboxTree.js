wdk.util.namespace("window.wdk.checkboxTree", function(ns, $) {
  "use strict";

  var checkboxTreeConfig;
  if ((typeof checkboxTreeConfig) == 'undefined') {
    checkboxTreeConfig = {};
  }
  
  function setUpCheckboxTree($elem, $attrs) {
    var id = $attrs.id;
    var leafUrl = wdk.assetsUrl($attrs.leafimage);
    var selectedNodes = parseJsonArray($attrs.selectednodes);
    var defaultNodes = parseJsonArray($attrs.defaultnodes);
    var initialNodes = parseJsonArray($attrs.initialnodes);
    var onchangeFunction = new Function($attrs.onchange);
    var onloadFunction = new Function($attrs.onload);

    addTreeToPage(id, $attrs.name, $attrs.useicons, $attrs.isallselected, leafUrl,
        selectedNodes, defaultNodes, initialNodes, onchangeFunction, onloadFunction);

    configureCheckboxTree(id);

    //id="${id}" data-name="${checkboxName}" data-useicons="${useIcons}"
    //    data-isallselected="${rootNode.isAllSelected}" data-leafimage="${leafImage}"
    //    data-selectednodes="[${rootNode.selectedAsList}]", data-defaultnodes="[${rootNode.defaultAsList}]"
    //    data-initialnodes="[${initiallySetList}]" data-onchange="function"
    //    data-onload="function"
  }
  
  // could be an array already, or could be a string trying to be a JSON array
  function parseJsonArray(jsArray) {
    return (typeof jsArray === "object" ? jsArray :
        jQuery.parseJSON(jsArray.replace("'", '"', "g")));
  }
  
  
  //wdk.checkboxTree.addTreeToPage("${id}", "${checkboxName}", ${useIcons}, 
  //    ${rootNode.isAllSelected}, wdk.assetsUrl('${leafImage}'), 
  //    [${rootNode.selectedAsList}], [${rootNode.defaultAsList}], [${initiallySetList}],
  //    function(){ setTimeout(function() { ${onchange}; }, 0); }, function(){ ${onload}; });
  
  // takes string, boolean, url, "current" array, "default" array, "initially set" array
  function addTreeToPage(id, checkboxName, useIcons, collapseOnLoad, leafImgUrl,
      currentList, defaultList, initiallySetList, onchange, onload) {
    var checkboxTree = checkboxTreeConfig[id];
    if ((typeof checkboxTree) == 'undefined') {
      checkboxTree = {};
      checkboxTreeConfig[id] = checkboxTree;
    }
    checkboxTree.id = id;
    checkboxTree.checkboxName = checkboxName;
    checkboxTree.useIcons = useIcons;
    checkboxTree.collapseOnLoad = collapseOnLoad;
    checkboxTree.leafImgUrl = leafImgUrl;
    checkboxTree.currentList = currentList;
    checkboxTree.defaultList = defaultList;
    checkboxTree.initiallySetList = initiallySetList;
    checkboxTree.onchange = onchange;
    checkboxTree.onload = onload;
    checkboxTree.configured = false;
  }

  function configureCheckboxTrees() {
    $(".checkbox-tree").each(function () {
      configureCheckboxTree(this.id);
    });
  }

  function configureCheckboxTree(treeId) {
    var checkboxTree = checkboxTreeConfig[treeId];
    if (!checkboxTree.configured) {
      $('#'+treeId)
        .bind("loaded.jstree", function (event, data) {
          // need to check all selected nodes, but wait to ensure page is ready
          selectListOfNodes(treeId, checkboxTree.initiallySetList);
          if (checkboxTree.collapseOnLoad) {
            collapseAll(treeId);
          }
          $('#'+treeId).find('ins.jstree-checkbox').click(checkboxTree.onchange);
          $('#'+treeId).show();
          checkboxTree.onload();
          checkboxTree.configured = true;
        })
        // hack to bubble change event up to containing form
        .bind("change_state.jstree", function(event, data) {
          $(this).trigger("change");
        })
        .jstree({
          "plugins" : [ "html_data", "themes", "types", "checkbox" ],
          "core" : { "initially_open" : [ "root" ] },
          "themes" : { "theme" : "classic", "icons" : checkboxTree.useIcons },
          "types" : { "types" : { "leaf" : { "icon" : { "image" : checkboxTree.leafImgUrl }}}},
          "checkbox" : {
            "two_state" : false,
            "real_checkboxes" : true,
            "real_checkboxes_names" : function(node) {
              return [checkboxTree.checkboxName, (node[0].id || "")];
            }
          }
        });
    }
  }

  function getInputName(treeId) {
    return checkboxTreeConfig[treeId].checkboxName;
  }

  function isConfigured(treeId) {
    var treeConfig = checkboxTreeConfig[treeId];
    if (treeConfig == null) return false;
    return treeConfig.configured;
  }

  function selectCurrentNodes(treeId) {
    selectListOfNodes(treeId, checkboxTreeConfig[treeId].currentList);
  }

  function selectDefaultNodes(treeId) {
    selectListOfNodes(treeId, checkboxTreeConfig[treeId].defaultList);
  }

  function selectListOfNodes(treeId, checkedArray) {
    var i;
    uncheckAll(treeId);
    // Why aren't we using valid IDs???
    // Have to manually select nodes and compare IDs since our ID names are not jquery-selection friendly
    // Ideally would be able to do the following for each item in the checked array:
    //   $('.checkboxTree').jstree("check_node", '#'+checkedArray[i];);
    for (i = 0; i < checkedArray.length; i++) {
      $('#' + treeId + ' .jstree-leaf').each(function(index) {
        if (this.id == checkedArray[i]) {
          $('#' + treeId).jstree("check_node", $(this));
        }
      });
    }
    // tree may have changed so call user's onchange function
    checkboxTreeConfig[treeId].onchange();
  }

  function checkAll(treeId) {
    $('#' + treeId).jstree("check_all");
    // tree may have changed so call user's onchange function
    checkboxTreeConfig[treeId].onchange();
  }

  function uncheckAll(treeId) {
    $('#' + treeId).jstree("uncheck_all");
    // tree may have changed so call user's onchange function
    checkboxTreeConfig[treeId].onchange();
  }

  function expandAll(treeId) {
    $('#' + treeId).jstree("open_all", -1, true);
  }

  function collapseAll(treeId) {
    $('#' + treeId).jstree("close_all", -1, true);
  }

  ns.setUpCheckboxTree = setUpCheckboxTree;
  ns.addTreeToPage = addTreeToPage;
  ns.configureCheckboxTree = configureCheckboxTree;
  ns.getInputName = getInputName;
  ns.isConfigured = isConfigured;
  ns.selectCurrentNodes = selectCurrentNodes;
  ns.selectDefaultNodes = selectDefaultNodes;
  ns.selectListOfNodes = selectListOfNodes;
  ns.checkAll = checkAll;
  ns.uncheckAll = uncheckAll;
  ns.expandAll = expandAll;
  ns.collapseAll = collapseAll;

});
