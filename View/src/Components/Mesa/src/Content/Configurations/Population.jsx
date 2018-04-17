import React from 'react';

import Icon from 'Mesa/Components/Icon';
import PopulationByAge from 'Content/Data/PopulationByAge.json';

const genderCell = (key, value, row) => {
  let total = row.total;
  let icon = key === 'females' ? 'female' : 'male';
  let percent = Math.floor(((value / total) * 10000)) / 100;
  return (
    <div>
      <Icon fa={icon} /> &nbsp; {value.toLocaleString()} &nbsp;  / &nbsp; {percent}%
    </div>
  );
};

const Population = {
  label: 'US Population 1980 (paginated)',
  rows: PopulationByAge,
  columns: [
    { key: 'age', name: 'Age', primary: true, width: '100px' },
    { key: 'total', name: 'Total Population' },
    {
      key: 'males',
      name: 'Males',
      renderCell: genderCell
    },
    {
      key: 'females',
      name: 'Females',
      renderCell: genderCell
    }
  ],
  options: {
    paginate: true,
    columnDefaults: {
      style: {
        textAlign: 'center'
      },
      headingStyle: {
        textAlign: 'center'
      }
    }
  }
};

export default Population;
