import React from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';
import {isLeafNode} from '../utils/TreeUtils';
import {getLeaves} from '../utils/TreeUtils';
import {getBranches} from '../utils/TreeUtils';
import {getNodeById} from '../utils/TreeUtils';

export default class CheckboxTree extends React.Component {

  constructor(props) {
    super(props);

    // hard bind the toggle functions to the this checkbox tree component
    this.selectAll = this.selectAll.bind(this);
    this.clearAll = this.clearAll.bind(this);
    this.expandAll = this.expandAll.bind(this);
    this.collapseAll = this.collapseAll.bind(this);
    this.toCurrent = this.toCurrent.bind(this);
    this.toDefault = this.toDefault.bind(this);
    this.toggleExpansion = this.toggleExpansion.bind(this);
    this.toggleCheckbox = this.toggleCheckbox.bind(this);
  }

  componentWillMount() {
    // Use the expanded list given but apply business rules to populate the business list if no
    // expanded list is provided.
    if(this.props.selectedList === null || this.props.selectedList === undefined) {
      this.setSelectedList()
    }
    if (this.props.expandedList===null || this.props.expandedList===undefined) {
      this.setExpandedList(this.props.tree, this.props.selectedList);
    }
  }

  // Used to update a selected list
  setSelectedList(selectedList = []) {
    this.props.updateSelectedListAction(selectedList);
  }


  // Used to replace a non-existant expanded list with one obeying business rules (called recursively)
  setExpandedList(nodes, selectedList, expandedList=[]) {

    // If the selected list is empty or non-existant, the expanded list is likewise empty and there is nothing
    // more to do.
    if(!selectedList) {
      nodes.forEach(node => {

        // According to the business rule, indeterminate nodes get expanded.
        if (this.isIndeterminate(node, selectedList)) {
          expandedList.push(node.id);
        }

        // descend the tree
        this.setExpandedList(node.children, selectedList, expandedList);
      });
    }
    this.props.updateExpandedListAction(expandedList);
  }


  // selects all the tree's leaves and calls the appropriate update method in the action creator
  selectAll() {
    let selectedList = [];
    this.props.tree.forEach(node =>
      isLeafNode(node) ? selectedList.push(node.id) : selectedList.push(...getLeaves(node).map(leaf => leaf.id))
    );
    this.props.updateSelectedListAction(selectedList);
  }


  // clears the selected list and calls the appropriate update method in the action creator
  clearAll() {
    this.props.updateSelectedListAction([]);
  }


  // selects all the tree's branches and calls the appropriate update method in the action creator
  expandAll() {
    let expandedList = [];
    this.props.tree.forEach(node => {
      expandedList.push(...getBranches(node).map(branch => branch.id));
    });
    this.props.updateExpandedListAction(expandedList);
  }


  // clears the expanded list and calls the appropriate update method in the action creator
  collapseAll() {
    let expandedList = [];
    this.props.updateExpandedListAction(expandedList);
  }


  // calls the appropriate method in the action creator to reload the original selects
  toCurrent() {
    console.log("toCurrent called");
    this.props.loadCurrentSelectedListAction();
  }


  // calls the appropriate method in the action creator to load the default selects
  toDefault() {
    console.log("toDefault called");
    this.props.loadDefaultSelectedListAction();
  }


  // convey branch to be toggled to action creator - id indicates the node id to
  // be changed.
  toggleExpansion(id) {
    let newExpandedList = this.props.expandedList;

    // if the expanded list is null (unlikely, but possible) create an intial
    // expanded list that obeys business rules.
    if(newExpandedList === null || newExpandedList === undefined) {
      newExpandedList = this.setExpandedList(this.props.tree, this.props.selectedList);
    }
    let index = newExpandedList.indexOf(id);
    index <= -1 ? newExpandedList.push(id) : newExpandedList.splice(index, 1);
    this.props.updateExpandedListAction(newExpandedList);
  }


  // convey checkbox to be toggled to action creator
  // new approach:
  // if toggled checkbox is a selected leaf - add the leaf to the select list to be returned
  // if toggled checkbox is an unselected leaf - remove the leaf from the select list to be returned
  // if toggled checkbox is a selected non-leaf - identify the node's leaves (cached) and add them to the select list to be returned
  // if toggled checkbox is an unselected leaf - identify the node's leaves (cached) and remove them from the select list to be returned
  toggleCheckbox(nodeId, selected) {
    let newSelectedList = this.props.selectedList;
    let node = getNodeById(nodeId, this.props.tree);
    if(isLeafNode(node)) {
      let index =  newSelectedList.indexOf(nodeId);
      index > -1 ? newSelectedList.splice(index, 1) : newSelectedList.push(nodeId);
    }
    else {
      let leafIds = getLeaves(node).map(leafNode => leafNode.id);
      leafIds.forEach(leafId => {
        let index =  newSelectedList.indexOf(leafId);
        if(selected && index === -1) {
          newSelectedList.push(leafId);
        }
        if(!selected && index > -1) {
          newSelectedList.splice(index, 1);
        }
      });
    }

    // convey new selected list back to action creator
    this.props.updateSelectedListAction(newSelectedList);
  }



  // returns boolean indicating whether the given node is selected
  isSelected(node, selectedList) {

    // If the selected list is empty or non-existant, no check is needed.  The node is not selected.
    if(selectedList) {
      if (!isLeafNode(node)) {

        // When the node is not a leaf, it is considered selected if every one of its leaf nodes
        // is in the selected list.
        let leafIds = getLeaves(node).map(leafNode => leafNode.id);
        return leafIds.every(leafId => selectedList.indexOf(leafId) > -1);
      }
      else {
        return selectedList.indexOf(node.id) > -1;
      }
    }
    return false;
  }

  // returns boolean indicating whether the given node is indeterminate
  isIndeterminate(node, selectedList) {

    // if only some of the descendent leaf nodes are in the selected nodes list, the given
    // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
    let indeterminate = false;

    // If the selected list is empty, or non-existant no nodes are intermediate and there is nothing to do.
    if(selectedList) {
      if (!isLeafNode(node)) {
        let leafIds = getLeaves(node).map(leafNode => leafNode.id);
        let total = leafIds.reduce(function (count, leafId) {
          return selectedList.indexOf(leafId) > -1 ? count + 1 : count;
        }, 0);
        if (total > 0 && total < leafIds.length) {
          indeterminate = true;
        }
      }
    }
    return indeterminate;
  }


  // returns boolean indicating whether given node should be shown expanded or collapsed.
  isExpanded(node, expandedList, selectedList) {

    // The expandedList exists, empty or not, at this point, but if somehow it isn't
    // the business rules are used to decide whether the node is expanded or collapsed.
    // The business expand only indeterminate nodes.
    if (this.props.expandedList===null || this.props.expandedList===undefined) {
      return this.isIndeterminate(node, selectedList);
    }
    else {
      return expandedList.indexOf(node.id) > -1 ? true : false;
    }
  }


  // render the checkbox tree at the current node
  render() {
    return (
      <div className="wdk-CheckboxTree" id={this.props.name}>
        {this.renderLinks()}
        <ul className="fa-ul wdk-CheckboxTree-list" key={"list_root"}>
          {this.props.tree.map(function(node) {
            return this.renderTreeNode(node);
          }, this)}
        </ul>
        {this.renderLinks()}
      </div>
    );
  }

  renderTreeNode(node) {
    let expandedList = this.props.expandedList || this.defaultExpandedList;
    let toggleCheckbox = this.toggleCheckbox;
    let toggleExpansion = this.toggleExpansion;
    let indeterminate = this.isIndeterminate(node, this.props.selectedList);
    let selected = this.isSelected(node, this.props.selectedList);
    let expanded = this.isExpanded(node, expandedList, this.props.selectedList);
    let leaf = isLeafNode(node);
    let nodeType = !leaf && !expanded ? "wdk-CheckboxTree-collapsedItem" :
                   leaf ? "wdk-CheckboxTree-leafItem" : "wdk-CheckboxTree-expandedItem";

    return (
      <li className={nodeType} key={"item_" + node.id}>

        <AccordionButton leaf={leaf}
                         expanded={expanded}
                         id={node.id}
                         key = {"accordion_" + node.id}
                         toggleExpansion={toggleExpansion}
        />
        <IndeterminateCheckbox
          checked = {selected}
          indeterminate = {indeterminate}
          id={node.id}
          key={"checkbox_" + node.id}
          value={node.id}
          toggleCheckbox={toggleCheckbox}
        />
        <label title={node.description} key={"label_" + node.id}>{node.displayName}</label>
        {!leaf && expanded  ?
          <ul className="fa-ul wdk-CheckboxTree-list" key={"list_" + node.id}>
            {node.children.map(child => this.renderTreeNode(child))}
          </ul> : "" }
      </li>
    )
  }

  renderLinks() {
    let selectAll = this.selectAll;
    let clearAll = this.clearAll;
    let expandAll = this.expandAll;
    let collapseAll = this.collapseAll;
    let toCurrent = this.toCurrent;
    let toDefault = this.toDefault;
    return (
      <div className="wdk-CheckboxTree-links">
        <a href="#" onClick={selectAll}>select all</a> |
        <a href="#" onClick={clearAll}> clear all</a> |
        <a href="#" onClick={expandAll}> expand all</a> |
        <a href="#" onClick={collapseAll}> collapse all</a>
        <br />
        <a href="#" onClick={toCurrent}>reset to current</a> |
        <a href="#" onClick={toDefault}> reset to default</a>
      </div>
    )
  }

}