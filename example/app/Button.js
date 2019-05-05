import React from 'react'
import { Text, TouchableOpacity } from 'react-native'
import P from 'prop-types'

export const Button = ({ children, onPress }) => (
  <TouchableOpacity onPress={onPress} style={{ flex: 1 }}>
    <Text style={{ color: '#007aff', textAlign: 'center' }}>{children}</Text>
  </TouchableOpacity>
)

Button.propTypes = {
  children: P.string.isRequired,
  onPress: P.func,
}

Button.defaultProps = {
  onPress: () => {},
}
