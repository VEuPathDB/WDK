// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import SiteMap from './SiteMap';

let SiteMapController = React.createClass({

  render() {
    let title = "Site Map";
    return ( <Doc title={title}><SiteMap/></Doc> );
  }
});

// Export the React Component class we just created.
export default wrappable(SiteMapController);
