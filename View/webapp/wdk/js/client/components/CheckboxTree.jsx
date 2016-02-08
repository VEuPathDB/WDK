import React,{PropTypes} from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';
import {isLeafNode} from '../utils/TreeUtils';
import {getLeaves} from '../utils/TreeUtils';
import {getBranches} from '../utils/TreeUtils';
import {getNodeById} from '../utils/TreeUtils';

/**
 * A null or undefined selected list should be made into an empty array
 * @type {Array}
 */
//CheckboxTree.selectedList = [];

export default class CheckboxTree extends React.Component {

  /**
   * Hards binds all the user interaction methods to this object.
   * @param props - component properties
   */
  constructor(props) {
    super(props);
    this.newSelectedList = this.props.selectedList;
    this.newExpandedList = this.props.expandedList;

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

  /**
   * Insure that null selectedList or expandedList are replaced with appropriate defaults.
   */
  componentWillMount() {
    // Use the expanded list given but apply business rules to populate the business list if no
    // expanded list is provided.
    if (this.props.expandedList === null || this.props.expandedList === undefined) {
      this.setExpandedList(this.props.tree, this.props.selectedList);
    }
  }

  /**
   * Once initial rendering is complete, return any changes made to the selectedList or expandedList
   * to the controller.
   */
  componentDidMount() {
    this.props.onExpandedListUpdated(this.newExpandedList);
  }

  /**
   *  Used to update a selected list
   *  Invokes action callback for updating the new selected list.
   */
  setSelectedList(selectedList = []) {
    this.props.onSelectedListUpdated(selectedList);
  }


  /**
   * Used to replace a non-existant expanded list with one obeying business rules (called recursively).
   * Invokes action callback for updating the new expanded list.
   */
  setExpandedList(nodes, selectedList, expandedList = []) {

    // If the selected list is empty or non-existant, the expanded list is likewise empty and there is nothing
    // more to do.
    if (selectedList && selectedList.length > 0) {
      nodes.forEach(node => {

        // According to the business rule, indeterminate nodes get expanded.
        if (this.isIndeterminate(node, selectedList)) {
          expandedList.push(node.id);
        }

        // descend the tree
        this.setExpandedList(node.children, selectedList, expandedList);
      });
    }
    this.newExpandedList = expandedList;
  }


  /**
   * Selects all the tree's leaves and calls the appropriate update method in the action creator
   */
  selectAll(event) {
    let selectedList = [];
    this.props.tree.forEach(node =>
      isLeafNode(node) ? selectedList.push(node.id) : selectedList.push(...getLeaves(node).map(leaf => leaf.id))
    );
    this.setSelectedList(selectedList);
    
    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Clears the selected list and calls the appropriate update method in the action creator
   */
  clearAll(event) {
    this.setSelectedList();

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Selects all the tree's branches and calls the appropriate update method in the action creator
   */
  expandAll(event) {
    let expandedList = [];
    this.props.tree.forEach(node => {
      expandedList.push(...getBranches(node).map(branch => branch.id));
    });
    this.props.onExpandedListUpdated(expandedList);

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Clears the expanded list and calls the appropriate update method in the action creator
   */
  collapseAll(event) {
    let expandedList = [];
    this.props.onExpandedListUpdated(expandedList);

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Calls the appropriate method in the action creator to reload the original selects
   */
  toCurrent(event) {
    this.props.onCurrentSelectedListLoaded();

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Calls the appropriate method in the action creator to load the default selects
   */
  toDefault(event) {
    this.props.onDefaultSelectedListLoaded();

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Convey branch to be toggled to action creator - id indicates the node id to
   * be changed.
   */
  toggleExpansion(id) {
    let newExpandedList = this.props.expandedList;

    // if the expanded list is null (unlikely, but possible) create an intial
    // expanded list that obeys business rules.
    if (newExpandedList === null || newExpandedList === undefined) {
      this.setExpandedList(this.props.tree, this.props.selectedList);
      newExpandedList = this.newExpandedList;
    }
    let index = newExpandedList.indexOf(id);
    index <= -1 ? newExpandedList.push(id) : newExpandedList.splice(index, 1);
    this.props.onExpandedListUpdated(newExpandedList);
  }


  /**
   * Convey checkbox to be toggled to action creator
   * If toggled checkbox is a selected leaf - add the leaf to the select list to be returned
   * If toggled checkbox is an unselected leaf - remove the leaf from the select list to be returned
   * If toggled checkbox is a selected non-leaf - identify the node's leaves (cached) and add them to the select list to be returned
   * If toggled checkbox is an unselected leaf - identify the node's leaves (cached) and remove them from the select list to be returned
   */ƒ
  toggleCheckbox(nodeId, selected) {
    let newSelectedList = this.props.selectedList || [];
    let node = getNodeById(nodeId, this.props.tree);
    if (isLeafNode(node)) {
      let index = newSelectedList.indexOf(nodeId);
      index > -1 ? newSelectedList.splice(index, 1) : newSelectedList.push(nodeId);
    }
    else {
      let leafIds = getLeaves(node).map(leafNode => leafNode.id);
      leafIds.forEach(leafId => {
        let index = newSelectedList.indexOf(leafId);
        if (selected && index === -1) {
          newSelectedList.push(leafId);
        }
        if (!selected && index > -1) {
          newSelectedList.splice(index, 1);
        }
      });
    }

    // convey new selected list back to action creator
    this.props.onSelectedListUpdated(newSelectedList);
  }


  /**
   * Returns boolean indicating whether the given node is selected
   */
  isSelected(node, selectedList) {

    // If the selected list is empty or non-existant, no check is needed.  The node is not selected.
    if (selectedList) {
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

  /**
   * Returns boolean indicating whether the given node is indeterminate
   */
  isIndeterminate(node, selectedList) {

    // if only some of the descendent leaf nodes are in the selected nodes list, the given
    // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
    let indeterminate = false;

    // If the selected list is empty, or non-existant no nodes are intermediate and there is nothing to do.
    if (selectedList) {
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


  /**
   * Returns boolean indicating whether given node should be shown expanded or collapsed.
   */
  isExpanded(node, expandedList, selectedList) {

    // The expandedList exists, empty or not, at this point, but if somehow it isn't
    // the business rules are used to decide whether the node is expanded or collapsed.
    // The business expand only indeterminate nodes.
    if (this.props.expandedList === null || this.props.expandedList === undefined) {
      return this.isIndeterminate(node, selectedList);
    }
    else {
      return expandedList.indexOf(node.id) > -1 ? true : false;
    }
  }


  /**
   * Render the checkbox tree
   */
  render() {
    return (
      <div className="wdk-CheckboxTree" id={this.props.name}>
        {this.renderLinks()}
        <ul className="fa-ul wdk-CheckboxTree-list" key={"list_root"}>
          {this.props.tree.map(function (node) {
            return this.renderTreeNode(node);
          }, this)}
        </ul>
        {this.renderLinks()}
      </div>
    );
  }

  /**
   * Render each node of the checkbox tree
   * @param node
   * @returns {XML}
   */
  renderTreeNode(node) {
    let toggleCheckbox = this.toggleCheckbox;
    let toggleExpansion = this.toggleExpansion;
    let indeterminate = this.isIndeterminate(node, this.props.selectedList);
    let selected = this.isSelected(node, this.props.selectedList);
    let expanded = this.isExpanded(node, this.props.expandedList, this.props.selectedList);
    let leaf = isLeafNode(node);
    let nodeType = !leaf && !expanded ? "wdk-CheckboxTree-collapsedItem" :
      leaf ? "wdk-CheckboxTree-leafItem" : "wdk-CheckboxTree-expandedItem";

    return (
      <li className={nodeType} key={"item_" + node.id}>

        <AccordionButton leaf={leaf}
                         expanded={expanded}
                         id={node.id}
                         key={"accordion_" + node.id}
                         toggleExpansion={toggleExpansion}
        />
        <label title={node.description} key={"label_" + node.id}>
          <IndeterminateCheckbox
            checked={selected}
            indeterminate={indeterminate}
            id={node.id}
            key={"checkbox_" + node.id}
            value={node.id}
            toggleCheckbox={toggleCheckbox}
          />
          {node.displayName}
        </label>
        {!leaf && expanded ?
          <ul className="fa-ul wdk-CheckboxTree-list" key={"list_" + node.id}>
            {node.children.map(child => this.renderTreeNode(child))}
          </ul> : "" }
      </li>
    )
  }

  /**
   * Render the checkbox tree links
   * @returns {XML}
   */
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

CheckboxTree.propTypes = {

  /** Value to use for name of checkbox tree - used as id of enclosing div  */
  name: PropTypes.string,

  /** Array representing top level nodes in the checkbox tree **/
  tree: PropTypes.array.isRequired,

  /**
   * List of selected nodes as represented by their ids.
   */
  selectedList: PropTypes.array,

  /**
   * List of expanded nodes as represented by their ids.
   */
  expandedList: PropTypes.array,

  /**
   * Called when the set of selected (leaf) nodes changes.
   * The function will be called with the array of the selected node
   * ids.
   */
  onSelectedListUpdated: PropTypes.func,

  /**
   * Called when the set of expanded (branch) nodes changes.
   * The function will be called with the array of the expanded node
   * ids.
   */
  onExpandedListUpdated: PropTypes.func,

  /**
   * Called when the user reverts to the default select list.
   * The function will be called with no arguments as the default
   * state is maintained by the store.
   */
  onDefaultSelectedListLoaded: PropTypes.func,

  /**
   * Called when the user reverts to the select list with which he/she started.
   * The function will be called with no arguments as the original
   * state is maintained by the store.
   */
  onCurrentSelectedListLoaded: PropTypes.func

};

