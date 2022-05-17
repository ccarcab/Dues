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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.MainActivity
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.LoginFragmentBinding
import es.clcarras.mydues.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import javax.security.auth.login.LoginException

class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var viewModelFactory: LoginViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModelFactory =
            LoginViewModel.Factory(configureSignInGoogle(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        binding.viewModel = viewModel

        with(viewModel) {
            signIn.observe(viewLifecycleOwner) {
                if (it) showSignInView()
                else showSignUpView()
            }
            with(binding) {
                etEmail.doOnTextChanged { text, _, _, _ -> setEmail(text.toString()) }
                etPass.doOnTextChanged { text, _, _, _ -> setPass(text.toString()) }
                etConfirmPass.doOnTextChanged { text, _, _, _ -> setConfirmPass(text.toString()) }
                btnEnter.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            viewModel!!.sign(null)
                            findNavController().navigate(R.id.nav_home)
                        } catch (e: Exception) {
                            when (e) {
                                is FirebaseAuthException, is LoginException ->
                                    snackBar(e.message!!)
                                else -> throw e
                            }
                        }
                    }
                }
                btnGoogle.setOnClickListener { showGoogleSignIn() }
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser != null)
            findNavController().navigate(R.id.nav_home)
        else
            with(requireActivity() as MainActivity) {
                supportActionBar?.hide()
                getFab().hide()
            }
    }

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

    private fun configureSignInGoogle(): GoogleSignInClient {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.defaultWebId))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInClient.signOut()

        return googleSignInClient
    }

    private fun showGoogleSignIn() {
        resultLauncher.launch(viewModel.googleSignInClient.signInIntent)
    }

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

    private fun snackBar(msg: String) {
        Snackbar.make(
            requireView(),
            msg,
            Snackbar.LENGTH_LONG
        ).show()
    }
}