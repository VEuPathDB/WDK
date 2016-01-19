import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTree from './CheckboxTree.js';

// serves as view controller for checkbox tree React component
export default class CheckboxTreeController extends React.Component {

  constructor(props) {
    super(props);
    this.initialData = props.initialData;
    this.store = props.store;
    this.actions = props.actions;
    this.state = this.store.getState();
    // hard bind the toggle functions to the view controller
    this.toggleCheckbox = this.toggleCheckbox.bind(this);
    this.toggleExpansion = this.toggleExpansion.bind(this);
  }

  // set up store listener and load up a properly initialized checkbox tree
  componentWillMount() {
    this.storeSubscription = this.store.addListener(function () {
      this.setState(this.store.getState());
    }.bind(this));

    // create a cache to simply look up of a node's parent.
    this.parentMap = this.computeParents(this.initialData);

    // A null selected list should be re-cast as an empty array.
    this.selectedList = this.initialData.selected || [];

    // A null expanded list should be re-initialized based upon business rules
    this.expandedList = this.initialData.expanded || this.computeExpanded(this.initialData, this.selectedList);


    this.indeterminateList = this.computeIndeterminateStates(this.initialData.tree, this.initialData.selected, []);
    this.actions.loadCheckboxTree({"name":this.initialData.name,
                                   "tree": this.initialData.tree,
                                   "selected": this.selectedList,
                                   "expanded": this.expandedList
    });
  }

  // remove the store listener upon unmounting the component.
  componentWillUnmount() {
    this.storeSubscription.remove();
  }

  // determine expansion according to business rules when no expanded list is provided (i.e., expanded list is null).
  // only nodes whose leaves are partially selected are expanded.  This possibility is only expected on initial mount of
  // the component.
  computeExpanded(initialData, selectedList) {
    return this.getExpanded(initialData.tree, selectedList);
  }


  // return all the nodes to be expanded upon initial load (called recursively)
  getExpanded(nodes, selectedList, expandedList = []) {
    nodes.forEach(function(node) {
      let leafNodes = this.getDescendentNodes(node, [], true);

      // if only some of the descendent leaf nodes for the given node are selected, expand the given node
      let total = leafNodes.reduce(function (count, leafNode) {
        return selectedList.indexOf(leafNode.id) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafNodes.length) {
        expandedList.push(node.id);
      }
      if (node.children.length > 0) {
        expandedList = this.getExpanded(node.children, selectedList, expandedList);
      }
    }, this);
    return expandedList;
  }


  // provides cache map of parent for each node (done when the checkbox tree component is mounted
  // to speed identification of ascendent nodes)
  computeParents(initialData) {
    let nodes = initialData.tree;
    let parentMap = {};

    // top level nodes have no parent so their parent is set to undefined.  Then continue to
    // descend down the subtree identifying all other parent-child combinations.
    nodes.forEach(function(node) {
      parentMap[node.id] = undefined;
      this.addParents(node, parentMap);
    },this);
    return parentMap;
  }


  // adds parents to the parent map (called recursively)
  addParents(parentNode, parentMap) {

    // If the parent node has no children, this branch is exhausted.
    if(parentNode.children.length > 0) {
      parentNode.children.map(function(childNode) {
        parentMap[childNode.id] = parentNode.id;
        this.addParents(childNode, parentMap);
      }, this);
    }
  }

  // identifies a given node as selected or not
  computeAscendentNodes(node, selectedList, results = []) {
    let parentId = this.parentMap[node.id];
    if(parentId !== undefined) {
      let parentNode = this.getNodeById(parentId, this.initialData.tree);
      let result = {"id": parentNode.id, "checked" : false};
      let leafNodes = this.getDescendentNodes(parentNode, [], true);
      let total = leafNodes.reduce(function (count, leafNode) {
        return selectedList.indexOf(leafNode.id) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total == leafNodes.length) {
          result.checked = true;
      }
      results.push(result);
      results = this.computeAscendentNodes(parentNode, selectedList, results);
    }
    return results;
  }


  // populates the indeterminate list with all indeterminate nodes (for initial pass when mounting
  // a checkbox tree component - called recursively)
  computeIndeterminateStates(nodes, selectedList, indeterminateList = []) {
    nodes.forEach(function(node){
      if(this.getIndeterminateState(node, selectedList)) {
        indeterminateList.push(node.id);
        if(node.children.length > 0) {
          this.computeIndeterminateStates(node.children, selectedList, indeterminateList);
        }
      }
    },this);
    return indeterminateList;
  }


  // discovers whether the given node is indeterminate
  getIndeterminateState(node, selectedList) {
    let leafNodes = this.getDescendentNodes(node, [], true);

    // if only some of the descendent leaf nodes are in the selected nodes list, the given
    // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
    let indeterminate = false;
    if (leafNodes.length > 0) {
      let total = leafNodes.reduce(function (count, leafNode) {
        return selectedList.indexOf(leafNode.id) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafNodes.length) {
        indeterminate = true;
      }
    }
    return indeterminate;
  }


  // returns those descendent nodes of the given node with checked state modified to reflect state of given node (auto-fill : true)
  computeDescendentNodes(node, selectedList) {

    // boolean - is given node selected or not?
    let selected = selectedList.indexOf(node.id) > -1;

    // get all descendent nodes and render each checked state to be identical to that of the given node.
    let descendentNodes = this.getDescendentNodes(node, []);
    descendentNodes.forEach(function(descendentNode){
      descendentNode.checked = selected;
    });
    return descendentNodes;
  }


  // returns all the descendent nodes of the given node (called recursively)
  // the leavesOnly argument, when true, returns only descendent leaf nodes.  defaults to false.
  getDescendentNodes(node, descendentNodes, leavesOnly = false) {
    if(node.children.length > 0) {
      node.children.map(function(child) {

        // push only leaf nodes into the array if the leaves only flag is set
        // otherwise push every descendent node into the array
        if(!leavesOnly || (leavesOnly && child.children.length === 0)) {
          descendentNodes.push(child);
        }
        this.getDescendentNodes(child, descendentNodes, leavesOnly);
      }, this);
    }
    return descendentNodes;
  }


  // descend the tree to find the node associated with the node id given (called recursively)
  getNodeById(nodeId, nodes) {
    for(let i = 0; i < nodes.length; i++) {
      let node = undefined;
      if(nodes[i].id === nodeId) {
        return nodes[i];
      }
      if(nodes[i].children.length > 0) {
        node = this.getNodeById(nodeId, nodes[i].children);
        if(node !== undefined) {
          return node;
        }
      }
    }
    return undefined;
  }

  // convey branch to be toggled to action creator - id indicates the node id to
  // be changed.
  toggleExpansion(id) {
    let newExpandedList = this.store.getState().expandedList;
    let index = newExpandedList.indexOf(id);
    index <= -1 ? newExpandedList.push(id) : newExpandedList.splice(index,1);
    this.actions.toggleExpansion(newExpandedList);
  }

  // convey checkbox to be toggled to action creator
  toggleCheckbox(nodeId) {
    let state = this.store.getState();
    let newSelectedList = state.selectedList;
    let computedList;

    // add or subtract the altered node to/from the selected list as appropriate
    let alteredNode = this.getNodeById(nodeId, state.tree);
    let index =  newSelectedList.indexOf(nodeId);
    index > -1 ? newSelectedList.splice(index, 1) : newSelectedList.push(nodeId);

    // add or subtract descendent nodes to/from the selected list as appropriate
    computedList = this.computeDescendentNodes(alteredNode, newSelectedList);
    computedList.forEach(function(node) {
      let index = newSelectedList.indexOf(node.id);
      if(node.checked && index <= -1) {
        newSelectedList.push(node.id);
      }
      if(!node.checked && index > -1) {
        newSelectedList.splice(index,1);
      }
    });

    // add or subtract ascendent nodes to/from the selected list as appropriate (note that
    // indeterminate nodes are considered unselected and so not added to the selected list).
    computedList = this.computeAscendentNodes(alteredNode, newSelectedList);
    computedList.forEach(function(node) {
      let index = newSelectedList.indexOf(node.id);
      if(node.checked && index <= -1) {
        newSelectedList.push(node.id);
      }
      if(!node.checked && index > -1) {
        newSelectedList.splice(index,1);
      }
    },this);

    // compute the list of indeterminate nodes
    this.indeterminateList = this.computeIndeterminateStates(state.tree, newSelectedList, []);

    // convey new selected list back to action creator
    this.actions.toggleCheckbox(newSelectedList);
  }

  // render wraps the top-level component, passing latest state
  render() {
    let store = this.store;
    let data = store.getState();
    let toggleCheckbox = this.toggleCheckbox;
    let toggleExpansion = this.toggleExpansion;
    return (
      <div className="wdk-CheckboxTree" data-name={this.props.name}>
        <CheckboxTree tree={data.tree}
                      selectedList={data.selectedList}
                      expandedList={data.expandedList}
                      indeterminateList={this.indeterminateList}
                      store={store}
                      toggleCheckbox={toggleCheckbox}
                      toggleExpansion={toggleExpansion}
        />
      </div>
    )
  }
}