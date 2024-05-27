package com.aspire.aquitoy.nurse.ui.home.model

data class ServiceInfo(
    val serviceID: String? = null,
    val nurseID: String? = null,
    val patientID: String? = null,
    val patientLocationService: String? = null,
    var state: String? = null,
    var sendHistory: String? = null
)
