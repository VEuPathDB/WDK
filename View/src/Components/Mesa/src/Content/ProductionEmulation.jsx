import React from 'react';

const ProductionEmulation = {
  dataSet: {
    primary: true,
    name: 'Data Set',
    width: '25%',
    renderCell (key, value) {
      return <a href="#">{value}</a>;
    }
  },
  organism: {
    type: 'html',
    name: 'Organism(s) (source or reference)'
  },
  category: {
    name: 'Category',
    sortable: true
  },
  description: {
    hidden: true,
    type: 'html',
    name: 'Description',
    width: '30%',
    truncated: true
  },
  released: 'Release # / Date',
  summary: 'Summary',
  releasePolicy: {
    hidden: true,
    name: 'Release Policy'
  },
  publications: {
    hidden: true,
    name: 'Publications',
    renderCell (key, value = [], row = {}) {
      return (
        <div>
          {value.map(({ id }, idx) => {
            let suffix = (idx === (value.length - 1) ? '' : ', ');
            return <a key={id} href={'#' + id}>{id}{suffix}</a>
          })}
        </div>
      );
    }
  },
  contact: {
    name: 'Contact',
    hidden: true
  },
  contactInstitution: 'Contact Institution'
};

export default ProductionEmulation;
