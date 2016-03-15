import React,{PropTypes} from 'react';
import pick from 'lodash/object/pick';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';
import CheckboxTreeNode from './CheckboxTreeNode';
import {isLeafNode} from '../utils/TreeUtils';
import {getLeaves} from '../utils/TreeUtils';
import {getBranches} from '../utils/TreeUtils';
import {mapStructure} from '../utils/TreeUtils';

/**
 * Render the checkbox tree links
 * @returns {XML}
 */
let TreeLinks = props => {
  let {
    showSelectionLinks, showExpansionLinks,
    onSelectAll, onClearAll, onExpandAll, onCollapseAll,
    onResetToCurrent, onResetToDefault } = props;
  return (
    <div className="wdk-CheckboxTree-links">
      {showSelectionLinks ?
        <span>
          <a href="#" onClick={onSelectAll}>select all</a> |
          <a href="#" onClick={onClearAll}> clear all</a>
          <br />
        </span> :
        "" }

      {showExpansionLinks ?
        <span>
          <a href="#" onClick={onExpandAll}> expand all</a> |
          <a href="#" onClick={onCollapseAll}> collapse all</a>
          <br />
        </span> :
        "" }

      {showSelectionLinks ?
        <span>
          <a href="#" onClick={onResetToCurrent}>reset to current</a> |
          <a href="#" onClick={onResetToDefault}>reset to default</a>
          <br />
        </span> :
        "" }
    </div>
  );
}

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
  onSelectAll(event) {
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
  onClearAll(event) {
    this.setSelectedList();

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Selects all the tree's branches and calls the appropriate update method in the action creator
   */
  onExpandAll(event) {
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
  onCollapseAll(event) {
    let expandedList = [];
    this.props.onExpandedListUpdated(expandedList);

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Calls the appropriate method in the action creator to reload the original selects
   */
  onResetToCurrent(event) {
    this.props.onCurrentSelectedListLoaded();

    // prevent update to URL
    event.preventDefault();
  }


  /**
   * Calls the appropriate method in the action creator to load the default selects
   */
  onResetToDefault(event) {
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
   * Render the checkbox tree
   */
  render() {
    let {
      tree,
      getSearchBox,
      isSearchMode,
      onSearch,
      selectedList,
      expandedList,
      fieldName,
      removeCheckboxes,
      getNodeFormValue,
      getNodeChildren,
      getBasicNodeReactElement
    } = this.props;
    let toggleCheckbox = this.toggleCheckbox;
    let toggleExpansion = this.toggleExpansion;
    let treeLinkHandlers =
      pick(this, [ 'onSelectAll', 'onClearAll',
                   'onExpandAll', 'onCollapseAll',
                   'onResetToCurrent', 'onResetToDefault' ]);
    return (
      <div className="wdk-CheckboxTree" id={this.props.name}>
        <TreeLinks showSelectionLinks={!removeCheckboxes} showExpansionLinks={!isSearchMode} {...treeLinkHandlers} />
        {getSearchBox ? getSearchBox() : ""}
        <ul className="fa-ul wdk-CheckboxTree-list">
          {tree.map(node => {
            let mappedChildren = getNodeChildren(node);
            let nodeType = getNodeChildren(node) === 0 && !isExpanded && !isSearchMode ? "wdk-CheckboxTree-collapsedItem" :
                getNodeChildren(node) > 0 ? "wdk-CheckboxTree-leafItem" : "wdk-CheckboxTree-expandedItem";
            return mapStructure(function(node, mappedChildren) {
              return (
                <CheckboxTreeNode
                  key={"node_" + getNodeFormValue(node)}
                  node={node}
                  nodeType={nodeType}
                  isSearchMode={isSearchMode}
                  toggleCheckbox={toggleCheckbox}
                  toggleExpansion={toggleExpansion}
                  removeCheckboxes={removeCheckboxes}
                  isSelected={CheckboxTree.isSelected(node, selectedList, getNodeFormValue, getNodeChildren)}
                  isIndeterminate={CheckboxTree.isIndeterminate(node, selectedList, getNodeFormValue, getNodeChildren)}
                  isExpanded={CheckboxTree.isExpanded(node, expandedList, getNodeFormValue)}
                  isVisible={CheckboxTree.isVisible(node, expandedList, isSearchMode, onSearch, getNodeFormValue)}
                  isMatching={CheckboxTree.isMatching(node, isSearchMode, onSearch)}
                  fieldName={fieldName}
                  getNodeFormValue={getNodeFormValue}
                  getNodeChildren={getNodeChildren}
                  getBasicNodeReactElement={getBasicNodeReactElement}>
                  {mappedChildren}
                </CheckboxTreeNode>
              );
            }, getNodeChildren, node);
           })
          }
        </ul>
        <TreeLinks showSelectionLinks={!removeCheckboxes} showExpansionLinks={!isSearchMode} {...treeLinkHandlers} />
      </div>
    );
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
   *  its ancestors match the search criteria.  The predicate function accepts a node as input and outputs a
   *  boolean indicating whether or not the node matches the criteria.
   */
  onSearch: PropTypes.func,

  /** Indicates whether checkboxes (the default L&F) should be omitted */
  removeCheckboxes: PropTypes.bool

};


/**
 * Returns boolean indicating whether the given node is selected
 */
CheckboxTree.isSelected = function(node, selectedList, getNodeFormValue, getNodeChildren) {

  // If the selected list is empty or non-existant, no check is needed.  The node is not selected.
  if (selectedList) {
    if (!isLeafNode(node, getNodeChildren)) {

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
CheckboxTree.isExpanded = function(node, expandedList, getNodeFormValue) {
  return expandedList == null ? false : expandedList.indexOf(getNodeFormValue(node)) > -1;
};

/**
 * Return boolean indicating whether node matches the search parameter as defined
 * by the included search predicate.  If no search is in progress, the function returns true.
 * @param node
 * @param isSearchMode
 * @param onSearch - fn - search predicate which takes the node and returns true for a match and false otherwise
 */
CheckboxTree.isMatching = function(node, isSearchMode, onSearch) {
  return isSearchMode ? onSearch(node) : true;
};


/**
 * Return boolean indicating whether given node's children should be visible.  This is governed
 * by whether the node is expanded but is overriden by the isMatching fn when a search is in progress.
 * @param node
 * @param expandedList
 * @returns {*}
 */
CheckboxTree.isVisible = function(node, expandedList, isSearchMode, onSearch, getNodeFormValue) {
  return isSearchMode ? onSearch(node) : CheckboxTree.isExpanded(node, expandedList, getNodeFormValue);
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
};


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
      CheckboxTree.setExpandedList(getNodeChildren(node), selectedList, expandedList);
    });
  }
  return expandedList;
};
