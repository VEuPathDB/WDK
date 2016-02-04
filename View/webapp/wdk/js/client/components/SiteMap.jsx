import React from 'react';
import { wrappable } from '../utils/componentUtils';

let SiteMap = React.createClass({

  render() {

   

    return (
      <div style={{ margin: "0 2em"}}>
        <h1>Hello World, this is a Site Map</h1>
      </div>
    );
  }
});

export default wrappable(SiteMap);
