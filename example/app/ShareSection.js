import React from 'react'
import { Text, View } from 'react-native'
import P from 'prop-types'

export const ShareSection = ({ children, isAvailable, title }) => (
  <>
    <Text style={{ fontWeight: 'bold', fontSize: 24 }}>{title}</Text>
    <Text style={{ color: isAvailable ? '#4cd864' : '#ff3b30' }}>
      {isAvailable ? 'available' : 'not available'}
    </Text>
    <View
      style={{
        flexDirection: 'row',
        justifyContent: 'space-around',
        width: '100%',
        paddingVertical: 24,
      }}
    >
      {children}
    </View>
  </>
)

ShareSection.propTypes = {
  children: P.oneOfType([P.element, P.array]),
  isAvailable: P.bool,
  title: P.string.isRequired,
}

ShareSection.defaultProps = {
  isAvailable: false,
}
