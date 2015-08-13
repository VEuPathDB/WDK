import React from 'react';

export default {
  contextTypes: {
    dispatch: React.PropTypes.func.isRequired,
    subscribe: React.PropTypes.func.isRequired,
    state: React.PropTypes.object.isRequired,
    config: React.PropTypes.object.isRequired
  }
};
