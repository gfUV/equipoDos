package com.wt2dadmuvy.spinbot.view.instructions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.wt2dadmuvy.spinbot.databinding.FragmentInstructionsBinding
import com.wt2dadmuvy.spinbot.viewmodel.SharedAudioViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InstructionsFragment : Fragment() {

    private var _binding: FragmentInstructionsBinding? = null
    private val binding get() = _binding!!

    // HU 5 Criterio 1 y 3: ViewModel compartido para pausar y restaurar el audio de fondo
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // HU 5 Criterio 1: pausar el audio de fondo al entrar a esta pantalla
        sharedAudioViewModel.requestPause(true)

        // HU 5 Criterio 3: botón atrás vuelve al Home y restaura el audio si estaba encendido
        binding.btnBack.setOnClickListener {
            sharedAudioViewModel.requestPause(false)
            findNavController().popBackStack()
        }

        setupTrophyAnimation()
    }

    private fun setupTrophyAnimation() {
        _binding?.imgTrophy?.let { trophy ->
            trophy.animate()
                .translationY(-50f) // Salta hacia arriba
                .rotationBy(15f)    // Se inclina un poco
                .scaleX(1.1f)       // Crece un poco
                .scaleY(1.1f)
                .setDuration(500)
                .withEndAction {
                    trophy.animate()
                        .translationY(0f)  // Cae a su posición
                        .rotationBy(-15f)  // Endereza
                        .scaleX(1.0f)      // Vuelve a su tamaño
                        .scaleY(1.0f)
                        .setDuration(500)
                        .withEndAction {
                            // Pequeña pausa antes de volver a saltar
                            trophy.postDelayed({ setupTrophyAnimation() }, 200)
                        }
                        .start()
                }
                .start()
        }
    }

    override fun onDestroyView() {
        // Cancelamos la animación para evitar que intente ejecutarse tras destruir la vista
        _binding?.imgTrophy?.animate()?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
