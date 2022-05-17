package es.clcarras.mydues.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.security.auth.login.LoginException

class LoginViewModel(
    private val _googleSignInClient: GoogleSignInClient,
    private val _firebaseAuth: FirebaseAuth
) : ViewModel() {

    class Factory(
        private val googleSignInClient: GoogleSignInClient,
        private val firebaseAuth: FirebaseAuth
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(googleSignInClient, firebaseAuth) as T
    }

    val googleSignInClient: GoogleSignInClient get() = _googleSignInClient

    private val _signIn = MutableLiveData(true)
    val signIn: LiveData<Boolean> get() = _signIn

    private val _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private val _pass = MutableLiveData("")
    val pass: LiveData<String> get() = _pass

    private val _confirmPass = MutableLiveData("")
    val confirmPass: LiveData<String> get() = _confirmPass

    fun toggleSignInSignUp() {
        _signIn.value = !_signIn.value!!
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPass(pass: String) {
        _pass.value = pass
    }

    fun setConfirmPass(confirmPass: String) {
        _confirmPass.value = confirmPass
    }

    suspend fun sign(idToken: String?) {
        var signComplete = false
        var exceptionMsg = ""

        val listener = OnCompleteListener<AuthResult> {
            if (it.isSuccessful) signComplete = true
            else exceptionMsg = it.exception?.message.toString()
        }

        when {
            idToken != null -> _firebaseAuth.signInWithCredential(
                GoogleAuthProvider.getCredential(idToken, null)
            ).addOnCompleteListener(listener).await()

            _signIn.value!! -> {

                if (!validSignInInput())
                    throw LoginException("You must fill all the fields!")

                _firebaseAuth.signInWithEmailAndPassword(_email.value!!, _pass.value!!)
                    .addOnCompleteListener(listener).await()
            }

            else -> {
                if (_pass.value!!.length < 6 || _confirmPass.value!!.length < 6)
                    throw LoginException("Password should have 6 or more characters!")
                else if (!validSignUpInput())
                    throw LoginException("Passwords do not match!")
                else if (!validSignInInput())
                    throw LoginException("You must fill all the fields!")

                _firebaseAuth.createUserWithEmailAndPassword(_email.value!!, _pass.value!!)
                    .addOnCompleteListener(listener).await()
            }
        }

        if (!signComplete) throw LoginException(exceptionMsg)

    }

    private fun validSignInInput(): Boolean =
        _email.value!!.isNotBlank() && _pass.value!!.isNotBlank()

    private fun validSignUpInput(): Boolean =
        _confirmPass.value!!.isNotBlank() && _pass.value.equals(_confirmPass.value)

}