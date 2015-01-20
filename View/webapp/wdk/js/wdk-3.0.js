import React from 'react';
import Router from 'react-router';
import { appRoutes } from './flux/router';
import { config as configService } from './flux/ServiceAPI';

var wdk = window.wdk = {
  config(spec) {
    configService({ serviceUrl: spec.serviceUrl });
  },

  run(spec) {
    if (spec) {
      this.config(spec);
    }
    Router.run(appRoutes, Handler => React.render(<Handler/>, document.body));
  }
};

export default wdk;
