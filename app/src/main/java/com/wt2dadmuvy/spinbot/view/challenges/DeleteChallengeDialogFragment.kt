package com.wt2dadmuvy.spinbot.view.challenges

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.wt2dadmuvy.spinbot.databinding.DialogDeleteChallengeBinding
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.viewmodel.ChallengesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Cuadro de diálogo para ELIMINAR un reto existente - HU 9.0 (German).
 *
 * Muestra la descripción del reto y solicita confirmación para eliminarlo
 * de la base de datos local (SQLite).
 */
@AndroidEntryPoint
class DeleteChallengeDialogFragment : DialogFragment() {

    private var _binding: DialogDeleteChallengeBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido con el ChallengesFragment para realizar el delete
    private val viewModel: ChallengesViewModel by viewModels({ requireParentFragment() })

    // El reto que se desea eliminar
    private var challengeToDelete: Challenge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperamos el reto pasado como argumento
        arguments?.let {
            val id = it.getInt(ARG_ID)
            val name = it.getString(ARG_NAME) ?: ""
            val description = it.getString(ARG_DESC) ?: ""
            challengeToDelete = Challenge(id, name, description)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeleteChallengeBinding.inflate(inflater, container, false)
        
        // Criterio 1: Fondo blanco (configurado en el XML y vía window para transparencia de bordes)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Criterio 6: El diálogo solo se cierra con los botones, no al tocar fuera
        isCancelable = false
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Criterio 3: Mostrar la descripción del reto a eliminar
        challengeToDelete?.let {
            binding.tvChallengeDescription.text = it.description
        }

        setupListeners()
    }

    private fun setupListeners() {
        // Criterio 4: Botón NO cierra el diálogo sin eliminar nada
        binding.btnNo.setOnClickListener {
            dismiss()
        }

        // Criterio 5: Botón SI elimina de la base de datos y cierra el diálogo
        binding.btnSi.setOnClickListener {
            challengeToDelete?.let {
                viewModel.delete(it)
                dismiss()
            }
        }
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
         * Método estático para crear una instancia del diálogo pasando el reto a eliminar.
         */
        fun newInstance(challenge: Challenge): DeleteChallengeDialogFragment {
            val fragment = DeleteChallengeDialogFragment()
            val args = Bundle().apply {
                putInt(ARG_ID, challenge.id)
                putString(ARG_NAME, challenge.name)
                putString(ARG_DESC, challenge.description)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
