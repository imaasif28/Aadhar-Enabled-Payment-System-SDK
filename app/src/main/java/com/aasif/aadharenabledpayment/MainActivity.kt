package com.aasif.aadharenabledpayment

import android.Manifest.permission.INTERNET
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aasif.aadharenabledpayment.databinding.ActivityMainBinding
import com.aasif.aadharenabledpayment.enc.AepsDataModel
import com.aasif.aadharenabledpayment.enc.SkipReceiptData
import com.google.gson.Gson
import org.json.JSONObject
import java.security.SecureRandom
import java.time.Instant
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {

        private const val CLIENT_ID = "42Zuw71Ok7e2TGAgHPKttM7PFGMspJLLy3ewq15dhgjtGM9l"
        private const val CLIENT_SECRET =
            "MDB9krmA8OqYdgjTKflkXXU7BTNAJgVDEWBmhWjQ8YBvAPNKNPLbxnJGSKcKiEV9"
        private const val PASS_KEY = "NNK7BD3GVEEQSFC3AYJW544MFQT2F33DS7OG4NJHWCFOFTG2HCKA"
        private const val ENCRYPTION_KEY = "VfzItIDssAmv4hfldszp81Er+5kMZbFxdVg2EwdvptA="

        //        private const val CLIENT_ID = "AdOZaGcsQqq38Dk5RA4VU1sMbxTeYXtAEPbcobapuu9MhGij"
        //        private const val CLIENT_SECRET = "VDJMKviTuHO12VAd5e4d7X6nZltrFkWsBlXxPhs2iN5Rmv66JG3KhfMQzGMDRycD"
        //        private const val PASS_KEY = "GzHGpb48c7HcDRCJoO31IlUWKHfsvwUdb4IhtK4IFsaypzT6"
        //        private const val ENCRYPTION_KEY = "EZsw7iLGY4fFCe4P/O+6CLCcuW0DH5KdGsl7WtonH1I="
        private const val REQUEST_CODE_INTERNET = 100
        private const val REQUEST_CODE_SERVICE = 80
        private const val AES_KEY_SIZE: Int = 256
        private const val AES_BLOCK_SIZE: Int = 16
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ContextCompat.checkSelfPermission(this, INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(INTERNET),
                REQUEST_CODE_INTERNET
            )
        }
        binding.submitButton.setOnClickListener {
            checkAppInstalledOrNot()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_INTERNET -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Toast.makeText(this, "Internet Permission granted", Toast.LENGTH_LONG).show()
                    // Permission granted, proceed with internet access
                } else {
                    Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show()
                    // Permission denied, handle accordingly
                }
                return
            }
            // ... handle other request codes
        }
    }

    private fun checkAppInstalledOrNot() {
        val installed = appInstalledOrNot()
        try {
            if (installed) {
                sendDataToService(this, activityResultLauncher)
            } else {
                showAlert()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appInstalledOrNot(): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo("com.isu.aepssdk", PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    private fun showAlert() = try {
        val alertBuilderUpdate =
            AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
        alertBuilderUpdate.setCancelable(false)
        val message = "Please download the AEPS SERVICE app ."
        alertBuilderUpdate.setTitle("Alert")
            .setMessage(message)
            .setPositiveButton("Download Now") { _, _ -> redirectToAppStore() }
            .setNegativeButton("Not Now") { dialog, _ -> dialog.dismiss() }
        val alert11: AlertDialog = alertBuilderUpdate.create()
        alert11.show()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

    private fun redirectToAppStore() {
        val uri = Uri.parse("https://liveappstore.in/shareapp?com.isu.aepssdk=")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://liveappstore.in/shareapp?com.isu.aepssdk=")
                    )
                )
            } catch (exception: Exception) {
                Toast.makeText(this, "App Not Found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateEncryptedData(dataToEncrypt: ByteArray, key: String?): String? {
        try {
            val iv = ByteArray(AES_BLOCK_SIZE)
            val random = SecureRandom()
            random.nextBytes(iv)
            val decodedKey: ByteArray = Base64.decode(key, Base64.NO_WRAP)
            val secretKeySpec = SecretKeySpec(decodedKey, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
            val paddedData: ByteArray = pkcs7Padding(dataToEncrypt, AES_BLOCK_SIZE)
            val encrypted = cipher.doFinal(paddedData)
            val result = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, result, 0, iv.size)
            System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)
            return Base64.encodeToString(result, Base64.NO_WRAP)
        } catch (e: Exception) {
            return null
        }
    }

    private fun pkcs7Padding(data: ByteArray, blockSize: Int): ByteArray {
        val padding = blockSize - (data.size % blockSize)
        val padText = ByteArray(padding)
        Arrays.fill(padText, padding.toByte())
        val paddedData = ByteArray(data.size + padding)
        System.arraycopy(data, 0, paddedData, 0, data.size)
        System.arraycopy(padText, 0, paddedData, data.size, padding)
        return paddedData
    }

    @Throws(Exception::class)
    fun encryptRequest(incomingJsonReq: Any): String {
        val decodedKey = Base64.decode(ENCRYPTION_KEY, Base64.NO_WRAP)
        // Generate a random IV
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        // Convert payload to byte array
        val payloadBytes = incomingJsonReq.toString().toByteArray()
        // Initialize AES cipher
        val secretKeySpec = SecretKeySpec(decodedKey, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
        // Encrypt the data
        val encryptedBytes = cipher.doFinal(payloadBytes)
        // Combine IV and encrypted data
        val result = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, result, iv.size, encryptedBytes.size)
        // Return Base64 encoded result
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    private fun getBase64DataModel(aepsDataModel: AepsDataModel): String {
        val gson = Gson()
        val getData = gson.toJson(aepsDataModel)
        val bytes = getData.toString().toByteArray()
        val responseToBase64 = String(Base64.encode(bytes, Base64.NO_WRAP))
        println("getBase64DataModel $responseToBase64")
        return responseToBase64
    }

    private fun sendDataToService(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        val dataToEncrypt = JSONObject().apply {
            put("client_id", CLIENT_ID)
            put("client_secret", CLIENT_SECRET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) put(
                "epoch", Instant.now().epochSecond.toString()
            ) else put("epoch", "${System.currentTimeMillis() / 1000}")
        }
        println("data $dataToEncrypt")
        try {
            val encryptedHeader = encryptRequest(
                dataToEncrypt.toString()
            )  // Follow the step 1 to generate dataToEncrypt.
            println("encryptedHeader $encryptedHeader")
            val aepsDataModel = AepsDataModel(
                apiUserName = "isutest", // Mandatory
                userName = "aepsTestR",  // Mandatory
                transactionType = "3",
                /*mandatory, Set the transaction type 0-Balance Enquiry, 1-
                                Cash Withdrawal, 2-Mini Statement, 3-Aadhaar Pay, 4-Cash Deposit*/
                transactionAmount = "100",
                /* mandatory, Set the transaction amount for cash withdrawal, cash
                                deposit, aadhaar pay and for others it to 0*/
                clientRefID = "TEST".plus(Random.Default.nextInt().toString()),
                shopName = "Main Shop",
                brandName = "Livlong",
                paramB = "",
                paramC = "",
                agent = "",
                location = "Delhi",
                passKey = PASS_KEY,// Mandatory provided by iServeU
                headerSecrets = encryptedHeader  // Mandatory
            )
            println("aepsDataModel $aepsDataModel ")
            // Convert AepsDataModel to Base64
            val encodedData = getBase64DataModel(aepsDataModel)
            val manager: PackageManager = context.packageManager
            val intent = manager.getLaunchIntentForPackage("com.isu.aepssdk")
            intent!!.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = 0
            intent.putExtra("dataToAepsSdk", encodedData)
            activityResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.localizedMessage?.let { toast(it) }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val encodedData = result.data!!.getStringExtra("encodedSkipReceiptData")
                if (!encodedData.isNullOrEmpty()) {
                    // Decode the Base64 encoded data
                    val decodedBytes = Base64.decode(encodedData, Base64.NO_WRAP)
                    val decodedData = String(decodedBytes)
                    // Parse the JSON back into SkipReceiptData
                    println("decodedData $decodedData")
                    val gson = Gson()
                    val skipReceiptData = gson.fromJson(decodedData, SkipReceiptData::class.java)
                    // Use the decoded SkipReceiptData for example
                    toast("Status: ${skipReceiptData.status}, Transaction Amount: ${skipReceiptData.transactionAmount}")
                }
            }
        }

    private fun toast(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
    }
}