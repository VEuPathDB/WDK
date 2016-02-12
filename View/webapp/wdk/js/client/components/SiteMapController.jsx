// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import SiteMap from './SiteMap';

let SiteMapController = React.createClass({

 componentWillMount() {
    this.siteMapStore = this.props.stores.SiteMapStore;
    this.siteMapActions = this.props.actionCreators.SiteMapActionCreator;
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
    let title = "Site Map";
    if (this.state.isLoading) {
      return ( <Doc title={title}><Loading/></Doc> );
    }

    return ( <Doc title={title}><SiteMap {...this.state} siteMapActions={this.siteMapActions}/></Doc> );
  }
});

// Export the React Component class we just created.
export default wrappable(SiteMapController);
