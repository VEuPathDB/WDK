import React from 'react';
import IndeterminateCheckbox from './IndeterminateCheckbox.js';
import AccordionButton from './AccordionButton.js';

export default class CheckboxTree extends React.Component {

  // identify the state of the node based upon the selected list (selected) and the indeterminate list (indeterminate)
  setNodeState(node, selectedList, indeterminateList, expandedList) {
    let result = {"checked": false, "indeterminate": false, "expanded" : false};
    if (selectedList.indexOf(node.id) > -1) {
      result.checked = true;
    }
    if(indeterminateList.indexOf(node.id) > -1) {
      result.indeterminate = true;
    }
    if(expandedList.indexOf(node.id) > -1) {
      result.expanded = true;
    }
    return result;
  }

  // render the checkbox tree at the current node (as defined in the props)
  render() {
    let selectedList = this.props.selectedList;
    let expandedList = this.props.expandedList;
    let indeterminateList = this.props.indeterminateList;
    let store = this.props.store;
    let toggleCheckbox = this.props.toggleCheckbox;
    let toggleExpansion = this.props.toggleExpansion;
    return (
        <ul className="fa-ul wdk-CheckboxTree-list" key={"list_" + parent}>
          {this.props.tree.map(function(node) {
            let nodeState = this.setNodeState(node, selectedList, indeterminateList, expandedList);
            let icon = nodeState.expanded && node.children.length > 0 ? <i className="fa-li fa fa-caret-down"></i> : null;
            icon = icon === null && node.children.length > 0 ? <i className="fa-li fa fa-caret-right"></i> : icon;
            let nodeType = node.children.length > 0 && !nodeState.expanded ? "wdk-CheckboxTree-collapsedItem" : "wdk-CheckboxTree-expandedItem";
            nodeType = node.children.length === 0 ? "wdk-CheckboxTree-leafItem" : nodeType;

            return (
                <li className={nodeType} key={"item_" + node.id}>

                  <AccordionButton icon={icon}
                                   id={node.id}
                                   key = {"accordion_" + node.id}
                                   toggleExpansion={toggleExpansion}
                  />
                  <IndeterminateCheckbox
                      checked = {nodeState.checked}
                      indeterminate = {nodeState.indeterminate}
                      id={node.id}
                      key={"checkbox_" + node.id}
                      value={node.id}
                      toggleCheckbox={toggleCheckbox}
                  />
                  <label title={node.description} key={"label_" + node.id}>{node.displayName}</label>
                  {node.children.length > 0 && nodeState.expanded  ? <CheckboxTree tree={node.children}
                                                            selectedList={selectedList}
                                                            expandedList={expandedList}
                                                            indeterminateList={indeterminateList}
                                                            key={"childOf_" + node.id}
                                                            store={store}
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