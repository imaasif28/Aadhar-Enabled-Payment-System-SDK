package com.aasif.aadharenabledpayment

data class DataModel(
    var applicationType: String? = null,
    var userNameFromCoreApp: String? = null,
    var clientRefID: String? = null,
    var clientID: String? = null,
    var clientSecret: String? = null,
    var paramB: String? = null,
    var paramC: String? = null,
    var API_USER_NAME_VALUE: String? = null,
    var SHOP_NAME: String? = null,
    var BRAND_NAME: String? = null,
    var location: String? = null,
    var agent: String? = null,
    var skipReceipt: Boolean? = null,
    var transactionAmount: String? = null,
    var transactionType: String? = null,
    var bankCode: String? = null,
)
