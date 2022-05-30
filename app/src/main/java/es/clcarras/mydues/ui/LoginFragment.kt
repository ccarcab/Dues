package es.clcarras.mydues.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.FragmentLoginBinding
import es.clcarras.mydues.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import javax.security.auth.login.LoginException

/**
 * Fragment que muestra la pantalla de inicio de sesión y de registro
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var viewModelFactory: LoginViewModel.Factory

    /**
     * Método que crea la vista
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModelFactory =
            LoginViewModel.Factory(configureSignInGoogle(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        binding.viewModel = viewModel

        with(viewModel) {
            signIn.observe(viewLifecycleOwner) {
                if (it) showSignInView() // Si está activada la vista login
                else showSignUpView() // Si está activada la vista registro
            }
            with(binding) {
                // Se asignan los eventos de escucha de los cambios en los campos de texto
                etEmail.doOnTextChanged { text, _, _, _ -> setEmail(text.toString()) }
                etPass.doOnTextChanged { text, _, _, _ -> setPass(text.toString()) }
                etConfirmPass.doOnTextChanged { text, _, _, _ -> setConfirmPass(text.toString()) }
                // Onclick del botón enter
                btnEnter.setOnClickListener {
                    // Se lanza un scope que intenta realizar el registro, el inicio de sesión
                    // con google o el inicio de sesión con correo electrónico
                    lifecycleScope.launch {
                        try {
                            viewModel!!.sign(null)
                            findNavController().navigate(R.id.nav_home)
                        } catch (e: Exception) {
                            when (e) {
                                is FirebaseAuthException, is LoginException, is FirebaseException ->
                                    snackBar(e.message!!)
                                else -> throw e
                            }
                        }
                    }
                }
                // Onclick del botón de inicio con Google
                btnGoogle.setOnClickListener { showGoogleSignIn() }
            }
        }
        return binding.root
    }

    /**
     * Método llamado al iniciar el Fragment
     */
    override fun onStart() {
        super.onStart()
        // Si el usuario ya está logueado se navega hacia la vista home
        if (Firebase.auth.currentUser != null)
            findNavController().navigate(R.id.nav_home)
        // En caso contrario se oculta el action bar y el fab
        else
            with(requireActivity() as MainActivity) {
                supportActionBar?.hide()
                getFab().hide()
            }
    }

    /**
     * Método que muestra los elementos de la vista de inicio de sesión y
     * establece los strings correspondientes
     */
    private fun showSignInView() {
        with(binding) {
            tvTitle.text = getString(R.string.sign_in_title)
            tvSubtitle1.text = getString(R.string.sign_in_subtitle1)
            tvSubtitle2.text = getString(R.string.sign_in_subtitle2)
            tvToggleView.text = getString(R.string.sign_in_toggle)
            btnEnter.text = getString(R.string.sign_in)
            tilConfirmPass.visibility = View.GONE
            btnGoogle.visibility = View.VISIBLE
        }
    }

    /**
     * Método que muestra los elementos de la vista de registro y
     * establece los strings correspondientes
     */
    private fun showSignUpView() {
        with(binding) {
            tvTitle.text = getString(R.string.sign_up_title)
            tvSubtitle1.text = getString(R.string.sign_up_subtitle1)
            tvSubtitle2.text = getString(R.string.sign_up_subtitle2)
            tvToggleView.text = getString(R.string.sign_up_toggle)
            btnEnter.text = getString(R.string.sign_up)
            tilConfirmPass.visibility = View.VISIBLE
            btnGoogle.visibility = View.GONE
        }
    }

    /**
     * Método que configura y devuelve un cliente de inicio de sesión con Google
     */
    private fun configureSignInGoogle(): GoogleSignInClient {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.defaultWebId))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInClient.signOut()

        return googleSignInClient
    }

    /**
     * Método que muestra el cliente de inicio de sesión con google
     */
    private fun showGoogleSignIn() {
        resultLauncher.launch(viewModel.googleSignInClient.signInIntent)
    }

    /**
     * Result Launcher usado para recoger la respuesta del cliente de inicio de sesión
     * con Google y actuar en consecuencia
     */
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            .getResult(ApiException::class.java)!!

                        viewModel.sign(account.idToken!!)
                        findNavController().navigate(R.id.nav_home)

                    } catch (e: Exception) {
                        snackBar(e.message!!)
                    }
                }
            }
        }

    /**
     * Método para mostrar un mensaje mediante un SnackBar
     */
    private fun snackBar(msg: String) {
        Snackbar.make(
            requireView(),
            msg,
            Snackbar.LENGTH_LONG
        ).show()
    }
}