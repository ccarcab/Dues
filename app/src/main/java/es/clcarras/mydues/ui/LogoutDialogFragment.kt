package es.clcarras.mydues.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DialogLogoutBinding

/**
 * DialogFragment que muestra un cuadro de diálogo para cerrar sesión
 */
class LogoutDialogFragment : DialogFragment() {

    private lateinit var binding: DialogLogoutBinding

    /**
     * Método que crea el diálogo
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogLogoutBinding.inflate(layoutInflater)

        with(binding) {

            // Onclick botón de cancelar el cierre de sesión
            btnCancel.setOnClickListener { dialog?.hide() }

            // Onclick botón de cierre de sesión
            btnLogOut.setOnClickListener {
                // Se cierra la sesión de firebase
                Firebase.auth.signOut()
                findNavController().navigate(R.id.nav_login)
            }
        }

        // Se devuelve el diálogo
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    /**
     * Método que inicializa la vista del diálogo
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Fondo personalizado
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return binding.root
    }
}