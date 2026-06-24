package com.wt2dadmuvy.spinbot.view.auth

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.wt2dadmuvy.spinbot.R
import com.wt2dadmuvy.spinbot.databinding.FragmentLoginBinding
import com.wt2dadmuvy.spinbot.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * HU 2.0 - Ventana Login y Registro.
 *
 * Implementación parcial solicitada para el Miniproyecto II:
 * Criterios 1 al 5, más criterios 6, 7, 8, 11, 12 y 15.
 *
 * No se implementan aún los criterios de autenticación Firebase ni navegación
 * por Login/Registro exitoso, porque corresponden a criterios posteriores.
 */
@AndroidEntryPoint
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
        setupInitialState()
        setupTextWatchers()
        setupClickListeners()
        setupPasswordEye()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            loginViewModel.loginUser()
        }

        binding.tvRegister.setOnClickListener {
            loginViewModel.registerUser()
        }
    }

    private fun setupInitialState() {
        updateActionViews(enabled = false)
    }

    private fun setupTextWatchers() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.onEmailChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.onPasswordChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupPasswordEye() {
        binding.tilPassword.setEndIconOnClickListener {
            loginViewModel.togglePasswordVisibility()
        }
    }

    private fun observeViewModel() {
        loginViewModel.passwordError.observe(viewLifecycleOwner) { errorMessage ->
            binding.tilPassword.error = errorMessage
            binding.tilPassword.isErrorEnabled = errorMessage != null
        }

        loginViewModel.actionsEnabled.observe(viewLifecycleOwner) { enabled ->
            updateActionViews(enabled)
        }

        loginViewModel.passwordVisible.observe(viewLifecycleOwner) { visible ->
            updatePasswordVisibility(visible)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.authState.collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        val exception = it.exceptionOrNull()
                        val errorMessage = when (exception) {
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException,
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> 
                                getString(R.string.login_error_incorrect)
                            
                            is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                                getString(R.string.login_error_email_exists)
                                
                            else -> exception?.message ?: getString(R.string.login_error_unknown)
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updatePasswordVisibility(visible: Boolean) {
        val selection = binding.etPassword.selectionStart.coerceAtLeast(0)

        if (visible) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_NUMBER
            binding.tilPassword.setEndIconDrawable(R.drawable.ic_visibility_closed)
            binding.tilPassword.setEndIconContentDescription(
                getString(R.string.login_password_eye_hide_description)
            )
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
            binding.tilPassword.setEndIconDrawable(R.drawable.ic_visibility_open)
            binding.tilPassword.setEndIconContentDescription(
                getString(R.string.login_password_eye_show_description)
            )
        }

        binding.etPassword.setSelection(selection.coerceAtMost(binding.etPassword.text?.length ?: 0))
    }

    private fun updateActionViews(enabled: Boolean) {
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val disabledText = ContextCompat.getColor(requireContext(), R.color.login_disabled_text)
        val orange = ContextCompat.getColor(requireContext(), R.color.login_orange)
        val registerGray = ContextCompat.getColor(requireContext(), R.color.login_register_gray)

        binding.btnLogin.isEnabled = enabled
        binding.btnLogin.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
        binding.btnLogin.setTextColor(if (enabled) white else disabledText)
        binding.btnLogin.typeface = Typeface.DEFAULT_BOLD.takeIf { enabled } ?: Typeface.DEFAULT
        binding.btnLogin.backgroundTintList = ColorStateList.valueOf(orange)

        binding.tvRegister.isEnabled = enabled
        binding.tvRegister.isClickable = enabled
        binding.tvRegister.alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
        binding.tvRegister.setTextColor(if (enabled) white else registerGray)
        binding.tvRegister.typeface = Typeface.DEFAULT_BOLD.takeIf { enabled } ?: Typeface.DEFAULT
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ENABLED_ALPHA = 1.0f
        private const val DISABLED_ALPHA = 0.45f
    }
}
