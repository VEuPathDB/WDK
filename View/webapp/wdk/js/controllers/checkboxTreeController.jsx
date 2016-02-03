import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTree from '../client/components/CheckboxTree';


// serves as MVC controller for checkbox tree on results page
export default class CheckboxTreeController {

  constructor(tree, selectedList, expandedList, defaultSelectedList) {
    this.tree = tree;
    this.selectedList = selectedList;
    this.expandedList = expandedList;
    this.defaultSelectedList = defaultSelectedList;
    this.currentSelectedList = selectedList;
    this.displayCheckboxTree = this.displayCheckboxTree.bind(this);
    this.updateSelectedList = this.updateSelectedList.bind(this);
    this.updateExpandedList = this.updateExpandedList.bind(this);
    this.loadDefaultSelectedList = this.loadDefaultSelectedList.bind(this);
    this.loadCurrentSelectedList = this.loadCurrentSelectedList.bind(this);
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
    this.updateSelectedList(this.currentSelectedList);
  }
}