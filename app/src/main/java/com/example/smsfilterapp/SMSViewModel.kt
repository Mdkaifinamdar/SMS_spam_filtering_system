package com.example.smsfilterapp

import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SMSViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SMSViewModel"
    
    private val _spamMessages = MutableStateFlow<List<SMSMessage>>(emptyList())
    private val _hamMessages = MutableStateFlow<List<SMSMessage>>(emptyList())
    
    val spamMessages: StateFlow<List<SMSMessage>> = _spamMessages
    val hamMessages: StateFlow<List<SMSMessage>> = _hamMessages

    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val contentResolver: ContentResolver = context.contentResolver
            
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "SMS permission not granted")
                return@launch
            }
            
            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                Telephony.Sms.DEFAULT_SORT_ORDER
            )
            
            val spamList = mutableListOf<SMSMessage>()
            val hamList = mutableListOf<SMSMessage>()
            
            cursor?.use { c ->
                val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)
                
                while (c.moveToNext()) {
                    val message = SMSMessage(
                        sender = c.getString(addressIndex) ?: "",
                        content = c.getString(bodyIndex) ?: "",
                        timestamp = c.getLong(dateIndex)
                    )
                    
                    if (isSpam(message.content)) {
                        spamList.add(message)
                    } else {
                        hamList.add(message)
                    }
                }
            }
            
            _spamMessages.value = spamList
            _hamMessages.value = hamList
        }
    }
    
    private fun isSpam(content: String): Boolean {
        val text = content.lowercase()
        return text.contains("offer") || 
               text.contains("win") || 
               text.contains("prize") ||
               text.contains("free") ||
               text.contains("click") ||
               text.contains("subscribe") ||
               text.contains("loan") ||
               text.contains("credit") ||
               text.contains("cash") ||
               text.contains("lucky") ||
               text.contains("winner") ||
               text.contains("selected") ||
               text.contains("congrat") ||
               text.contains("claim") ||
               text.contains("reward") ||
               text.contains("gift") ||
               text.contains("limited time") ||
               text.contains("urgent") ||
               text.contains("act now") ||
               text.contains("buy now") ||
               text.contains("special offer") ||
               text.contains("discount") ||
               text.contains("deal") ||
               text.contains("expires") ||
               text.contains("lottery") ||
               text.contains("prize") ||
               text.contains("jackpot")
    }
}
