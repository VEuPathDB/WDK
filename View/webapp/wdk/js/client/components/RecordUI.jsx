import {Component} from 'react';
import classnames from 'classnames';
import {wrappable} from '../utils/componentUtils';
import Main from './Main';
import Record from './Record';
import RecordHeading from './RecordHeading';
import RecordNavigationSection from './RecordNavigationSection';
import Sticky from './Sticky';

/**
 * Renders the main UI for the WDK Record page.
 */
class RecordUI extends Component {

  constructor(props) {
    super(props);
    this.state = { showSidebar: true };
    this.toggleSidebar = event => {
      event.preventDefault();
      this.setState({ showSidebar: !this.state.showSidebar });
    }
  }

  componentDidMount() {
    let { hash } = window.location;
    let target = document.getElementById(hash.slice(1));
    if (target != null) {
      target.scrollIntoView();
    }
  }

  render() {
    let classNames = classnames(
      'wdk-RecordContainer',
      'wdk-RecordContainer__' + this.props.recordClass.name,
      {
        'wdk-RecordContainer__withSidebar': this.state.showSidebar,
        'wdk-RecordContainer__withAdvanced': this.state.showAdvanced
      }
    );

    let sidebarIconClass = classnames({
      'fa fa-lg': true,
      'fa-angle-double-down': !this.state.showSidebar,
      'fa-angle-double-up': this.state.showSidebar
    });

    return (
      <Main className={classNames}>
        <div
          className="wdk-RecordMainSectionFieldToggles"
          fixedClassName="wdk-RecordMainSectionFieldToggles__fixed"
        >
          <a href="#"
            onClick={event => {
              event.preventDefault();
              this.props.showAllFields();
            }}
            title="Show all content" >Show All</a>
          <br/>
          <a href="#"
            onClick={event => {
              event.preventDefault();
              this.props.hideAllFields();
            }}
            title="Hide all content" >Hide All</a>
        </div>
        <RecordHeading
          record={this.props.record}
          recordClass={this.props.recordClass}
          headerActions={this.props.headerActions}
        />
        <Sticky className="wdk-RecordSidebar" fixedClassName="wdk-RecordSidebar__fixed">
          <a href="#" className="wdk-RecordSidebarToggle" onClick={this.toggleSidebar}>
            {this.state.showSidebar ? '' : 'Show Contents '}
            <i className={sidebarIconClass}
              title={this.state.showSidebar ? 'Close sidebar' : 'Open sidebar'}/>
          </a>
          <RecordNavigationSection
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedSections={this.props.collapsedSections}
            onSectionToggle={this.props.toggleSection}
          />
        </Sticky>
        <div className="wdk-RecordMain">
          <Record
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedSections={this.props.collapsedSections}
            onSectionToggle={this.props.toggleSection}
          />
        </div>
      </Main>
    )
  }
}

export default wrappable(RecordUI);
