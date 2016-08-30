/**
 * Page wrapper used by view controllers.
 */
import React from 'react';
import {wrappable} from '../utils/componentUtils';
import Header from './Header';
import Footer from './Footer';

type Props = {
  children: React.ReactChild;
};

function Page(props: Props) {
  return (
    <div className="wdk-RootContainer">
      <Header/>
      <div className="wdk-PageContent">{props.children}</div>
      <Footer/>
    </div>
  );
}

export default wrappable(Page);
