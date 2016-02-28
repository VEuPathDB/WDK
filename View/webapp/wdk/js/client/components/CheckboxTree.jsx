import React,{PropTypes} from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';
import {isLeafNode} from '../utils/TreeUtils';
import {getLeaves} from '../utils/TreeUtils';
import {getBranches} from '../utils/TreeUtils';


/**
 * A null or undefined selected list should be made into an empty array
 * @type {Array}
 */
export default class CheckboxTree extends React.Component {

  /**
   * Hards binds all the user interaction methods to this object.
   * @param props - component properties
   */
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


  /**
   *  Used to update a selected list
   *  Invokes action callback for updating the new selected list.
   */
  setSelectedList(selectedList = []) {
    this.props.onSelectedListUpdated(selectedList);
  }


  /**
   * Selects all the tree's leaves and calls the appropriate update method in the action creator
   */
  selectAll(event) {
    let selectedList = [];
    this.props.tree.forEach(node =>
      isLeafNode(node, this.props.getNodeChildren) ?
        selectedList.push(this.props.getNodeFormValue(node)) :
        selectedList.push(...getLeaves(node, this.props.getNodeChildren).map(leaf => this.props.getNodeFormValue(leaf)))
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
      expandedList.push(...getBranches(node, this.props.getNodeChildren).map(branch => this.props.getNodeFormValue(branch)));
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
  toggleExpansion(node) {
    let value = this.props.getNodeFormValue(node);
    let newExpandedList = this.props.expandedList || [];
    let index = newExpandedList.indexOf(value);
    index <= -1 ? newExpandedList.push(value) : newExpandedList.splice(index, 1);
    this.props.onExpandedListUpdated(newExpandedList);
  }


  /**
   * Convey checkbox to be toggled to action creator
   * If toggled checkbox is a selected leaf - add the leaf to the select list to be returned
   * If toggled checkbox is an unselected leaf - remove the leaf from the select list to be returned
   * If toggled checkbox is a selected non-leaf - identify the node's leaves (cached) and add them to the select list to be returned
   * If toggled checkbox is an unselected leaf - identify the node's leaves (cached) and remove them from the select list to be returned
   */
  toggleCheckbox(node, selected) {
    let value = this.props.getNodeFormValue(node);
    let newSelectedList = this.props.selectedList || [];
    if (isLeafNode(node, this.props.getNodeChildren)) {
      let index = newSelectedList.indexOf(value);
      index > -1 ? newSelectedList.splice(index, 1) : newSelectedList.push(value);
    }
    else {
      let leafNodes = getLeaves(node, this.props.getNodeChildren);
      leafNodes.forEach(leafNode => {
        let leafValue = this.props.getNodeFormValue(leafNode);
        let index = newSelectedList.indexOf(leafValue);
        if (selected && index === -1) {
          newSelectedList.push(leafValue);
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
      if (!isLeafNode(node, this.props.getNodeChildren)) {

        // When the node is not a leaf, it is considered selected if every one of its leaf nodes
        // is in the selected list.
        let leafNodes = getLeaves(node, this.props.getNodeChildren);
        return leafNodes.every(leafNode => selectedList.indexOf(this.props.getNodeFormValue(leafNode)) > -1);
      }
      else {
        return selectedList.indexOf(this.props.getNodeFormValue(node)) > -1;
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
      if (!isLeafNode(node, this.props.getNodeChildren)) {
        let leafNodes = getLeaves(node, this.props.getNodeChildren);
        let total = leafNodes.reduce((count, leafNode) => {
          return selectedList.indexOf(this.props.getNodeFormValue(leafNode)) > -1 ? count + 1 : count;
        }, 0);
        if (total > 0 && total < leafNodes.length) {
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
      return expandedList.indexOf(this.props.getNodeFormValue(node)) > -1;
    }
  }


  /**
   * Render the checkbox tree
   */
  render() {
    let getSearchBox = this.props.getSearchBox;
    let isSearchMode = this.props.isSearchMode;
    return (
      <div className="wdk-CheckboxTree" id={this.props.name}>
        {this.renderLinks(isSearchMode)}
        {getSearchBox ? getSearchBox() : ""}
        <ul className="fa-ul wdk-CheckboxTree-list" key={"list_root"}>
          {this.props.tree.map(function (node) {
            return this.renderTreeNode(node, isSearchMode);
          }, this)}
        </ul>
        {this.renderLinks(isSearchMode)}
      </div>
    );
  }


  /**
   * Render each node of the checkbox tree
   * @param node
   * @returns {XML}
   */
  renderTreeNode(node, isSearchMode) {
    let matchingNode = this.props.onSearch === undefined || this.props.onSearch === null ? undefined : this.props.onSearch(node);
    let toggleCheckbox = this.toggleCheckbox;
    let toggleExpansion = this.toggleExpansion;
    let indeterminate = this.isIndeterminate(node, this.props.selectedList);
    let selected = this.isSelected(node, this.props.selectedList);
    let expanded = this.isExpanded(node, this.props.expandedList, this.props.selectedList);
    // Hiding unexpanded checkbox subtrees rather then not constructing them.  More compatible
    // with more standard (non-React) form submission.
    let display = expanded ? "block" : "none";
    let match = matchingNode === undefined ? "block" : matchingNode ? "block" : "none";
    display = matchingNode === undefined ? display : matchingNode ? "block" : "none";
    let fieldName = this.props.fieldName;
    let value = this.props.getNodeFormValue(node);
    let leaf = isLeafNode(node, this.props.getNodeChildren);
    let nodeType = !leaf && !expanded && !isSearchMode ? "wdk-CheckboxTree-collapsedItem" :
      leaf ? "wdk-CheckboxTree-leafItem" : "wdk-CheckboxTree-expandedItem";

    return (
      <li className={nodeType} key={"item_" + value} style={{display: match}}>

        <AccordionButton leaf={leaf}
                         expanded={expanded}
                         visible={!isSearchMode}
                         node={node}
                         toggleExpansion={toggleExpansion} />

        <label>
          {!this.props.removeCheckboxes ?
          <IndeterminateCheckbox
            name={fieldName}
            checked={selected}
            indeterminate={indeterminate}
            node={node}
            value={value}
            toggleCheckbox={toggleCheckbox} /> : "" }
          {this.props.getBasicNodeReactElement(node)}
        </label>
        {!leaf ?
          <ul className="fa-ul wdk-CheckboxTree-list" key={"list_" + value} style={{display}}>
            {this.props.getNodeChildren(node).map(child => this.renderTreeNode(child, isSearchMode))}
          </ul> : "" }
      </li>
    )
  }

  /**
   * Render the checkbox tree links
   * @returns {XML}
   */
  renderLinks(isSearchMode) {
    let selectAll = this.selectAll;
    let clearAll = this.clearAll;
    let expandAll = this.expandAll;
    let collapseAll = this.collapseAll;
    let toCurrent = this.toCurrent;
    let toDefault = this.toDefault;
    return (
      <div className="wdk-CheckboxTree-links">
        {!this.props.removeCheckboxes ?
        <span>
          <a href="#" onClick={selectAll}>select all</a> |
          <a href="#" onClick={clearAll}> clear all</a>
          <br />
        </span> :
        "" }

        {!isSearchMode ?
          <span>
            <a href="#" onClick={expandAll}> expand all</a> |
            <a href="#" onClick={collapseAll}> collapse all</a>
            <br />
          </span> :
          "" }

        {!this.props.removeCheckboxes ?
          <span>
            <a href="#" onClick={toCurrent}>reset to current</a> |
            <a href="#" onClick={toDefault}> reset to default</a>
          </span> :
          "" }
      </div>
    )
  }
}

CheckboxTree.propTypes = {

  /** Value to use for name of checkbox tree - used as id of enclosing div  */
  name: PropTypes.string,

  /** Array representing top level nodes in the checkbox tree **/
  tree: PropTypes.array.isRequired,

  /** Value to use for the name of the checkboxes in the tree */
  fieldName: PropTypes.string,

  /** List of selected nodes as represented by their ids. */
  selectedList: PropTypes.array,

  /** List of expanded nodes as represented by their ids. */
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
  onCurrentSelectedListLoaded: PropTypes.func,

  /** Called during rendering to create the react element holding the display name and tooltip for the current node */
  getBasicNodeReactElement: PropTypes.func,

  /** Called during rendering to provide the input value for the current node */
  getNodeFormValue: PropTypes.func,

  /** Called during rendering to provide the children for the current node */
  getNodeChildren:  PropTypes.func,

  /** Indicates whether a search is ongoing - use to suppress expand/collapse functionality */
  isSearchMode: PropTypes.bool,

  /** Provides the search box React element to drop in */
  getSearchBox: PropTypes.func,

  /** Called during rendering to identify whether a given node should be made visible based upon whether it or
   *  its ancestors match the search criteria.
   */
  onSearch: PropTypes.func,

  /** Indicates whether checkboxes (the default L&F) should be omitted */
  removeCheckboxes: PropTypes.bool

};



/**
 * Returns boolean indicating whether the given node is indeterminate
 */
CheckboxTree.isIndeterminate = function(node, selectedList, getNodeFormValue, getNodeChildren) {

  // if only some of the descendent leaf nodes are in the selected nodes list, the given
  // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
  let indeterminate = false;

  // If the selected list is empty, or non-existant no nodes are intermediate and there is nothing to do.
  if (selectedList) {
    if (!isLeafNode(node, getNodeChildren)) {
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
 * Used to replace a non-existant expanded list with one obeying business rules (called recursively).
 * Invokes action callback for updating the new expanded list.
 */
CheckboxTree.setExpandedList = function(nodes, getNodeFormValue, getNodeChildren, selectedList, expandedList = [])  {

  // If the selected list is empty or non-existant, the expanded list is likewise empty and there is nothing
  // more to do.
  if (selectedList && selectedList.length > 0) {
    nodes.forEach(node => {

      // According to the business rule, indeterminate nodes get expanded.
      if (this.isIndeterminate(node, selectedList, getNodeFormValue, getNodeChildren)) {
        expandedList.push(getNodeFormValue(node));
      }
      // descend the tree
      this.setExpandedList(getNodeChildren(node), selectedList, expandedList);
    });
  }
  return expandedList;
}
