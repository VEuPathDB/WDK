/**
 * Rendered whenever a URL does not match a route
 */
import React from 'react';

var NotFoundController = React.createClass({

  render() {
    return (
      <div className="wdkNotFoundPage">
        <h1>Page not found</h1>
        <p>The page you are looking for doesn't appear to exist.</p>
      </div>
    );
  }

});

export default NotFoundController;
