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


// serves as MVC controller for checkbox tree on results page
export default class CheckboxTreeController {

  constructor(element, name, tree, selectedList, expandedList, defaultSelectedList, getAttribute) {
    this.element = element;
    this.name = name;
    this.tree = tree;
    this.selectedList = selectedList;
    this.expandedList = expandedList;
    this.defaultSelectedList = defaultSelectedList;
    this.currentSelectedList = (selectedList || []).concat();
    this.getAttribute = getAttribute;
    this.displayCheckboxTree = this.displayCheckboxTree.bind(this);
    this.updateSelectedList = this.updateSelectedList.bind(this);
    this.updateExpandedList = this.updateExpandedList.bind(this);
    this.loadDefaultSelectedList = this.loadDefaultSelectedList.bind(this);
    this.loadCurrentSelectedList = this.loadCurrentSelectedList.bind(this);
    this.getNodeProperties = this.getNodeProperties.bind(this);
  }

  displayCheckboxTree() {
    console.log("Using element " + this.element[0]);
    ReactDOM.render(
      <CheckboxTree tree={this.tree}
                    key="Root"
                    selectedList={this.selectedList}
                    expandedList={this.expandedList}
                    name={this.name}
                    onSelectedListUpdated={this.updateSelectedList}
                    onExpandedListUpdated={this.updateExpandedList}
                    onDefaultSelectedListLoaded={this.loadDefaultSelectedList}
                    onCurrentSelectedListLoaded={this.loadCurrentSelectedList}
                    onGetNodeProperties={this.getNodeProperties}
      />, this.element[0]);
    console.log("Rendered checkbox with selectedList: " + JSON.stringify(this.selectedList));
  }

  getNodeProperties(node) {
    let properties = {};
    let targetType = getTargetType(node);
    if (targetType === 'attribute') {
      let attribute = this.getAttribute(node);
      if(attribute == null) {
      // This should not happen...will replace with an exception
      properties.displayName = getRefName(node) + "??";
      properties.description = getRefName(node) + "??";
      properties.id =  "attribute_" + getId(node);
      }
      else {
        properties.displayName = attribute.displayName;
        properties.description = attribute.help;
        properties.id = getRefName(node);
      }
    }
    else {
      properties.id = getId(node);
      properties.displayName = getDisplayName(node);
      properties.description = getDescription(node);
    }
    return properties;
    //return {"id":node.id, "displayName":node.displayName, "description":node.description};
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