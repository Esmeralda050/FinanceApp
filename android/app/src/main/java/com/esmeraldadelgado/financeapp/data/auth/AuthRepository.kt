package com.esmeraldadelgado.financeapp.data.auth

import com.esmeraldadelgado.financeapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    val sessionStatus: StateFlow<SessionStatus> = supabase.auth.sessionStatus

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(name: String, email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("full_name", name) }
        }
    }

    suspend fun signOut() = supabase.auth.signOut()
}