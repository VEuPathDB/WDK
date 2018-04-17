import React from 'react';

import Datasets from 'Content/Data/Datasets';

const ProductionEmulationColumns = {
  dataSet: {
    primary: true,
    name: 'Data Set',
    info: 'Click a data set to go the full data set record',
    width: '25%',
    renderCell (key, value) {
      return <a href="#">{value}</a>;
    }
  },
  organism: {
    type: 'html',
    name: 'Organism(s) (source or reference)',
    info: 'the \'source\' is the organism used to generate the samples that were analyzed to produce this data set. The \'reference\' is the genome that the data were aligned to. For functional datasets, the source may differ from the reference.'
  },
  category: {
    name: 'Category',
    sortable: true,
    info: 'Datasets are assigned to categories based on the biological attributes of the data.'
  },
  description: {
    hidden: true,
    type: 'html',
    name: 'Description',
    width: '30%',
    truncated: true
  },
  released: {
    name: 'Release # / Date',
    info: 'The EuPathDB Release number and date that the data set first appeared in EuPathDB.'
  },
  summary: {
    name: 'Summary',
    info: 'A short description of the dataset.'
  },
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
    width: '10%',
    info: 'The person or organization that serves as a source of information about this dataset.'
  },
  contactInstitution: {
    name: 'Contact Institution',
    hidden: true
  }
};

const ProductionEmulation = {
  label: 'Production Emulation',
  columns: ProductionEmulationColumns,
  rows: Datasets,
  options: {
    title: 'Data Sets'
  }
};

export default ProductionEmulation;
