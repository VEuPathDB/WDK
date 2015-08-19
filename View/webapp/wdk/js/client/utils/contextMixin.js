import React from 'react';

export default {
  contextTypes: {
    store: React.PropTypes.shape({
      dispatch: React.PropTypes.func.isRequired,
      subscribe: React.PropTypes.func.isRequired,
      getState: React.PropTypes.func.isRequired
    }).isRequired
  }
};
