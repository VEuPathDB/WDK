import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTree from '../client/components/CheckboxTree';


// serves as MVC controller for checkbox tree on results page
export default class CheckboxTreeController {

  constructor(tree, selectedList, expandedList) {
    this.tree = tree;
    this.selectedList = selectedList;
    this.expandedList = expandedList;
    this.displayCheckboxTree = this.displayCheckboxTree.bind(this);
    this.updateSelectedList = this.updateSelectedList.bind(this);
    this.updateExpandedList = this.updateExpandedList.bind(this);
  }

  displayCheckboxTree() {
    ReactDOM.render(
      <CheckboxTree tree={this.tree}
                    key="Root"
                    selectedList={this.selectedList}
                    expandedList={this.expandedList}
                    name="AttributeList"
                    onSelectedListUpdated={this.updateSelectedList}
                    onExpandedListUpdated={this.updateExpandedList}
                    onDefaultSelectedListLoaded={this.loadDefaultSelectedList}
                    onCurrentSelectedListLoaded={this.loadCurrentSelectedList}
      />, document.getElementById("newAttributeCheckboxTree"));
    console.log("Checkbox Tree rendered");
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
    console.log("TBD");
  }

  loadCurrentSelectedList() {
    console.log("TBD");
  }
}