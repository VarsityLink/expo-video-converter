import React, { useState } from "react";
import { Button, Text, View } from "react-native";
import * as ImagePicker from "expo-image-picker";
import * as ExpoVideoConverter from "expo-video-converter";
import RNFS, { stat } from "react-native-fs";

export default function App() {
  // const [image, setImage] = useState<null | string>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [convertedRes, setConvertedRes] = useState<string>("");

  const pickImage = async () => {
    // No permissions request is necessary for launching the image library
    let result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.All,
      quality: 1,
    });

    if (!result.canceled) {
      const outputPath =
        RNFS.TemporaryDirectoryPath +
        `/convertedFile_${new Date().getTime()}.mp4`;

      const conversionOptions = {
        id: 25,
        sourcePath: result.assets[0].uri.replace("file://", ""),
        outputPath: outputPath,
        width: 390,
        height: 640,
        frameRate: 30,
        videoBitrate: 5000000,
        audioBitrate: 96000,
        audioChannels: 2,
        trimStart: 0,
        trimEnd: 60,
      };

      setLoading(true);
      const res = await ExpoVideoConverter.convert(conversionOptions);

      if (!res?.uri) {
        setLoading(false);
        setConvertedRes("Error");

        return;
      }

      const statResult = await stat(res.uri);
      setConvertedRes(JSON.stringify(statResult));
      setLoading(false);
    }
  };

  return (
    <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
      <Button title="Pick a video from camera roll" onPress={pickImage} />
      {loading && <Text>Loading...</Text>}
      <Text>{convertedRes}</Text>
    </View>
  );
}
