/* jshint evil:true */
wdk.util.namespace("window.wdk.checkboxTree", function(ns, $) {
  "use strict";

  var escapeSelectorComponent = wdk.util.escapeSelectorComponent;
  var checkboxTreeConfig = {};

  function setUpCheckboxTree($elem, $attrs) {

    var id = $attrs.id;
    var checkboxTree = checkboxTreeConfig[id];
    if ((typeof checkboxTree) == 'undefined') {
      checkboxTree = {};
      checkboxTreeConfig[id] = checkboxTree;
    }

    checkboxTree.id = id;
    checkboxTree.checkboxName = $attrs.name;
    checkboxTree.useIcons = $attrs.useicons;
    checkboxTree.collapseOnLoad = $attrs.isallselected;
    checkboxTree.leafImgUrl = wdk.assetsUrl($attrs.leafimage);
    checkboxTree.currentList = parseJsonArray($attrs.selectednodes);
    checkboxTree.defaultList = parseJsonArray($attrs.defaultnodes);
    checkboxTree.initiallySetList = parseJsonArray($attrs.initialnodes);
    checkboxTree.onchange = new Function($attrs.onchange);
    checkboxTree.onload = new Function($attrs.onload);
    checkboxTree.configured = false;

    configureCheckboxTree(id);
  }

  // could be an array already, or could be a string trying to be a JSON array
  function parseJsonArray(jsArray) {
    return (typeof jsArray === "object" ? jsArray :
        jQuery.parseJSON(jsArray.replace(/'/g, '"')));
  }

  function configureCheckboxTree(treeId) {
    var checkboxTree = checkboxTreeConfig[treeId];
    if (!checkboxTree.configured) {
      $('#'+treeId)
        .bind("loaded.jstree", function () {
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
        .bind("change_state.jstree", function() {
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
    if (treeConfig === undefined) return false;
    return treeConfig.configured;
  }

  function selectCurrentNodes(treeId) {
    selectListOfNodes(treeId, checkboxTreeConfig[treeId].currentList, 'current');
  }

  function selectDefaultNodes(treeId) {
    selectListOfNodes(treeId, checkboxTreeConfig[treeId].defaultList, 'default');
  }

  function selectListOfNodes(treeId, checkedArray, reason) {
    // jshint loopfunc:true
    var i;
    var $tree = $('#' + treeId);
    uncheckAll(treeId);

    for (var id of checkedArray) {
      // Our ID names are not jquery-selection friendly. Our ID names are also
      // not unique, so we need to scope our search to the tree.
      var node = $tree.find('#' + escapeSelectorComponent(id));
      $tree.jstree('check_node', node);
    }

    // tree may have changed so call user's onchange function
    checkboxTreeConfig[treeId].onchange();

    // trigger event with mo
    $tree.trigger('change', [ reason ]);
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
