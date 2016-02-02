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

    this.toggleCategory = this.toggleCategory.bind(this);
    this.toggleTable = this.toggleTable.bind(this);
    this.toggleSidebar = this.toggleSidebar.bind(this);
  }

  toggleCategory(category, isCollapsed) {
    this.props.actions.toggleCategoryCollapsed(
      this.props.recordClass.name,
      getPropertyValue('label', category),
      isCollapsed
    );
  }

  toggleTable(table, isCollapsed) {
    this.props.actions.toggleTableCollapsed(
      this.props.recordClass.name,
      table.name,
      isCollapsed
    );
  }

  toggleSidebar(event) {
    event.preventDefault();
    this.setState({ showSidebar: !this.state.showSidebar });
  }

  render() {
    let classNames = classnames({
      'wdk-RecordContainer': true,
      'wdk-RecordContainer__withSidebar': this.state.showSidebar,
      'wdk-RecordContainer__withAdvanced': this.state.showAdvanced
    });

    let sidebarIconClass = classnames({
      'fa fa-lg': true,
      'fa-angle-double-down': !this.state.showSidebar,
      'fa-angle-double-up': this.state.showSidebar
    });

    return (
      <Main className={classNames}>
        <RecordHeading record={this.props.record} recordClass={this.props.recordClass}/>
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
            collapsedCategories={this.props.collapsedCategories}
            categoryWordsMap={this.props.categoryWordsMap}
            onCategoryToggle={this.toggleCategory}
          />
        </Sticky>
        <div className="wdk-RecordMain">
          <Record
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedCategories={this.props.collapsedCategories}
            collapsedTables={this.props.collapsedTables}
            onCategoryToggle={this.toggleCategory}
            onTableToggle={this.toggleTable}
          />
        </div>
      </Main>
    )
  }
}

RecordUI.propTypes = {
  record: PropTypes.object.isRequired,
  recordClass: PropTypes.object.isRequired,
  collapsedCategories: PropTypes.array.isRequired,
  collapsedTables: PropTypes.array.isRequired,
  actions: PropTypes.object.isRequired
};



export default wrappable(RecordUI);
