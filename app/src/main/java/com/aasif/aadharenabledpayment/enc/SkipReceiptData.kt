package com.aasif.aadharenabledpayment.enc

data class SkipReceiptData(
    var data: String? = "",
    var status: String? = "",
    var statusDesc: String? = "",
    var clientRefId: String? = "",
    var transactionAmount: String? = ""
)

data class TransferResponseDomain(
    val transactionMode: String? = null,
    val txId: String? = null,
    val bankName: String? = null,
    val updatedDate: String? = null,
    val iin: String? = null,
    val originIdentifier: String? = null,
    val createdDate: String? = null,
    val apiTid: String? = null,
    val balance: String? = null,
    val isRetriable: Boolean? = null,
    val ministatement: List<MiniStatementDomain?>? = null,
    val apiComment: String? = null,
    val gateway: Int? = null,
    val errors: Any? = null,
    val status: String? = null,
    val authCode: String? = null,
    val uidRefId: String? = null,
    val depositId: String? = null,
    val statusCode: String? = null,
    val clientRefId: String? = null
) {
    data class MiniStatementDomain(
        val date: String? = null,
        val amount: String? = null,
        val txntype: String? = null,
        val txnDesc: String? = null,
    )
}
