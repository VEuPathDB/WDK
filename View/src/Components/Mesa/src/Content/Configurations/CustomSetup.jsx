import React from 'react';

import Datasets from 'Content/Data/Datasets';
import Icon from 'Mesa/Components/Icon';

const bgImage = 'https://media.giphy.com/media/VxbvpfaTTo3le/giphy.gif';
const embarrassment = { fontFamily: 'Comic Sans, Comic Sans MS, Papyrus', color: 'blue' };
const fancy = {
  color: '#fff',
  fontWeight: 900,
  fontStyle: 'italic',
  fontSize: '0.8em',
  display: 'block',
  margin: '-40px -20px',
  padding: '20px',
  height: 'calc(100% + 50px)',
  width: 'calc(100% + 40px)',
  backgroundPosition: 'center 35%',
  backgroundImage: `url(${bgImage})`
};

const CustomSetupColumns = {
  dataSet: {
    primary: true,
    name: 'Data Set',
    style: { width: '20%' },
    renderCell (key, value) {
      let style = { fontWeight: 400, letterSpacing: 0 };
      return (
        <h2 style={style}>{value}</h2>
      )
    },
    renderHeading: () => {
      return (
        <div style={fancy}>
          DATA SET
        </div>
      );
    }
  },
  organism: {
    name: 'Organism',
    type: 'html',
    width: '150px'
  },
  released: { name: 'Released', width: '10%' },
  category: {
    name: 'Category',
    filterable: true
  },
  contact: {
    name: 'Contact',
    filterable: true,
    style: { fontWeight: 'bold' }
  },
  description: {
    type: 'html',
    name: 'Description',
    width: '50%',
    maxHeight: ''
  }
};

const CustomSetup = {
  label: 'Custom Setup with Filter',
  columns: CustomSetupColumns,
  rows: Datasets,
  options: {
    title: <span style={embarrassment}>Cool Internet ! (｡◕‿◕｡)</span>
  },
  actions: [
    {
      element: <button>Example Action <Icon fa="magic" /></button>,
      handler (row) {
        alert('one row has the category of '+ row.category);
      },
      callback (rows) {
        alert('there are ' + rows.length + 'rows');
      }
    }
  ]
};

export default CustomSetup;
