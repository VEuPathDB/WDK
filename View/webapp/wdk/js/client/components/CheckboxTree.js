import React from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox.js';
import AccordionButton from './AccordionButton.js';

export default class CheckboxTree extends React.Component {

  // convenience method to identify nodes that are leaves
  isLeafNode(node) {
    return node.children.length === 0;
  }

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

  isSelected(node, selectedList) {
    if(!this.isLeafNode(node)) {
      let leaves = this.getLeafIds(node);
      if (selectedList.indexOf(node.id) === -1) {
        return leaves.reduce(function (included, leafId) {
          included = !included || selectedList.indexOf(leafId) === -1 ? false : true;
          return included;
        }, true);
      }
    }
    else {
      return selectedList.indexOf(node.id) > -1;
    }
  }

  // discovers whether the given node is indeterminate
  isIndeterminate(node, selectedList) {
    let leafIds = this.getLeafIds(node);

    // if only some of the descendent leaf nodes are in the selected nodes list, the given
    // node is indeterminate.  If the given node is a leaf node, it cannot be indeterminate
    let indeterminate = false;
    if (leafIds.length > 0) {
      let total = leafIds.reduce(function (count, leafId) {
        return selectedList.indexOf(leafId) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafIds.length) {
        indeterminate = true;
      }
    }
    return indeterminate;
  }


  isExpanded(node, expandedList, selectedList) {
    if (expandedList === null || expandedList === undefined) {
      let leafIds = this.getLeafIds(node);
      let expanded;

      // if only some of the leaf nodes for the given node are selected, expand the given node
      let total = leafIds.reduce(function (count, leafId) {
        return selectedList.indexOf(leafId) > -1 ? count + 1 : count;
      }, 0);
      if (total > 0 && total < leafIds.length) {
        expanded = true;
      }
      return expanded;
    }
    else {
      return expandedList.indexOf(node.id) > -1 ? true : false;
    }
  }


  // render the checkbox tree at the current node (as defined in the props)
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
            let icon = expanded && node.children.length > 0 ? <i className="fa-li fa fa-caret-down"></i> : null;
            icon = icon === null && node.children.length > 0 ? <i className="fa-li fa wdk-CheckboxTree-icon fa-caret-right"></i> : icon;
            let nodeType = node.children.length > 0 && !expanded ? "wdk-CheckboxTree-collapsedItem" : "wdk-CheckboxTree-expandedItem";
            nodeType = node.children.length === 0 ? "wdk-CheckboxTree-leafItem" : nodeType;

            return (
                <li className={nodeType} key={"item_" + node.id}>

                  <AccordionButton icon={icon}
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
                  {node.children.length > 0 && expanded  ? <CheckboxTree tree={node.children}
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