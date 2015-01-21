import React from 'react';
import Router from 'react-router';
import { appRoutes } from './flux/router';
import { config as configService } from './flux/ServiceAPI';

/**
 * TODO Provide a more comprehensive configuration module. Possibly look into
 * dependency injection libraries (wire.js is one that looks good). This will
 * make it much easier to manage the needs of WDK client consumers.
 */
var wdk = window.wdk = {
  config(spec) {
    configService({ serviceUrl: spec.serviceUrl });
  },

  run(spec) {
    if (spec) {
      this.config(spec);
    }
    Router.run(appRoutes, (Handler, state) => React.render(
      <Handler {...state}/>, document.body
    ));
  }
};

export default wdk;
