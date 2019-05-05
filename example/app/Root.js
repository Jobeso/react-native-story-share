import React, { useEffect, useState } from 'react'
import { Text, View } from 'react-native'
import RNStoryShare from 'react-native-story-share'
import { Button } from './Button'
import { ShareSection } from './ShareSection'

const base64Background =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAQAAAAHUWYVAAABKUlEQVR42u3RMQEAAAjDMOZf9DDBwZFKaNKOHhUgQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQAQEiIAAERAgAgJEQICYAERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABERAgAgIEAEBIiBABAQIECACAkRAgAgIEAEBIiACAkRAgAgIEAEBIiACAkRAgOiyBdIBj0hI3a/GAAAAAElFTkSuQmCC'
const base64Sticker =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAABn0lEQVR42u3TAQ0AAAjDMO5fNCAABaTNLCxdtQGXGAQMAgYBg4BBwCBgEDAIGAQMAhgEDAIGAYOAQcAgYBAwCBgEDGIQMAgYBAwCBgGDgEHAIGAQMAhgEDAIGAQMAgYBg4BBwCBgEDAIYBAwCBgEDAIGAYOAQcAgYBDAIGAQMAgYBAwCBgGDgEHAIGAQwCBgEDAIGAQMAgYBg4BBwCCAQcAgYBAwCBgEDAIGAYOAQcAggEHAIGAQMAgYBAwCBgGDgEHAIAYBg4BBwCBgEDAIGAQMAgYBgwAGAYOAQcAgYBAwCBgEDAIGAYMYBAwCBgGDgEHAIGAQMAgYBAwCGAQMAgYBg4BBwCBgEDAIGAQMAhgEDAIGAYOAQcAgYBAwCBgEMAgYBAwCBgGDgEHAIGAQMAgYBDAIGAQMAgYBg4BBwCBgEDAIYBAwCBgEDAIGAYOAQcAgYBAwCGAQMAgYBAwCBgGDgEHAIGAQMIhBwCBgEDAIGAQMAgYBg4BBwCCAQcAgYBAwCBgEDAIGAYOAQcAgBgGDgEHAIGAQMAgYBAwCHw2n4Y9IKdr/IQAAAABJRU5ErkJggg=='

const shareInstagram = (onError, backgroundAsset, stickerAsset) => () => {
  RNStoryShare.shareToInstagram({
    backgroundAsset,
    stickerAsset,
    attributionLink: 'https://github.com/Jobeso/react-native-story-share',
    type: RNStoryShare.BASE64,
  }).catch(onError)
}

const shareSnapchat = (onError, backgroundAsset, stickerAsset) => () => {
  RNStoryShare.shareToSnapchat({
    backgroundAsset,
    stickerAsset,
    attributionLink: 'https://github.com/Jobeso/react-native-story-share',
    type: RNStoryShare.BASE64,
  }).catch(onError)
}

export const Root = () => {
  const [isInstagramAvailable, setIsInstagramAvailable] = useState(false)
  const [isSnapchatAvailable, setIsSnapchatAvailable] = useState(false)
  const [error, setError] = useState(null)

  const shareInstagramBackground = shareInstagram(setError, base64Background)
  const shareInstagramSticker = shareInstagram(
    setError,
    undefined,
    base64Sticker
  )
  const shareInstagramBoth = shareInstagram(
    setError,
    base64Background,
    base64Sticker
  )
  const shareSnapchatBackground = shareSnapchat(setError, base64Background)
  const shareSnapchatSticker = shareSnapchat(setError, undefined, base64Sticker)
  const shareSnapchatBoth = shareSnapchat(
    setError,
    base64Background,
    base64Sticker
  )

  useEffect(() => {
    const checkAvailability = async () => {
      try {
        const _isInstagramAvailable = await RNStoryShare.isInstagramAvailable()
        const _isSnapchatAvailable = await RNStoryShare.isSnapchatAvailable()

        setIsInstagramAvailable(_isInstagramAvailable)
        setIsSnapchatAvailable(_isSnapchatAvailable)
      } catch (e) {
        setError(e)
      }
    }

    checkAvailability()
  }, [])

  return (
    <View style={{ flex: 1, alignItems: 'center', paddingVertical: 64 }}>
      <Text
        style={{
          fontWeight: 'bold',
          fontSize: 24,
          marginVertical: 64,
        }}
      >
        RNStoryShare Example
      </Text>

      <ShareSection title="Instagram" isAvailable={isInstagramAvailable}>
        <Button onPress={shareInstagramBackground}>Background</Button>
        <Button onPress={shareInstagramSticker}>Sticker</Button>
        <Button onPress={shareInstagramBoth}>Both</Button>
      </ShareSection>

      <ShareSection title="Snapchat" isAvailable={isSnapchatAvailable}>
        <Button onPress={shareSnapchatBackground}>Background</Button>
        <Button onPress={shareSnapchatSticker}>Sticker</Button>
        <Button onPress={shareSnapchatBoth}>Both</Button>
      </ShareSection>

      <Text style={{ marginTop: 48, minHeight: 80 }}>
        {error ? `error: ${error.message}` : null}
      </Text>
    </View>
  )
}
