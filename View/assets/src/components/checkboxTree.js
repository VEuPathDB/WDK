wdk.util.namespace("window.wdk.checkboxTree", function(ns, $) {
  "use strict";

  var checkboxTreeConfig;
  if ((typeof checkboxTreeConfig) == 'undefined') {
    checkboxTreeConfig = {};
  }

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
          cbt_selectListOfNodes(treeId, checkboxTree.initiallySetList);
          if (checkboxTree.collapseOnLoad) {
            cbt_collapseAll(treeId);
          }
          $('#'+treeId).find('ins.jstree-checkbox').click(checkboxTree.onchange);
          checkboxTree.configured = true;
          $('#'+treeId).show();
          checkboxTree.onload();
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

  function cbt_selectCurrentNodes(treeId) {
    cbt_selectListOfNodes(treeId, checkboxTreeConfig[treeId].currentList);
  }

  function cbt_selectDefaultNodes(treeId) {
    cbt_selectListOfNodes(treeId, checkboxTreeConfig[treeId].defaultList);
  }

  function cbt_selectListOfNodes(treeId, checkedArray) {
    var i;
    cbt_uncheckAll(treeId);
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

  function cbt_checkAll(treeId) {
    $('#' + treeId).jstree("check_all");
    // tree may have changed so call user's onchange function
    checkboxTreeConfig[treeId].onchange();
  }

  function cbt_uncheckAll(treeId) {
    $('#' + treeId).jstree("uncheck_all");
    // tree may have changed so call user's onchange function
    checkboxTreeConfig[treeId].onchange();
  }

  function cbt_expandAll(treeId) {
    $('#' + treeId).jstree("open_all", -1, true);
  }

  function cbt_collapseAll(treeId) {
    $('#' + treeId).jstree("close_all", -1, true);
  }

  ns.addTreeToPage = addTreeToPage;
  ns.configureCheckboxTree = configureCheckboxTree;
  ns.cbt_selectCurrentNodes = cbt_selectCurrentNodes;
  ns.cbt_selectDefaultNodes = cbt_selectDefaultNodes;
  ns.cbt_checkAll = cbt_checkAll;
  ns.cbt_uncheckAll = cbt_uncheckAll;
  ns.cbt_expandAll = cbt_expandAll;
  ns.cbt_collapseAll = cbt_collapseAll;

});
