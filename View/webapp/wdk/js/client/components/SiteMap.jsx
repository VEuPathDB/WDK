import React from 'react';
import { wrappable } from '../utils/componentUtils';

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
