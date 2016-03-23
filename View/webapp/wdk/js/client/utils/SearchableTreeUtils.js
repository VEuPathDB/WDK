import {
  isLeaf,
  getLeaves,
  getNodeByValue
} from './TreeUtils';
import {
  getNodeChildren
} from './OntologyUtils';
import {
  getNodeId,
  getDisplayName,
  getDescription
} from './CategoryUtils';

/**
 * Returns all the ancestor nodes for a given node.  Climbs the parent
 * map.
 * @param node - node for which ancestors are being identified
 * @param parentMap - map linking child node identifiers to parent nodes
 * @param ancestors - growing list of ancestor nodes (initially empty)
 * @returns {Array} - list of ancestor nodes
 */
export let getAncestors = function(node, parentMap, ancestors = []) {
  if(parentMap[getNodeFormValue(node)] === undefined) {
    return ancestors;
  }
  let parent = parentMap[getNodeFormValue(node)];
  ancestors.push(parent);
  getAncestors(parent, parentMap, ancestors);
  return ancestors;
};


/**
 * Create a map of parent nodes for the given tree, keyed by child node identifiers.  Top level nodes have
 * undefined as parents.
 * @param nodes - top level nodes of the tree
 * @returns {{}} - map of node identifier by parent node
 */
export let computeParents = function(nodes) {
  let parentMap = {};
  nodes.forEach(node => {
    parentMap[getNodeFormValue(node)] = undefined;
    addParents(node, parentMap);
  });
  return parentMap;
};

/**
 * Recursive helper function for computeParents
 * @param parentNode - node to be linked to children (if any)
 * @param parentMap - map of node identifier by parent node
 */
export let addParents = function(parentNode, parentMap) {
  if(!isLeaf(parentNode, getNodeChildren)) {
    getNodeChildren(parentNode).forEach(childNode => {
      parentMap[getNodeFormValue(childNode)] = parentNode;
      addParents(childNode, parentMap);
    });
  }
};

/**
 * Creates a map of searchable text for each node in the tree, the key is the node
 * identifier.  The searchable text is drawn from the display name and description (if any) of the
 * node and its descendants.
 * @param nodes - top level nodes of the tree
 * @returns {{}} - map of node identifier by search text string (all lower case)
 */
export let createSearchableTextMap = function(nodes) {
  let searchableTextMap = {};
  // Start with a map that relates a node identifier to its parent node
  let parentMap = computeParents(nodes);
  nodes.forEach(node => {
    let leaves = getLeaves(node, getNodeChildren);
    leaves.forEach(leaf => {
      // Will contain a list of identifiers for the leaf node and all of its ancestors
      let nodeList = [];
      nodeList.push(getNodeFormValue(leaf));
      // Start an array of searchable text, beginning with the leaf's display name and description (if any)
      let searchableText = [];
      if (getDisplayName(leaf) != undefined) searchableText.push(getDisplayName(leaf).toLowerCase());
      if(getDescription(leaf) != undefined) {
        searchableText.push(getDescription(leaf).toLowerCase());
      }
      // Add each ancestor identifier to the node list and add it's display name and description (if any) to the
      // searchable text array
      getAncestors(leaf, parentMap).forEach(ancestor => {
        nodeList.push(getNodeFormValue(ancestor));
        if (getDisplayName(leaf) != undefined) searchableText.push(getDisplayName(ancestor).toLowerCase());
        if(getDescription(ancestor) != undefined) {
          searchableText.push(getDescription(ancestor).toLowerCase());
        }
      });
      // Add a concatenated string version of the searchable text to each item in the node list.  In the case of an
      // ancestor, the node may already exist in the map so the new searchable text must simply be added.
      nodeList.forEach(item => {
        searchableTextMap[item] = searchableTextMap[item] ? searchableTextMap[item] + " " + searchableText.join(" ") : searchableText.join(" ");
      });
    });
  });
  return searchableTextMap;
};

/**
 * Returns boolean indicating whether the given node is indeterminate
 */
let isIndeterminate = function(node, selectedList, getNodeFormValue, getNodeChildren) {

  // if only some of the descendent leaf nodes are in the selected nodes list, the given
  // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
  let indeterminate = false;

  // If the selected list is empty, or non-existant no nodes are intermediate and there is nothing to do.
  if (selectedList) {
    if (!isLeaf(node, getNodeChildren)) {
      let leafNodes = getLeaves(node, getNodeChildren);
      let total = leafNodes.reduce((count, leafNode) => {
        return selectedList.indexOf(getNodeFormValue(leafNode)) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafNodes.length) {
        indeterminate = true;
      }
    }
  }
  return indeterminate;
}


/**
 * Returns a list of nodes that should be expanded based on currently selected
 * nodes.  If a branch's children are partially selected (i.e. the branch is
 * indeterminant), then it is added to the expanded list.  If a branch is
 * completely selected or completely unselected, it is not added.
 */
export let getExpandedList = function(nodes, getNodeFormValue, getNodeChildren, selectedList, expandedList = []) {

  // If the selected list is empty or non-existant, the expanded list is likewise empty and there is nothing
  // more to do.
  if (selectedList && selectedList.length > 0) {
    nodes.forEach(node => {

      // According to the business rule, indeterminate nodes get expanded.
      if (isIndeterminate(node, selectedList, getNodeFormValue, getNodeChildren)) {
        expandedList.push(getNodeFormValue(node));
      }
      // descend the tree
      getExpandedList(getNodeChildren(node), getNodeFormValue, getNodeChildren, selectedList, expandedList);
    });
  }
  return expandedList;
}

function createSearchableTextMap(nodes) {
  let searchableTextMap = {};
  let parentMap = this.computeParents(nodes);
  nodes.forEach(node => {
    let leaves = getLeaves(node, getNodeChildren);
    leaves.forEach(leaf => {
      let nodeList = [];
      nodeList.push(getNodeFormValue(leaf));
      let searchableText = [];
      searchableText.push(getDisplayName(leaf).toLowerCase());
      if(getDescription(leaf) != undefined) {
        searchableText.push(getDescription(leaf).toLowerCase());
      }
      this.getAncestors(leaf, parentMap).forEach(ancestor => {
        nodeList.push(getNodeFormValue(ancestor));
        searchableText.push(getDisplayName(ancestor).toLowerCase());
        if(getDescription(ancestor) != undefined) {
          searchableText.push(getDescription(ancestor).toLowerCase());
        }
      });
      nodeList.forEach(item => {
        searchableTextMap[item] = searchableTextMap[item] ? searchableTextMap[item] + " " + searchableText.join(" ") : searchableText.join(" ");
      });
    });
  });
  return searchableTextMap;
}


function getAncestors(node, parentMap, ancestors = []) {
  if(parentMap[getNodeFormValue(node)] === undefined) {
    return ancestors;
  }
  let parent = parentMap[getNodeFormValue(node)];
  ancestors.push(parent);
  this.getAncestors(parent, parentMap, ancestors);
  return ancestors;
}


function computeParents(nodes) {
  let parentMap = {};
  nodes.forEach(node => {
    parentMap[getNodeFormValue(node)] = undefined;
    this.addParents(node, parentMap);
  });
  return parentMap;
}


function addParents(parentNode, parentMap) {
  if(!isLeaf(parentNode, getNodeChildren)) {
    getNodeChildren(parentNode).forEach(childNode => {
      parentMap[getNodeFormValue(childNode)] = parentNode;
      this.addParents(childNode, parentMap);
    });
  }
}


/**
 * Returns boolean indicating whether the given node is selected
 */
let isSelected = function(node, selectedList, getNodeFormValue, getNodeChildren) {

  // If the selected list is empty or non-existant, no check is needed.  The node is not selected.
  if (selectedList) {
    if (!isLeaf(node, getNodeChildren)) {

      // When the node is not a leaf, it is considered selected if every one of its leaf nodes
      // is in the selected list.
      let leafNodes = getLeaves(node, getNodeChildren);
      return leafNodes.every(leafNode => selectedList.indexOf(getNodeFormValue(leafNode)) > -1);
    }
    else {
      return selectedList.indexOf(getNodeFormValue(node)) > -1;
    }
  }
  return false;
};


/**
 * Returns boolean indicating whether given node should be shown expanded or collapsed.
 */
let isExpanded = function(node, expandedList, getNodeFormValue) {
  return expandedList == null ? false : expandedList.indexOf(getNodeFormValue(node)) > -1;
};

/**
 * Return boolean indicating whether node matches the search parameter as defined
 * by the included search predicate.  If no search is in progress, the function returns true.
 * @param node
 * @param isSearchMode
 * @param onSearch - fn - search predicate which takes the node and returns true for a match and false otherwise
 */
let isMatching = function(node, isSearchMode, onSearch) {
  return isSearchMode ? onSearch(node) : true;
};


/**
 * Return boolean indicating whether given node's children should be visible.  This is governed
 * by whether the node is expanded but is overriden by the isMatching fn when a search is in progress.
 * @param node
 * @param expandedList
 * @returns {*}
 */
let isVisible = function(node, expandedList, isSearchMode, onSearch, getNodeFormValue) {
  return isSearchMode ? onSearch(node) : CheckboxTree.isExpanded(node, expandedList, getNodeFormValue);
};


/**
 * Used to replace a non-existant expanded list with one obeying business rules (called recursively).
 * Invokes action callback for updating the new expanded list.
 */
let setExpandedList = function(nodes, getNodeFormValue, getNodeChildren, selectedList, expandedList = [])  {

  // If the selected list is empty or non-existant, the expanded list is likewise empty and there is nothing
  // more to do.
  if (selectedList && selectedList.length > 0) {
    nodes.forEach(node => {

      // According to the business rule, indeterminate nodes get expanded.
      if (this.isIndeterminate(node, selectedList, getNodeFormValue, getNodeChildren)) {
        expandedList.push(getNodeFormValue(node));
      }
      // descend the tree
      CheckboxTree.setExpandedList(getNodeChildren(node), selectedList, expandedList);
    });
  }
  return expandedList;
};