// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import { wrapActions } from '../utils/actionHelpers';
import Doc from '../components/Doc';
import Loading from '../components/Loading';
import SiteMap from '../components/SiteMap';
import * as SiteMapActionCreator from '../actioncreators/SiteMapActionCreator';

let SiteMapController = React.createClass({

 componentWillMount() {
    this.siteMapStore = this.props.stores.SiteMapStore;
    this.siteMapActions = wrapActions(this.props.dispatchAction, SiteMapActionCreator);
    this.setState(this.siteMapStore.getState());
    this.siteMapStoreSubscription = this.siteMapStore.addListener(() => {
        this.setState(this.siteMapStore.getState());
    });
  },

  componentDidMount() {
    if (this.state.tree == null) {
      this.siteMapActions.loadCurrentSiteMap();
    }
  },

  componentWillUnmount() {
    this.siteMapStoreSubscription.remove();
  },

  render() {
    let title = "Data Finder";
    if (this.state.isLoading) {
      return ( <Doc title={title}><Loading/></Doc> );
    }

    return (
      <Doc title={title}>
        <h1>Data Finder</h1>
        <p>
          Use this tool to find searches, tracks and data pages that might
          contain data you are interested in.
        </p>
        <SiteMap {...this.state} siteMapActions={this.siteMapActions}/>
      </Doc>
    );
  }
});

// Export the React Component class we just created.
export default wrappable(SiteMapController);
