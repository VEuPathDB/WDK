import { Component } from 'react';
import { wrappable } from '../utils/componentUtils';

class Main extends Component {

  render() {
    return <div className={this.props.className}>
      {this.props.children}
    </div>
  }

}

export default wrappable(Main);
