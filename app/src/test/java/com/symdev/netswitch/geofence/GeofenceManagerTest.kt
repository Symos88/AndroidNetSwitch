package com.symdev.netswitch.geofence

import org.junit.Assert.assertEquals
import org.junit.Test

class GeofenceManagerTest {

    @Test
    fun normalizeRadius_clampsBelowMinimum() {
        assertEquals(50, GeofenceManager.normalizeRadius(1))
    }

    @Test
    fun normalizeRadius_keepsValidValue() {
        assertEquals(150, GeofenceManager.normalizeRadius(150))
    }

    @Test
    fun normalizeRadius_clampsAboveMaximum() {
        assertEquals(500, GeofenceManager.normalizeRadius(10_000))
    }
}
