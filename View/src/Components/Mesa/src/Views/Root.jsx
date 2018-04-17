import React from 'react';

import Header from 'Ui/Header';

import Mesa from 'Mesa/Ui/Mesa';
import TableData from 'Content/TableData';
import CustomSetup from 'Content/CustomSetup';
import Icon from 'Mesa/Components/Icon';
import ProductionEmulation from 'Content/ProductionEmulation';

import PopulationByAge from 'Content/PopulationByAge.json';

const genderCell = (key, value, row) => {
  let total = row.total;
  let icon = key === 'females' ? 'female' : 'male';
  let percent = Math.floor(((value / total) * 100) * 100) / 100;
  return (
    <div>
      <Icon fa={icon} />
      &nbsp; {value.toLocaleString()} &nbsp;  / &nbsp; {percent}%
    </div>
  );
};

const embarrassment = { fontFamily: 'Comic Sans, Comic Sans MS, Papyrus', color: 'blue' };

const ConfigList = [
  {
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
  },
  {
    label: 'Extra Data + Inline Mode!',
    rows: [
      ...TableData, ...TableData,
      ...TableData, ...TableData,
      ...TableData, ...TableData,
      ...TableData, ...TableData,
      ...TableData, ...TableData
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
    }
  },
  {
    label: 'No Data',
    columns: null,
    rows: [],
    options: null
  },
  {
    label: 'Production Emulation',
    columns: ProductionEmulation,
    rows: TableData,
    options: {
      title: 'Data Sets'
    }
  },
  {
    label: 'Custom Setup with Filter',
    columns: CustomSetup,
    rows: TableData,
    options: {
      title: <span style={embarrassment}>Cool Internet ! (｡◕‿◕｡)</span>
    }
  },
  {
    label: 'No Configuration / Auto Mode',
    rows: TableData
  }
];

class Root extends React.Component {
  constructor (props) {
    super(props);
    let [ initial, ...others ] = ConfigList;
    this.state = { config: initial };

    this.changeConfig = this.changeConfig.bind(this);
    this.renderConfigMenu = this.renderConfigMenu.bind(this)
  }

  changeConfig (config) {
    this.setState({ config });
  }

  renderConfigMenu () {
    const active = this.state.config;
    const list = ConfigList.map(config => {
      const isActive = config === active;
      return (
        <box
          key={config.label}
          onClick={() => this.changeConfig(config)}
          className={'ConfigMenu-Item' + (isActive ? ' active' : '')}
        >
          {config.label}
        </box>
      );
    });

    return <stack className="ConfigMenu">{list}</stack>
  }

  render () {
    const { config } = this.state;
    return (
      <row className="Root">
        <aside>
          <Header />
          {this.renderConfigMenu()}
        </aside>
        <main>
          <Mesa
            rows={config.rows}
            options={config.options}
            columns={config.columns}
          />
        </main>
      </row>
    );
  }
};

export default Root;
