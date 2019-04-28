import React, { useEffect, useState } from "react";
import { Button, Text, View } from "react-native";
import RNStoryShare from "react-native-story-share";

const base64Background =
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAQAAAAHUWYVAAABKUlEQVR42u3RMQEAAAjDMOZf9DDBwZFKaNKOHhUgQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQICYAERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABAQIECACAkRAgAgIEAEBIiACAkRAgAgIEAEBIiACAkRAgOiyBdIBj0hI3a/GAAAAAElFTkSuQmCC";
const base64Sticker =
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAABn0lEQVR42u3TAQ0AAAjDMO5fNCAABaTNLCxdtQGXGAQMAgYBg4BBwCBgEDAIGAQMAhgEDAIGAYOAQcAgYBAwCBgEDGIQMAgYBAwCBgGDgEHAIGAQMAhgEDAIGAQMAgYBg4BBwCBgEDAIYBAwCBgEDAIGAYOAQcAgYBDAIGAQMAgYBAwCBgGDgEHAIGAQwCBgEDAIGAQMAgYBg4BBwCCAQcAgYBAwCBgEDAIGAYOAQcAggEHAIGAQMAgYBAwCBgGDgEHAIAYBg4BBwCBgEDAIGAQMAgYBgwAGAYOAQcAgYBAwCBgEDAIGAYMYBAwCBgGDgEHAIGAQMAgYBAwCGAQMAgYBg4BBwCBgEDAIGAQMAhgEDAIGAYOAQcAgYBAwCBgEMAgYBAwCBgGDgEHAIGAQMAgYBDAIGAQMAgYBg4BBwCBgEDAIYBAwCBgEDAIGAYOAQcAgYBAwCGAQMAgYBAwCBgGDgEHAIGAQMIhBwCBgEDAIGAQMAgYBg4BBwCCAQcAgYBAwCBgEDAIGAYOAQcAgBgGDgEHAIGAQMAgYBAwCHw2n4Y9IKdr/IQAAAABJRU5ErkJggg==";

export const App = () => {
  const [isInstagramAvailable, setIsInstagramAvailable] = useState(false);
  const [error, setError] = useState(null);

  const onPressShareInstagram = () => {
    setError(null);

    RNStoryShare.shareToInstagram({
      backgroundAsset: base64Background,
      stickerAsset: base64Sticker,
      attributionLink: "",
      type: RNStoryShare.BASE64
    }).catch(setError);
  };

  const onPressShareSnapchat = () => {
    setError(null);

    RNStoryShare.shareToSnapchat({
      backgroundAsset: base64Background,
      stickerAsset: base64Sticker,
      attributionLink: "",
      type: RNStoryShare.BASE64
    }).catch(setError);
  };

  useEffect(() => {
    RNStoryShare.isInstagramAvailable()
      .then(setIsInstagramAvailable)
      .catch(e => console.error(e));
  }, []);

  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <Text style={{ fontWeight: "bold", fontSize: 24, marginBottom: 24 }}>
        RNStoryShare Example
      </Text>
      <Text style={{ marginBottom: 24 }}>
        Instagram is{" "}
        <Text style={{ fontWeight: "bold" }}>
          {isInstagramAvailable ? "available" : "not available"}
        </Text>
      </Text>
      <Button title="Share to Instagram" onPress={onPressShareInstagram} />
      <Button
        title="Share to Snapchat"
        onPress={onPressShareSnapchat}
        style={{ marginTop: 24 }}
      />
      <Text style={{ marginTop: 48 }}>
        {error ? `error: ${error.message}` : null}
      </Text>
    </View>
  );
};
