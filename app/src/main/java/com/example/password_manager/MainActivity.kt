package com.example.password_manager

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.password_manager.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseClass
    private lateinit var recyclerView: RecyclerView

    val key: String = "MySecretKey12345"
    val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = DatabaseClass.getDatabase(applicationContext)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        CoroutineScope(Dispatchers.Main).launch {
            getData()
        }

        binding.addFab.setOnClickListener {
            createPopUpWindow()
        }
    }

    private suspend fun getData() {
        val trdao = database.DataDao()
        val passwords_list: List<DataClass> = withContext(Dispatchers.IO) {
            trdao.getAllData()
        }
        recyclerView.adapter = Adapter(passwords_list)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("InflateParams")
    private fun createPopUpWindow() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpWindow = inflater.inflate(R.layout.mainpopup , null)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popUpWindow , width , height , focusable)
        popupWindow.showAtLocation(popUpWindow , Gravity.BOTTOM , 0 , 100)

        val account = popUpWindow.findViewById<EditText>(R.id.account_name)
        val username = popUpWindow.findViewById<EditText>(R.id.edt_username)
        val password = popUpWindow.findViewById<EditText>(R.id.etPassword)
        val btnAdsData = popUpWindow.findViewById<Button>(R.id.add_btn_ac)
        val genratePswd = popUpWindow.findViewById<TextView>(R.id.txt_gent_pswd)

        genratePswd.setOnClickListener {
            val randomPassword = generateRandomPassword(8)
            println("=====Random Password: $randomPassword")
            val clipboard=getSystemService(Context.CLIPBOARD_SERVICE)as ClipboardManager
            val clip =ClipData.newPlainText("Password",randomPassword)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(applicationContext , "Password Copied" , Toast.LENGTH_SHORT).show()
        }


        btnAdsData.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val account_text = account.text.toString()
                    val usernameText = username.text.toString()
                    val pasword_text = password.text.toString()
                    val encryptedText = encryption(pasword_text)
                    if (account_text.isNotEmpty() && usernameText.isNotEmpty() && pasword_text.isNotEmpty()) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val data = DataClass(
                                0 ,
                                account_text ,
                                usernameText ,
                                encryptedText
                            )
                            database.DataDao().insertData(data)

                            Toast.makeText(
                                applicationContext , "Data SuccessFully Added" , Toast.LENGTH_SHORT
                            ).show()
                            account.text.clear()
                            username.text.clear()
                            password.text.clear()
                            val dataT = database.DataDao()
                            val passwords_list: List<DataClass> = withContext(Dispatchers.IO) {
                                dataT.getAllData()
                            }
                            println("====== $passwords_list")
                            recyclerView.adapter = Adapter(passwords_list)
                        }

                    } else {
                        Toast.makeText(
                            applicationContext , "Data not Added" , Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (_: Exception) {
                }
            }
        }

    }
    private fun generateRandomPassword(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()[]"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun encryption(string: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptByte = cipher.doFinal(string.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptByte)
    }

    fun decryption(string: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(string))
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
