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
import com.wt2dadmuvy.spinbot.databinding.DialogAddChallengeBinding
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.viewmodel.ChallengesViewModel

/**
 * Cuadro de diálogo para agregar un nuevo reto - HU 7.0 (Jonatan).
 *
 * Sigue el patrón MVVM comunicándose con ChallengesViewModel para la persistencia.
 */
class AddChallengeDialogFragment : DialogFragment() {

    private var _binding: DialogAddChallengeBinding? = null
    private val binding get() = _binding!!

    // Obtenemos el ViewModel compartido con el ChallengesFragment (el padre)
    private val viewModel: ChallengesViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddChallengeBinding.inflate(inflater, container, false)
        
        // Criterio 1: Fondo blanco y bordes (configurado vía window)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Criterio 7: No se cierra al dar clic afuera
        isCancelable = false
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        setupTextWatcher()
    }

    private fun setupListeners() {
        // Criterio 4: Botón Cancelar quita el diálogo
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // Criterio 6: Botón Guardar inserta en BD y cierra diálogo
        binding.btnSave.setOnClickListener {
            val challengeText = binding.etChallenge.text.toString().trim()
            if (challengeText.isNotEmpty()) {
                // El modelo exige un name, usamos un valor por defecto
                val newChallenge = Challenge(name = "Reto", description = challengeText)
                viewModel.insert(newChallenge)
                dismiss()
            }
        }
    }

    /**
     * Criterio 5: El botón Guardar se habilita solo si hay texto.
     */
    private fun setupTextWatcher() {
        binding.etChallenge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrBlank()
                binding.btnSave.isEnabled = hasText
                
                // Criterio 5: Cambiar color de fondo según si está habilitado
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
}
