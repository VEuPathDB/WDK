import React from 'react';

type Props = {
  fa: string;
  className?: string;
  onClick?: (e: React.MouseEvent<HTMLElement>) => void;
}

export default function Icon(props: Props) {
  let { className, fa, onClick } = props;
  className = `fa fa-${fa} ${className || ''}`;
  let clickHandler = (onClick ? onClick : (e: React.MouseEvent<HTMLElement>) => {});
  return (
    <i className={className} onClick={onClick}> </i>
  );
}
