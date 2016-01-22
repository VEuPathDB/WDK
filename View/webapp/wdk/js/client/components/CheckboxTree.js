import React from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox';
import AccordionButton from './AccordionButton';
import {isLeafNode} from '../utils/TreeUtils';
import {getLeaves} from '../utils/TreeUtils';

export default class CheckboxTree extends React.Component {

  // returns boolean indicating whether the given node is selected
  isSelected(node, selectedList) {
    if(!isLeafNode(node)) {

      // When the node is not a leaf, it is considered selected if every one of its leaf nodes
      // is in the selected list.
      let leafIds = getLeaves(node).map(leafNode => leafNode.id);
      return leafIds.every(leafId => selectedList.indexOf(leafId) > -1);
    }
    else {
      return selectedList.indexOf(node.id) > -1;
    }
  }

  // returns boolean indicating whether the given node is indeterminate
  isIndeterminate(node, selectedList) {

    // if only some of the descendent leaf nodes are in the selected nodes list, the given
    // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
    let indeterminate = false;
    if (!isLeafNode(node)) {
      let leafIds = getLeaves(node).map(leafNode => leafNode.id);
      let total = leafIds.reduce(function (count, leafId) {
        return selectedList.indexOf(leafId) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafIds.length) {
        indeterminate = true;
      }
    }
    return indeterminate;
  }


  // returns boolean indicating whether given node should be shown expanded or collapsed.
  isExpanded(node, expandedList, selectedList) {

    // The expandedList exists, empty or not, at this point, but if somehow it isn't
    // the business rules are used to decide whether the node is expanded or collapsed.
    // The business expand only indeterminate nodes.
    if (expandedList === null || expandedList === undefined) {
      return isIndeterminate(node, selectedList);
    }
    else {
      return expandedList.indexOf(node.id) > -1 ? true : false;
    }
  }


  // render the checkbox tree at the current node
  render() {
    let selectedList = this.props.selectedList;
    let expandedList = this.props.expandedList;
    let toggleCheckbox = this.props.toggleCheckbox;
    let toggleExpansion = this.props.toggleExpansion;
    return (
        <ul className="fa-ul wdk-CheckboxTree-list" key={"list_" + parent}>
          {this.props.tree.map(function(node) {
            let indeterminate = this.isIndeterminate(node, selectedList);
            let selected = this.isSelected(node, selectedList);
            let expanded = this.isExpanded(node, expandedList, selectedList);
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
                  {!leaf && expanded  ? <CheckboxTree tree={node.children}
                                                            selectedList={selectedList}
                                                            expandedList={expandedList}
                                                            key={"childOf_" + node.id}
                                                            toggleCheckbox={toggleCheckbox}
                                                            toggleExpansion={toggleExpansion}
                  /> : ""}
                </li>
            )
          }, this)}
        </ul>
    );
  }

}