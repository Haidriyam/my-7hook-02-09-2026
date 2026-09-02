package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sevenhooks_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        // Load initial session if exists
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val email = prefs.getString("user_email", "") ?: ""
            val name = prefs.getString("user_name", "") ?: ""
            val company = prefs.getString("user_company", "") ?: ""
            val phone = prefs.getString("user_phone", "") ?: ""
            if (email.isNotEmpty()) {
                _currentUser.value = UserAccount(email, name, company, phone, true)
            }
        }
    }

    fun login(email: String, name: String = "", company: String = "", phone: String = ""): Boolean {
        if (email.isBlank()) return false
        val resolvedName = name.ifBlank { email.substringBefore("@").replace(".", " ").capitalizeWords() }
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_email", email.trim())
            putString("user_name", resolvedName)
            putString("user_company", company.trim())
            putString("user_phone", phone.trim())
            apply()
        }
        _currentUser.value = UserAccount(email.trim(), resolvedName, company.trim(), phone.trim(), true)
        return true
    }

    fun signup(name: String, email: String, company: String, phone: String): Boolean {
        if (email.isBlank() || name.isBlank()) return false
        return login(email, name, company, phone)
    }

    fun logout() {
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            remove("user_email")
            remove("user_name")
            remove("user_company")
            remove("user_phone")
            apply()
        }
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value?.isLoggedIn == true

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
