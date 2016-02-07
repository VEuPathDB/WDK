import React from 'react';
import { wrappable } from '../utils/componentUtils';
import CheckboxTree from './CheckboxTree';

let SiteMap = React.createClass({

  render() {
    return (

      <CheckboxTree tree={this.props.tree.children}
                   selectedList={[]}
                   expandedList={this.props.expandedList}
                   name="SiteMapTree"
                   onSelectedListUpdated={()=>{}}
                   onExpandedListUpdated={this.props.siteMapActions.updateExpanded.bind(this.props.siteMapActions)}
                   onDefaultSelectedListLoaded={()=>{}}
                   onCurrentSelectedListLoaded={()=>{}}
     />
    );
  }
});

export default wrappable(SiteMap);
