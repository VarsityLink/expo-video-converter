import * as React from 'react';

import { ExpoVideoConverterViewProps } from './ExpoVideoConverter.types';

export default function ExpoVideoConverterView(props: ExpoVideoConverterViewProps) {
  return (
    <div>
      <span>{props.name}</span>
    </div>
  );
}
