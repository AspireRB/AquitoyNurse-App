package com.aspire.aquitoy.nurse.ui.home

import com.aspire.aquitoy.nurse.ui.home.Feature
import com.aspire.aquitoy.nurse.ui.home.Geometry
import com.google.gson.annotations.SerializedName

data class RouteResponse(@SerializedName("features") val features:List<Feature>)
data class Feature(@SerializedName("geometry") val geometry: Geometry)
data class Geometry(@SerializedName("coordinates") val coordinates:List<List<Double>>)