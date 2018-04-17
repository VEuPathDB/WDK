import React from 'react';

import Header from 'Ui/Header';

import Mesa from 'Mesa/Ui/Mesa';
import ConfigList from 'Content/ConfigList';

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
            actions={config.actions}
          />
        </main>
      </row>
    );
  }
};

export default Root;
