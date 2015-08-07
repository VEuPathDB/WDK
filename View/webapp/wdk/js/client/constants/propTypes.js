import React from 'react';

let { PropTypes } = React;

export let AttributePropType = PropTypes.shape({
  name: PropTypes.string.isRequired,
  displayName: PropTypes.string.isRequired,
  value: PropTypes.any,
  category: PropTypes.string,
  help: PropTypes.string,
  align: PropTypes.string,
  type: PropTypes.string
});
