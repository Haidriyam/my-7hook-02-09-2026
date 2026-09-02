package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SavedConfigEntity
import com.example.data.local.SessionManager
import com.example.data.model.*
import com.example.data.pdf.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    val currentUser: StateFlow<UserAccount?> = sessionManager.currentUser

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _signupError = MutableStateFlow<String?>(null)
    val signupError: StateFlow<String?> = _signupError.asStateFlow()

    fun login(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _loginError.value = "Please enter your email or username."
            return false
        }
        if (password.length < 4) {
            _loginError.value = "Password must be at least 4 characters."
            return false
        }
        _loginError.value = null
        return sessionManager.login(email)
    }

    fun signup(name: String, email: String, password: String, confirmPass: String, company: String, phone: String): Boolean {
        if (name.isBlank()) {
            _signupError.value = "Please enter your name."
            return false
        }
        if (email.isBlank() || !email.contains("@")) {
            _signupError.value = "Please enter a valid email address."
            return false
        }
        if (password.length < 4) {
            _signupError.value = "Password must be at least 4 characters."
            return false
        }
        if (password != confirmPass) {
            _signupError.value = "Passwords do not match."
            return false
        }
        _signupError.value = null
        return sessionManager.signup(name, email, company, phone)
    }

    fun logout() {
        sessionManager.logout()
    }

    fun clearErrors() {
        _loginError.value = null
        _signupError.value = null
    }
}
