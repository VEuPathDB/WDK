import React from 'react';

import Icon from 'Mesa/Components/Icon';
import Datasets from 'Content/Data/Datasets';

const ExtraData = {
  label: 'Extra Data / Inline / Actions',
  rows: [
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets
  ],
  options: {
    title: '10x Regular Data!',
    inline: true,
    paginate: true,
    inlineMaxWidth: '300px',
    columnDefaults: {
      truncated: false,
      overflowHeight: '2rem'
    }
  },
  actions: [
    {
      element: <small><a>Click Me, As Well</a></small>,
      callback (rows) {
        let message = 'An action\'s "element" doesn\'t need to be a button, per se.';
        message += '\nBy the way, you ' + (rows.length ? 'selected ' + rows.length + ' rows.' : 'haven\'t selected any rows.');
        alert(message)
      }
    },
    {
      element: (selection) => {
        return <button><span>Example Action for {selection.length} rows</span> <Icon fa="magic" /></button>
      },
      handler (row) {
        alert('one row has the category of '+ row.category);
      },
      callback (rows) {
        alert('there are ' + rows.length + ' rows');
      },
      selectionRequired: true
    },
    {
      element: <button><span>This button doesn't really do much</span> <Icon fa="meh-o" /></button>,
      callback () {
        alert('Hi!');
      }
    }
  ]
};

export default ExtraData;
