import {
  getDisplayName,
  getDescription,
  getNodeFormValue,
  getNodeChildren
} from './OntologyUtils';
import {
  isLeafNode,
  getLeaves,
  getNodeByValue
} from './TreeUtils';


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
  if(!isLeafNode(parentNode, getNodeChildren)) {
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
      searchableText.push(getDisplayName(leaf).toLowerCase());
      if(getDescription(leaf) != undefined) {
        searchableText.push(getDescription(leaf).toLowerCase());
      }
      // Add each ancestor identifier to the node list and add it's display name and description (if any) to the
      // searchable text array
      getAncestors(leaf, parentMap).forEach(ancestor => {
        nodeList.push(getNodeFormValue(ancestor));
        searchableText.push(getDisplayName(ancestor).toLowerCase());
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

