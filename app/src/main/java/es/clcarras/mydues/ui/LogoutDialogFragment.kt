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
import es.clcarras.mydues.R
import es.clcarras.mydues.databinding.DialogLogoutBinding

class LogoutDialogFragment : DialogFragment() {

    private lateinit var binding: DialogLogoutBinding

    /**
    Al crear el dialogo de cerrar sesión se crean eventos de escucha sobre los botones,
    que al pulsarlos desencadenarán una acción.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogLogoutBinding.inflate(layoutInflater)

        with(binding) {
            btnCancel.setOnClickListener { dialog?.hide() }
            btnLogOut.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.nav_login)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return binding.root
    }
}