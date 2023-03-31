package com.example.obd2pti.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.obd2pti.databinding.FragmentNotificationsBinding
import java.math.BigInteger
import java.security.MessageDigest

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val editTextMatricula: EditText = binding.editTextMatricula
        val editTextUsuario: EditText = binding.editTextUsuario
        val editTextPassword: EditText = binding.editTextPassword
        val loginButton: Button = binding.loginbutton

        var matricula: String
        var usuario: String
        var password: String

        loginButton.setOnClickListener{
            matricula = editTextMatricula.text.toString()
            usuario = editTextUsuario.text.toString()
            password = editTextPassword.text.toString()
            Toast.makeText(context, "Matricula: $matricula, Usuario: $usuario, Password: $password", Toast.LENGTH_SHORT).show()
            var md = MessageDigest.getInstance("SHA-256")
            val passwordHash = BigInteger(1, md.digest(password.toByteArray())).toString(16).padStart(32, '0')
            Toast.makeText(context, "Password Hash: $passwordHash", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}