package com.wt2dadmuvy.spinbot.view.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.wt2dadmuvy.spinbot.databinding.FragmentLoginBinding
import com.wt2dadmuvy.spinbot.viewmodel.LoginViewModel

/**
 * HU 2.0 - Ventana Login y Registro.
 *
 * Esta versión implementa únicamente los criterios 1 al 5 solicitados:
 * fondo negro sin toolbar, título, campo Email, campo Password con ícono de ojo
 * y validación de contraseña en tiempo real.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupTextWatchers() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.onPasswordChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun observeViewModel() {
        loginViewModel.passwordError.observe(viewLifecycleOwner) { errorMessage ->
            binding.tilPassword.error = errorMessage
            binding.tilPassword.isErrorEnabled = errorMessage != null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
