import React from 'react';
import { wrappable } from '../utils/componentUtils';
import CheckboxTree from './CheckboxTree';

let SiteMap = React.createClass({

  render() {
    return (

      <CheckboxTree tree={this.props.tree}
                   selectedList={[]}
                   expandedList={this.props.expandedList}
                   name="SiteMapTree"
                   onSelectedListUpdated={()=>{}}
                   onExpandedListUpdated={this.props.siteMapActions.updateExpanded}
                   onDefaultSelectedListLoaded={()=>{}}
                   onCurrentSelectedListLoaded={()=>{}}
     />
    );
  }
});

export default wrappable(SiteMap);
