package com.wt2dadmuvy.spinbot.view.challenges

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.wt2dadmuvy.spinbot.R
import com.wt2dadmuvy.spinbot.databinding.DialogEditChallengeBinding
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.viewmodel.ChallengesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Cuadro de diálogo para EDITAR un reto existente - HU 8.0 (Alexandra).
 *
 * Recibe el objeto Challenge a editar, muestra su descripción actual y
 * permite guardarlo tras modificarlo, actualizando la base de datos local.
 */
@AndroidEntryPoint
class EditChallengeDialogFragment : DialogFragment() {

    private var _binding: DialogEditChallengeBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido con el ChallengesFragment para realizar el update
    private val viewModel: ChallengesViewModel by viewModels({ requireParentFragment() })

    // El reto que se está editando
    private var challengeToEdit: Challenge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperamos el reto pasado como argumento
        arguments?.let {
            val id = it.getString(ARG_ID) ?: ""
            val name = it.getString(ARG_NAME) ?: ""
            val description = it.getString(ARG_DESC) ?: ""
            challengeToEdit = Challenge(id, name, description)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditChallengeBinding.inflate(inflater, container, false)
        
        // Criterio 1: Fondo blanco (configurado en el XML y vía window para transparencia de bordes)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Criterio 7: El diálogo solo se cierra con los botones, no al tocar fuera
        isCancelable = false
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Criterio 3: Pre-llenar el EditText con la descripción actual del reto
        challengeToEdit?.let {
            binding.etChallenge.setText(it.description)
            // Posicionar el cursor al final del texto
            binding.etChallenge.setSelection(it.description.length)
        }

        setupListeners()
        setupTextWatcher()
    }

    private fun setupListeners() {
        // Criterio 4: Botón Cancelar cierra el diálogo sin guardar cambios
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // Criterio 5 y 6: Botón Guardar actualiza en BD y cierra diálogo
        binding.btnSave.setOnClickListener {
            val updatedText = binding.etChallenge.text.toString().trim()
            if (updatedText.isNotEmpty() && challengeToEdit != null) {
                // Creamos un nuevo objeto con el mismo ID pero descripción actualizada
                val updatedChallenge = challengeToEdit!!.copy(description = updatedText)
                viewModel.update(updatedChallenge)
                dismiss()
            }
        }
    }

    /**
     * Mantiene el botón Guardar habilitado solo si hay texto en el campo.
     */
    private fun setupTextWatcher() {
        binding.etChallenge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrBlank()
                binding.btnSave.isEnabled = hasText
                
                // Cambiar color de fondo según el estado (naranja si hay texto, gris si está vacío)
                val colorRes = if (hasText) R.color.orange else R.color.gray
                binding.btnSave.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), colorRes)
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "challenge_id"
        private const val ARG_NAME = "challenge_name"
        private const val ARG_DESC = "challenge_desc"

        /**
         * Método estático para crear una instancia del diálogo pasando el reto a editar.
         */
        fun newInstance(challenge: Challenge): EditChallengeDialogFragment {
            val fragment = EditChallengeDialogFragment()
            val args = Bundle().apply {
                putString(ARG_ID, challenge.id)
                putString(ARG_NAME, challenge.name)
                putString(ARG_DESC, challenge.description)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
