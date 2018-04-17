import React from 'react';

import Header from 'Ui/Header';

import Mesa from 'Mesa/Components/Mesa';
import TableData from 'Content/TableData';
import CustomSetup from 'Content/CustomSetup';
import ProductionEmulation from 'Content/ProductionEmulation';

const embarrassment = { fontFamily: 'Comic Sans, Comic Sans MS, Papyrus', color: 'blue' };

const ConfigList = [
  {
    label: 'Production Emulation',
    config: ProductionEmulation,
    title: 'Data Sets'
  },
  {
    label: 'Custom Setup',
    config: CustomSetup,
    title: <span style={embarrassment}>Cool Internet ! (｡◕‿◕｡)</span>
  },
  {
    label: 'No Configuration / Auto Mode',
    config: null,
    title: null
  }
];

class Root extends React.Component {
  constructor (props) {
    super(props);
    let [ initial, ...others ] = ConfigList;
    this.state = {
      tableTitle: initial.title,
      columnConfig: initial.config
    };
    this.changeConfig = this.changeConfig.bind(this);
    this.renderConfigMenu = this.renderConfigMenu.bind(this)
  }

  changeConfig (columnConfig, tableTitle) {
    this.setState({ columnConfig, tableTitle });
  }

  renderConfigMenu () {
    const { columnConfig } = this.state;
    const list = ConfigList.map(({ label, config, title }) => {
      const isActive = columnConfig === config;
      return (
        <box
          key={label}
          onClick={() => this.changeConfig(config, title)}
          className={'ConfigMenu-Item' + (isActive ? ' active' : '')}
        >
          {label}
        </box>
      );
    });

    return <grid className="ConfigMenu">{list}</grid>
  }

  render () {
    const { columnConfig, tableTitle } = this.state;
    return (
      <div className="Root">
        <Header />
        {this.renderConfigMenu()}
        <div style={{ padding: '30px' }}>
          <Mesa rows={TableData} columns={columnConfig} title={tableTitle} />
        </div>
      </div>
    );
  }
};

export default Root;
