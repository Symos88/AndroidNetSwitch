package com.symos.netswitch.data

import kotlinx.serialization.Serializable

@Serializable
data class HomeLocation(
    val latitude: Double,
    val longitude: Double
)
