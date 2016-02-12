import React from 'react';
import { wrappable } from '../utils/componentUtils';
import CheckboxTree from './CheckboxTree';
import { getTargetType, getRefName, getDisplayName, getDescription, getId } from '../utils/OntologyUtils';

let SiteMap = React.createClass({

getNodeData(node) {
  let data = {};
  let targetType = getTargetType(node);
  if (node.children.length == 0) {
     data.id = data.displayName = data.description = getRefName(node);
  }
  else {
    data.id = getId(node);
    data.displayName = getDisplayName(node);
    data.description = getDescription(node);
  }
  return data;
},

getNodeFormValue(node) {
  return this.getNodeData(node).id
},


getNodeReactElement(node) {
  let data = this.getNodeData(node);
  return <span title={data.description}>{data.displayName}</span>
},


getNodeChildren(node) {
  return node.children;
},



  render() {
    return (

      <CheckboxTree tree={this.props.tree}
                   selectedList={[]}
                   expandedList={this.props.expandedList}
                   name="SiteMapTree"
                   onSelectedListUpdated={()=>{}}
                   onExpandedListUpdated={this.props.siteMapActions.updateExpanded.bind(this.props.siteMapActions)}
                   onDefaultSelectedListLoaded={()=>{}}
                   onCurrentSelectedListLoaded={()=>{}}
                   getNodeReactElement={this.getNodeReactElement}
                   getNodeFormValue={this.getNodeFormValue}
                   getNodeChildren={this.getNodeChildren}

     />
    );
  }
});

export default wrappable(SiteMap);
