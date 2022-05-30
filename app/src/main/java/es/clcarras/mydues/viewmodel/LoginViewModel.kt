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

/**
 * ViewModel del Fragment de la vista de Login
 */
class LoginViewModel(
    val googleSignInClient: GoogleSignInClient, // Cliente de inicio de sesión de Google
    private val _firebaseAuth: FirebaseAuth // Instancia de Firebase Auth
) : ViewModel() {

    /**
     * Clase Factory del ViewModel, usado para pasar parámetros al mismo
     */
    class Factory(
        private val googleSignInClient: GoogleSignInClient,
        private val firebaseAuth: FirebaseAuth
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(googleSignInClient, firebaseAuth) as T
    }

    // LiveData que actúa como bandera para saber si se debe mostrar la vista SignIn o SignUp
    private val _signIn = MutableLiveData(true)
    val signIn: LiveData<Boolean> get() = _signIn

    // LiveData para almacenar el email de usuario
    private val _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    // LiveData para almacenar la contraseña del usuario
    private val _pass = MutableLiveData("")
    val pass: LiveData<String> get() = _pass

    // LiveData para almacenar la confirmación de contraseña
    private val _confirmPass = MutableLiveData("")
    val confirmPass: LiveData<String> get() = _confirmPass

    /**
     * Método que cambia el estado de la bandera de SignIn
     */
    fun toggleSignInSignUp() {
        _signIn.value = !_signIn.value!!
    }

    // Setters de los LiveData //
    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPass(pass: String) {
        _pass.value = pass
    }

    fun setConfirmPass(confirmPass: String) {
        _confirmPass.value = confirmPass
    }

    /**
     * Función suspendida que intenta realizar el inicio de sesión o registro de usuario
     */
    suspend fun sign(idToken: String?) {
        var signComplete = false
        var exceptionMsg = ""

        // Listener que se ejecutará una vez se haya completado el inicio o registro
        val listener = OnCompleteListener<AuthResult> {
            if (it.isSuccessful) signComplete = true
            else exceptionMsg = it.exception?.message.toString()
        }

        when {
            // Si se ha recibido un token se intenta iniciar sesión con Google
            idToken != null -> _firebaseAuth.signInWithCredential(
                GoogleAuthProvider.getCredential(idToken, null)
            ).addOnCompleteListener(listener).await()

            // Si la bandera está en true, se intenta realizar el inicio de sesión
            _signIn.value!! -> {

                if (!validSignInInput())
                    throw LoginException("You must fill all the fields!")

                _firebaseAuth.signInWithEmailAndPassword(_email.value!!, _pass.value!!)
                    .addOnCompleteListener(listener).await()
            }

            // En caso contrario se intenta realizar el registro de usuario
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

    /**
     * Método que comprueba si los datos de inicio de sesión son correctos
     */
    private fun validSignInInput(): Boolean =
        _email.value!!.isNotBlank() && _pass.value!!.isNotBlank()

    /**
     * Método que comprueba si los datos de registro son correctos
     */
    private fun validSignUpInput(): Boolean =
        _confirmPass.value!!.isNotBlank() && _pass.value.equals(_confirmPass.value)

}