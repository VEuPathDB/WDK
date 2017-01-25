import {Component} from 'react';
import {debounce, get, throttle} from 'lodash';
import classnames from 'classnames';
import {wrappable} from '../utils/componentUtils';
import {postorderSeq} from '../utils/TreeUtils';
import {getId} from '../utils/CategoryUtils';
import RecordMainSection from './RecordMainSection';
import RecordHeading from './RecordHeading';
import RecordNavigationSection from './RecordNavigationSection';
import Sticky from './Sticky';

/**
 * Renders the main UI for the WDK Record page.
 */
class RecordUI extends Component {

  constructor(props) {
    super(props);
    // bind event handlers
    this._updateActiveSection = debounce(this._updateActiveSection.bind(this), 100);
    this._scrollToActiveSection = throttle(this._scrollToActiveSection.bind(this), 250);
    this.monitorActiveSection = debounce(this.monitorActiveSection.bind(this), 100);
  }

  componentDidMount() {
    // this.monitorActiveSection();
    window.addEventListener('resize', this._scrollToActiveSection, { passive: true });
    this._scrollToActiveSection();
  }

  componentDidUpdate(prevProps) {
    let navVisibilityChanged = prevProps.navigationVisible !== this.props.navigationVisible;
    let recordChanged = prevProps.record !== this.props.record;
    if (navVisibilityChanged || recordChanged) {
      this._scrollToActiveSection();
    }
  }

  componentWillUnmount() {
    this.unmonitorActiveSection();
    window.removeEventListener('resize', this._scrollToActiveSection, { passive: true });
    this._updateActiveSection.cancel();
    this._scrollToActiveSection.cancel();
    this.monitorActiveSection.cancel();
  }

  monitorActiveSection() {
    window.addEventListener('scroll', this._updateActiveSection, { passive: true });
  }

  unmonitorActiveSection() {
    window.removeEventListener('scroll', this._updateActiveSection, { passive: true });
  }

  _updateActiveSection() {
    let activeElement = postorderSeq(this.props.categoryTree)
    .map(node => document.getElementById(getId(node)))
    .filter(el => el != null)
    .find(el => {
      let rect = el.getBoundingClientRect();
      return rect.top <= 50 && rect.bottom > 50;
    });
    let activeSection = get(activeElement, 'id');
    let newUrl = location.pathname + location.search + (activeSection ? '#' + activeSection : '');
    history.replaceState(null, null, newUrl);
  }

  _scrollToActiveSection() {
    this.unmonitorActiveSection();
    let domNode = document.getElementById(location.hash.slice(1));
    if (domNode != null) {
      domNode.scrollIntoView(true);
    }
    this.monitorActiveSection();
  }

  render() {
    let classNames = classnames(
      'wdk-RecordContainer',
      'wdk-RecordContainer__' + this.props.recordClass.name,
      {
        'wdk-RecordContainer__withSidebar': this.props.navigationVisible      }
    );

    let sidebarIconClass = classnames({
      'fa fa-lg': true,
      'fa-angle-double-down': !this.props.navigationVisible,
      'fa-angle-double-up': this.props.navigationVisible
    });

    return (
      <div className={classNames}>
        <RecordHeading
          record={this.props.record}
          recordClass={this.props.recordClass}
          headerActions={this.props.headerActions}
        />
        <Sticky className="wdk-RecordSidebar" fixedClassName="wdk-RecordSidebar__fixed">
          <button type="button" className="wdk-RecordSidebarToggle"
            onClick={() => {
              if (!this.props.navigationVisible) window.scrollTo(0, window.scrollY);
              this.props.updateNavigationVisibility(!this.props.navigationVisible);
            }}
          >
            {this.props.navigationVisible ? '' : 'Show Contents '}
            <i className={sidebarIconClass}
              title={this.props.navigationVisible ? 'Close sidebar' : 'Open sidebar'}/>
          </button>
          <RecordNavigationSection
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedSections={this.props.collapsedSections}
            activeSection={this.props.activeSection}
            navigationQuery={this.props.navigationQuery}
            navigationExpanded={this.props.navigationExpanded}
            navigationCategoriesExpanded={this.props.navigationCategoriesExpanded}
            onSectionToggle={this.props.updateSectionVisibility}
            onNavigationVisibilityChange={this.props.updateNavigationVisibility}
            onNavigationCategoryExpansionChange={this.props.updateNavigationCategoryExpansion}
            onNavigationQueryChange={this.props.updateNavigationQuery}
          />
        </Sticky>
        <div className="wdk-RecordMain">
          <div className="wdk-RecordMainSectionFieldToggles">
            <button type="button" title="Expand all content" className="wdk-Link"
              onClick={this.props.updateAllFieldVisibility.bind(null, true)}>Expand All</button>
            {' | '}
            <button type="button" title="Collapse all content" className="wdk-Link"
              onClick={this.props.updateAllFieldVisibility.bind(null, false)}>Collapse All</button>
          </div>
          <RecordMainSection
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.categoryTree.children}
            collapsedSections={this.props.collapsedSections}
            onSectionToggle={this.props.updateSectionVisibility}
          />
        </div>
      </div>
    )
  }
}

export default wrappable(RecordUI);
