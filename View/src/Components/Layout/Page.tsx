/**
 * Page wrapper used by view controllers.
 */
import React from 'react';
import {wrappable} from 'Utils/ComponentUtils';
import Header from 'Components/Layout/Header';
import Footer from 'Components/Layout/Footer';

type Props = {
  children: React.ReactChild | null;
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
