package com.aasif.aadharenabledpayment.enc


data class AepsDataModel(
    var apiUserName: String = "",
    var userName: String = "",
    var clientRefID: String = "",
    var transactionType: String = "",
    var transactionAmount: String = "",
    var shopName: String = "",
    var brandName: String = "",
    var paramB: String = "",
    var paramC: String = "",
    var agent: String = "",
    var location: String = "",
    var headerSecrets: String = "",
    var skipReceipt: Boolean = false,
    var passKey: String = ""
)