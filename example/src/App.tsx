import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import {
  initMediaRecorder,
  multiply,
  startRecord,
  stopRecord,
} from 'mediarecorder';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  function mediaRecorder() {
    initMediaRecorder()
      .then(() => {
        console.info('App.tsx -> mediaRecorder is setup');
      })
      .catch((e) => {
        console.error('error : ', e);
      });
  }

  function start() {
    startRecord().then(() => {
      console.info('start');
    });
  }

  function stop() {
    stopRecord().then(() => {
      console.info('stop');
    });
  }
  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button title="init" onPress={mediaRecorder}>
        Init
      </Button>
      <Button title="start" onPress={start}>
        Init
      </Button>
      <Button title="stop" onPress={stop}>
        Init
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
