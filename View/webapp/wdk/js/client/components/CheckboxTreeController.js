import React from 'react';
import ReactDOM from 'react-dom';
import CheckboxTree from './CheckboxTree';
import {isLeafNode} from '../utils/TreeUtils';
import {getLeaves} from '../utils/TreeUtils';
import {getNodeById} from '../utils/TreeUtils';


// serves as view controller for checkbox tree React component
export default class CheckboxTreeController extends React.Component {

  constructor(props) {
    super(props);
    this.initialData = props.initialData;
    this.store = props.store;
    this.actions = props.actions;
    this.state = this.store.getState();
  }

  // set up store listener and load up a properly initialized checkbox tree
  componentWillMount() {
    this.storeSubscription = this.store.addListener(function () {
      this.setState(this.store.getState());
    }.bind(this));

    this.actions.loadCheckboxTree({"name":this.initialData.name,
                                   "tree": this.initialData.tree,
                                   "selected": this.initialData.selected,
                                   "expanded": this.initialData.expanded
    });
  }

  // remove the store listener upon unmounting the component.
  componentWillUnmount() {
    this.storeSubscription.remove();
  }


  // render wraps the top-level component, passing latest state
  render() {
    let data = this.store.getState();
    return (
      <div className="wdk-CheckboxTree" id={data.name}>
        <CheckboxTree tree={data.tree}
                      key="Root"
                      selectedList={data.selectedList}
                      expandedList={data.expandedList}
                      updateSelectedListAction={this.actions.updateSelectedList}
                      updateExpandedListAction={this.actions.updateExpandedList}
                      root={true}
        />
      </div>
    )
  }
}