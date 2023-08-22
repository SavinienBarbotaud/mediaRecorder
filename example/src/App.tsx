import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import MediaRecorder from 'mediarecorder';

export default function App() {

  var mediaRecorder: MediaRecorder;

  function init() {
    mediaRecorder = new MediaRecorder();
    mediaRecorder.initMediaRecorder()
      .then(() => {
        console.info('App.tsx -> mediaRecorder is setup');
      })
      .catch((e) => {
        console.error('error : ', e);
      });
  }

  function start() {
    mediaRecorder.start().then(() => {
      console.info('start');
    });
  }

  function stop() {
    mediaRecorder.stop().then(() => {
      console.info('stop');
    });
  }
  return (
    <View style={styles.container}>
      <Button title="init" onPress={init}>
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
