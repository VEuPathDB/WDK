import React from 'react';

import Header from 'Ui/Header';

import Mesa from 'Mesa/Ui/Mesa';
import TableData from 'Content/TableData';
import CustomSetup from 'Content/CustomSetup';
import ProductionEmulation from 'Content/ProductionEmulation';

const embarrassment = { fontFamily: 'Comic Sans, Comic Sans MS, Papyrus', color: 'blue' };

const ConfigList = [
  {
    label: 'Production Emulation',
    columns: ProductionEmulation,
    rows: TableData,
    options: {
      title: 'Data Sets'
    }
  },
  {
    label: 'Custom Setup',
    columns: CustomSetup,
    rows: TableData,
    options: {
      title: <span style={embarrassment}>Cool Internet ! (｡◕‿◕｡)</span>
    }
  },
  {
    label: 'No Configuration / Auto Mode',
    columns: null,
    rows: TableData,
    options: null
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

    return <grid className="ConfigMenu">{list}</grid>
  }

  render () {
    const { config } = this.state;
    return (
      <div className="Root">
        <Header />
        {this.renderConfigMenu()}
        <div style={{ padding: '30px' }}>
          <Mesa
            rows={config.rows}
            options={config.options}
            columns={config.columns}
          />
        </div>
      </div>
    );
  }
};

export default Root;
