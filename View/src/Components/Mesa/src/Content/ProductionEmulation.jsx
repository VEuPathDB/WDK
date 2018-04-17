import React from 'react';

const ProductionEmulation = {
  dataSet: {
    primary: true,
    name: 'Data Set',
    width: '25%'
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
    type: 'html',
    name: 'Description',
    width: '30%',
    truncated: true
  },
  released: 'Release # / Date',
  summary: 'Summary',
  releasePolicy: {
    name: 'Release Policy'
  },
  publications: {
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
  contact: 'Contact',
  contactInstitution: 'Contact Institution'
};

export default ProductionEmulation;
