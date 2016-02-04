import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTree from '../client/components/CheckboxTree';


// serves as MVC controller for checkbox tree on results page
export default class CheckboxTreeController {

  constructor(element, name, tree, selectedList, expandedList, defaultSelectedList) {
    this.element = element;
    this.name = name;
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
      />, this.element[0]);
    console.log("Rendered checkbox " + this.name + " under element " + JSON.stringify(this.element));
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