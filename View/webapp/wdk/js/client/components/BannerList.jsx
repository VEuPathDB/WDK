import React from 'react';
import { CSSTransitionGroup } from 'react-transition-group';

import Banner from './Banner';

class BannerList extends React.Component {
  constructor (props) {
    super(props);
  }

  onBannerClose (index) {
    const { banners, onClose } = this.props;
    if (onClose) onClose(index, banners[index]);
  }

  render () {
    const { banners } = this.props;

    const list = banners.map((banner, index) => (
      <Banner
        key={index}
        banner={banner}
        onClose={() => this.onBannerClose(index)}
      />
    ));

    return (
      <div className="wdk-BannerList">
        <CSSTransitionGroup
          transitionName="banner-list"
          transitionEnterTimeout={300}
          transitionLeaveTimeout={300}>
            {list}
        </CSSTransitionGroup>
      </div>
    )
  }
}

export default BannerList;
