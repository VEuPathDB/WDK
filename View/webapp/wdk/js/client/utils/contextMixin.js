import React from 'react';

export default {
  contextTypes: {
    router: React.PropTypes.func.isRequired,
    stores: React.PropTypes.object.isRequired,
    actions: React.PropTypes.object.isRequired
  }
};
