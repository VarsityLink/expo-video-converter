import { StyleSheet, Text, View } from 'react-native';

import * as ExpoVideoConverter from 'expo-video-converter';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>{ExpoVideoConverter.hello()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
