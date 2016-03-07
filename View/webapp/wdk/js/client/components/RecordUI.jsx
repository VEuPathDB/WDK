import { Component, PropTypes } from 'react';
import classnames from 'classnames';
import { wrappable } from '../utils/componentUtils';
import { getPropertyValue } from '../utils/OntologyUtils';
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
    this.state = {
      showSidebar: true
    };

    this.toggleSection = this.toggleSection.bind(this);
    this.toggleSidebar = this.toggleSidebar.bind(this);
  }

  componentDidMount() {
    let { hash } = window.location;
    let target = document.getElementById(hash.slice(1));
    if (target != null) {
      target.scrollIntoView();
    }
  }

  toggleSection(sectionName, isCollapsed) {
    this.props.recordActions.updateSectionCollapsed(
      this.props.recordClass.name,
      sectionName,
      isCollapsed
    );
  }

  toggleSidebar(event) {
    event.preventDefault();
    this.setState({ showSidebar: !this.state.showSidebar });
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
        <RecordHeading
          record={this.props.record}
          recordClass={this.props.recordClass}
          user={this.props.user}
          basket={this.props.baskets[this.props.recordClass.name][JSON.stringify(this.props.record.id)]}
          userActions={this.props.userActions}
          router={this.props.router}
        />
        <Sticky className="wdk-RecordSidebar" fixedClassName="wdk-RecordSidebar__fixed">
          {/*<h3 className="wdk-RecordSidebarHeader">{this.props.record.displayName}</h3>*/}
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
            onSectionToggle={this.toggleSection}
          />
        </Sticky>
        <div className="wdk-RecordMain">
          <Record
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedSections={this.props.collapsedSections}
            onSectionToggle={this.toggleSection}
          />
        </div>
      </Main>
    )
  }
}

RecordUI.propTypes = {
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  collapsedSections: PropTypes.array.isRequired,
  user: PropTypes.object.isRequired,
  baskets: PropTypes.object.isRequired,
  recordActions: PropTypes.object.isRequired,
  userActions: PropTypes.object.isRequired
};



export default wrappable(RecordUI);
