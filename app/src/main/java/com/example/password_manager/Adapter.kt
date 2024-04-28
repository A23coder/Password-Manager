package com.example.password_manager

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import secretKeySpec
import java.util.Base64
import javax.crypto.Cipher

class Adapter(private var dataTransaction: List<DataClass>) :
    RecyclerView.Adapter<Adapter.MyViewViewHolder>() {
    class MyViewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val account_name: TextView = itemView.findViewById(R.id.account_name_item)
        val img_src: ImageView = itemView.findViewById(R.id.ac_img)
    }

    private fun decryption(string: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE , secretKeySpec)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(string))
        return String(decryptedBytes , Charsets.UTF_8)
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): MyViewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item , parent , false)
        return MyViewViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataTransaction.size
    }

    override fun onBindViewHolder(holder: MyViewViewHolder , position: Int) {
        val item = dataTransaction[position]
        holder.account_name.text = item.account_name
        when (item.account_name) {
            "Google" -> {
                holder.img_src.setImageResource(R.drawable.search)
            }

            "Linkedin" -> {
                holder.img_src.setImageResource(R.drawable.linkedin)
            }

            "Twitter" -> {
                holder.img_src.setImageResource(R.drawable.twitter)
            }

            "Instagram" -> {
                holder.img_src.setImageResource(R.drawable.instagram)
            }

            "Facebook" -> {
                holder.img_src.setImageResource(R.drawable.facebook)
            }

            else -> {
                holder.img_src.setImageResource(R.drawable.account_user)
            }
        }

        holder.itemView.setOnClickListener {
            val inflater =
                holder.itemView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popUpWindow = inflater.inflate(R.layout.updatepopup , null)
            val width = ViewGroup.LayoutParams.FILL_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            val focusable = true
            val popupWindow = PopupWindow(popUpWindow , width , height , focusable)
            CoroutineScope(Dispatchers.Main).launch {
                val database = DatabaseClass.getDatabase(holder.itemView.context)
                val dataDao = database.DataDao()
                val dataFromDatabase = dataDao.getDataById(item.id)

                val accountNameText = popUpWindow.findViewById<TextView>(R.id.account_name)
                val userNameText = popUpWindow.findViewById<TextView>(R.id.edt_username)
                val passwordText = popUpWindow.findViewById<TextView>(R.id.etPassword)
                val btnUpdate = popUpWindow.findViewById<Button>(R.id.update_btn)
                val btnDelete = popUpWindow.findViewById<Button>(R.id.delete_btn)

                val dec_password = dataFromDatabase.password

                accountNameText.text = dataFromDatabase.account_name
                userNameText.text = dataFromDatabase.username
                passwordText.text = decryption(dec_password)

                btnUpdate.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val updatedData = DataClass(
                                item.id ,
                                accountNameText.text.toString() ,
                                userNameText.text.toString() ,
                                passwordText.text.toString()
                            )
                            dataDao.updateData(updatedData)
                            Toast.makeText(
                                holder.itemView.context ,
                                "Data Successfully Updated" ,
                                Toast.LENGTH_SHORT
                            ).show()
                            holder.account_name.text = accountNameText.text
                            accountNameText.text = ""
                            userNameText.text = ""
                            passwordText.text = ""
                        } catch (e: Exception) {
                            Toast.makeText(
                                holder.itemView.context ,
                                "Failed to update data: ${e.message}" ,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                btnDelete.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val database = DatabaseClass.getDatabase(holder.itemView.context)
                            val dataDao = database.DataDao()

                            dataDao.deleteDataById(item.id)
                            val updatedList = dataTransaction.toMutableList()
                            updatedList.remove(item)
                            dataTransaction = updatedList
                            notifyItemRemoved(holder.adapterPosition)
                            Toast.makeText(
                                holder.itemView.context ,
                                "Data Successfully Deleted" ,
                                Toast.LENGTH_SHORT
                            ).show()
                            accountNameText.text = ""
                            userNameText.text = ""
                            passwordText.text = ""
                        } catch (e: Exception) {
                            Toast.makeText(
                                holder.itemView.context ,
                                "Failed to delete data: ${e.message}" ,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                popupWindow.showAtLocation(popUpWindow , Gravity.BOTTOM , 0 , 100)
            }
            holder.account_name.text = item.account_name
        }
    }
}