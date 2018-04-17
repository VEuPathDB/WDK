import React from 'react';

import Header from 'Ui/Header';

import Mesa from 'Mesa/Components/Mesa';
import TableData from 'Content/TableData';
import AlphaColumnSet from 'Content/AlphaColumnSet';
import BravoColumnSet from 'Content/BravoColumnSet';

const ConfigList = [
  { label: 'Quick Setup', config: AlphaColumnSet },
  { label: 'Prod Emulation', config: BravoColumnSet },
  { label: 'No Configuration ("auto")', config: null }
];

class Root extends React.Component {
  constructor (props) {
    super(props);
    this.state = {
      columnConfig: BravoColumnSet
    };
    this.changeConfig = this.changeConfig.bind(this);
    this.renderConfigMenu = this.renderConfigMenu.bind(this)
  }

  changeConfig (columnConfig) {
    this.setState({ columnConfig });
  }

  renderConfigMenu () {
    const { columnConfig } = this.state;
    const list = ConfigList.map(({ label, config }) => {
      const isActive = columnConfig === config;
      return (
        <box
          key={label}
          onClick={() => this.changeConfig(config)}
          className={'ConfigMenu-Item' + (isActive ? ' active' : '')}
        >
          {label}
        </box>
      );
    });

    return (
      <row className="ConfigMenu">
        {list}
      </row>
    );
  }

  render () {
    const { columnConfig } = this.state;
    return (
      <div className="Root">
        <Header />
        {this.renderConfigMenu()}
        <div style={{ padding: '30px' }}>
          <Mesa rows={TableData} columns={columnConfig} title="Data Sets" />
        </div>
      </div>
    );
  }
};

export default Root;
