package com.example.smsfilterapp

data class SMSMessage(
    val sender: String,
    val content: String,
    val timestamp: Long
)
