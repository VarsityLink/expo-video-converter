import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { ExpoVideoConverterViewProps } from './ExpoVideoConverter.types';

const NativeView: React.ComponentType<ExpoVideoConverterViewProps> =
  requireNativeViewManager('ExpoVideoConverter');

export default function ExpoVideoConverterView(props: ExpoVideoConverterViewProps) {
  return <NativeView {...props} />;
}
