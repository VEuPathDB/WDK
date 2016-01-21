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

    // remove all invalid selected nodes from provided list (nodes must exist and be leaves only)
    this.selectedList = this.validateSelectedList(this.initialData.selected, this.initialData.tree);

    // Use the expanded list given but apply business rules to populate the business list if no
    // expanded list is provided.
    this.expandedList = this.initialData.expanded || this.setExpandedList(this.initialData.tree, this.selectedList);

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

  // Used the replace a non-existant expanded list with one obeying business rules (called recursively)
  setExpandedList(nodes, selectedList, expandedList=[]) {
    nodes.forEach(function(node) {
      let leafIds = this.getLeafIds(node);

      // if only some of the leaf nodes for the given node are selected, expand the given node
      let total = leafIds.reduce(function (count, leafId) {
        return selectedList.indexOf(leafId) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafIds.length) {
        expandedList.push(node.id);
      }
      if(!this.isLeafNode(node)) {
        this.setExpandedList(node.children, selectedList, expandedList);
      }
    },this);
    return expandedList;
  }


  // convenience method to identify nodes that are leaves
  isLeafNode(node) {
    return node.children.length === 0;
  }


  // return the leaf node ids for the given node;
  getLeafIds(node, leafIds=[]) {
    if(node.children.length > 0) {
      node.children.map(function(child) {

        // push only leaf nodes into the array
        if(child.children.length === 0) {
          leafIds.push(child.id);
        }
        this.getLeafIds(child,leafIds);
      }, this);
    }
    return leafIds;
  }

  // as a protection,remove any selected non-child nodes from the selected list (they should be there
  // in the first place)
  validateSelectedList(selectedList, nodes) {
    let validSelectedList = [];
    selectedList.forEach(function(selectedItem){
      let node = this.getNodeById(selectedItem, nodes);
      if(node != undefined && this.isLeafNode(node)) {
        validSelectedList.push(node.id);
      }
    }, this);
    return validSelectedList;
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
    let state = this.store.getState();
    let newExpandedList = state.expandedList;

    // if the expanded list is null (unlikely, but possible) create an intial
    // expanded list that obeys business rules.
    if(newExpandedList === null || newExpandedList === undefined) {
      newExpandedList = this.setExpandedList(state.tree, state.selectedList);
    }
    let index = newExpandedList.indexOf(id);
    index <= -1 ? newExpandedList.push(id) : newExpandedList.splice(index, 1);
    this.actions.toggleExpansion(newExpandedList);
  }

  // convey checkbox to be toggled to action creator
  // new approach:
  // if toggled checkbox is a selected leaf - add the leaf to the select list to be returned
  // if toggled checkbox is an unselected leaf - remove the leaf from the select list to be returned
  // if toggled checkbox is a selected non-leaf - identify the node's leaves (cached) and add them to the select list to be returned
  // if toggled checkbox is an unselected leaf - identify the node's leaves (cached) and remove them from the select list to be returned
  toggleCheckbox(nodeId, selected) {
    let state = this.store.getState();
    let newSelectedList = state.selectedList;
    let node = this.getNodeById(nodeId, state.tree);
    if(this.isLeafNode(node)) {
      let index =  newSelectedList.indexOf(nodeId);
      index > -1 ? newSelectedList.splice(index, 1) : newSelectedList.push(nodeId);
    }
    else {
      let leaves = this.getLeafIds(node);
      leaves.forEach(function(leaf) {
        let index =  newSelectedList.indexOf(leaf);
        if(selected && index === -1) {
          newSelectedList.push(leaf);
        }
        if(!selected && index > -1) {
          newSelectedList.splice(index, 1);
        }
      },this);
    }

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
                      toggleCheckbox={toggleCheckbox}
                      toggleExpansion={toggleExpansion}
        />
      </div>
    )
  }
}