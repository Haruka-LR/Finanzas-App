package com.example.finanzasapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Dimens {
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 16.dp
    val SpaceLg = 24.dp
    val SpaceXl = 32.dp

    val BorderThin = 1.dp
    val BorderMedium = 2.dp
    val RadiusSm = 8.dp
    val RadiusMd = 12.dp
    val RadiusLg = 20.dp
}

object AppShapes {
    val Small = RoundedCornerShape(Dimens.RadiusSm)
    val Medium = RoundedCornerShape(Dimens.RadiusMd)
    val Large = RoundedCornerShape(Dimens.RadiusLg)
}