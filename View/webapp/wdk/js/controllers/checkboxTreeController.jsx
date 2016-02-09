import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTree from '../client/components/CheckboxTree';
import {
  getTargetType,
  getRefName,
  getId,
  getDisplayName,
  getDescription
} from '../client/utils/OntologyUtils';
import {
  getNodeByValue
} from '../client/utils/TreeUtils';


// serves as MVC controller for checkbox tree on results page
export default class CheckboxTreeController {

  constructor(element, name, tree, selectedList, expandedList, defaultSelectedList, getAttributes) {
    this.element = element;
    this.name = name;
    this.tree = tree;
    this.getAttributes = getAttributes;
    this.displayCheckboxTree = this.displayCheckboxTree.bind(this);
    this.updateSelectedList = this.updateSelectedList.bind(this);
    this.updateExpandedList = this.updateExpandedList.bind(this);
    this.loadDefaultSelectedList = this.loadDefaultSelectedList.bind(this);
    this.loadCurrentSelectedList = this.loadCurrentSelectedList.bind(this);
    this.getNodeReactElement = this.getNodeReactElement.bind(this);
    this.getNodeFormValue = this.getNodeFormValue.bind(this);
    this.getNodeChildren = this.getNodeChildren.bind(this);
    this.getNodeData = this.getNodeData.bind(this);
    this.selectedList = selectedList;
    this.expandedList = expandedList;
    this.defaultSelectedList = defaultSelectedList;
    this.currentSelectedList = (selectedList || []).concat();
  }

  displayCheckboxTree() {
    ReactDOM.render(
      <CheckboxTree tree={this.tree}
                    selectedList={this.selectedList}
                    expandedList={this.expandedList}
                    name={this.name}
                    onSelectedListUpdated={this.updateSelectedList}
                    onExpandedListUpdated={this.updateExpandedList}
                    onDefaultSelectedListLoaded={this.loadDefaultSelectedList}
                    onCurrentSelectedListLoaded={this.loadCurrentSelectedList}
                    getNodeReactElement={this.getNodeReactElement}
                    getNodeFormValue={this.getNodeFormValue}
                    getNodeChildren={this.getNodeChildren}
      />, this.element[0]);
  }


  getNodeData(node) {
    let data = {};
    let targetType = getTargetType(node);
    if (targetType === 'attribute') {
      let attribute = this.getAttributes(node);
      if(attribute == null) {
      // This should not happen...will replace with an exception
      data.displayName = getRefName(node) + "??";
      data.description = getRefName(node) + "??";
      data.id =  "attribute_" + getId(node);
      }
      else {
        data.displayName = attribute.displayName;
        data.description = attribute.help;
        data.id = getRefName(node);
      }
    }
    else {
      data.id = getId(node);
      data.displayName = getDisplayName(node);
      data.description = getDescription(node);
    }
    return data;
  }

  getNodeFormValue(node) {
    return this.getNodeData(node).id
  }


  getNodeReactElement(node) {
    let data = this.getNodeData(node);
    return <span title={data.description}>{data.displayName}</span>
  }


  getNodeChildren(node) {
    return node.children;
  }

  updateSelectedList(selectedList) {
    this.selectedList = selectedList;
    this.displayCheckboxTree();
  }

  updateExpandedList(expandedList) {
    this.expandedList = expandedList;
    this.displayCheckboxTree();
  }

  loadDefaultSelectedList() {
    this.updateSelectedList(this.defaultSelectedList);
  }

  loadCurrentSelectedList() {
    console.log("Current selected list " + JSON.stringify(this.currentSelectedList));
    this.updateSelectedList(this.currentSelectedList);
  }
}