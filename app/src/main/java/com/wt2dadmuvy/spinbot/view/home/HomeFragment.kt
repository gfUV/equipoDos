package com.wt2dadmuvy.spinbot.view.home

import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wt2dadmuvy.spinbot.R
import com.wt2dadmuvy.spinbot.databinding.FragmentHomeBinding
import com.wt2dadmuvy.spinbot.viewmodel.HomeViewModel
import com.wt2dadmuvy.spinbot.viewmodel.SharedAudioViewModel

class HomeFragment : Fragment() {

    // Configuración de View Binding para acceder de forma segura a las vistas
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private val homeViewModel: HomeViewModel by viewModels()

    // ViewModel compartido con ChallengesFragment (HU 6) e InstructionsFragment (HU 5).
    // Permite que esos fragments sepan si el audio estaba ON y soliciten pausarlo/reanudarlo.
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    private var backgroundMusic: MediaPlayer? = null

    // MP1-24 Criterio 3: El estado del audio inicia por defecto en ENCENDIDO (true)
    private var isAudioOn = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar listeners de la Toolbar
        setupToolbarListeners()

        // 2. Inicializar lógica del juego
        configureAnimations()
        observeViewModel()
        homeViewModel.startCountdownPreview()

        // 3. Observar el estado del audio (ON/OFF) y las solicitudes de pausa (HU 5/6)
        observeSharedAudio()
    }

    override fun onResume() {
        super.onResume()
        // Solo reproduce música al volver si el usuario no la ha silenciado manualmente
        if (isAudioOn) {
            startBackgroundMusic()
        }
    }

    override fun onPause() {
        pauseBackgroundMusic()
        super.onPause()
    }

    override fun onDestroyView() {
        releaseBackgroundMusic()
        _binding = null // Evitamos fugas de memoria (Memory Leaks)
        super.onDestroyView()
    }

    private fun configureAnimations() {
        val pulseAnimation1 = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_ripple)
        val pulseAnimation2 = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_ripple)

        // Desfasamos las ondas para un efecto más natural
        pulseAnimation2.startOffset = 1000

        binding.viewPulse1.startAnimation(pulseAnimation1)
        binding.viewPulse2.startAnimation(pulseAnimation2)
    }

    private fun observeViewModel() {
        homeViewModel.countdown.observe(viewLifecycleOwner) { number ->
            binding.tvCountdown.text = number.toString()
        }
    }

    private fun startBackgroundMusic() {
        if (backgroundMusic == null) {
            backgroundMusic = MediaPlayer.create(requireContext(), R.raw.game_background).apply {
                isLooping = true
                setVolume(0.45f, 0.45f)
            }
        }

        backgroundMusic?.let { player ->
            if (!player.isPlaying) player.start()
        }
    }
    private fun pauseBackgroundMusic() {
        backgroundMusic?.let { player ->
            if (player.isPlaying) player.pause()
        }
    }

    private fun releaseBackgroundMusic() {
        backgroundMusic?.release()
        backgroundMusic = null
    }

    // --- AUDIO COMPARTIDO (HU 5 y HU 6) ---

    /**
     * Observa las solicitudes de pausa de audio que hacen ChallengesFragment (HU 6)
     * e InstructionsFragment (HU 5) al entrar y salir de sus pantallas.
     *
     * - pause = true  → pausar música (el fragment destino acaba de abrirse)
     * - pause = false → reanudar música (el fragment destino acaba de cerrarse)
     *
     * Solo reanuda si el usuario no apagó el audio manualmente (isAudioOn == true)
     * y si el MediaPlayer ya fue inicializado (backgroundMusic != null).
     * La segunda condición evita iniciar música prematuramente antes de onResume().
     */
    private fun observeSharedAudio() {
        // Observa si el usuario apagó o encendió el audio globalmente
        sharedAudioViewModel.isAudioOn.observe(viewLifecycleOwner) { isOn ->
            isAudioOn = isOn
            updateAudioIconUI(isOn)
        }

        // Observa solicitudes de pausa temporal (al entrar a otras pantallas)
        sharedAudioViewModel.pauseRequested.observe(viewLifecycleOwner) { shouldPause ->
            if (shouldPause) {
                pauseBackgroundMusic()
            } else {
                // Reanudar solo si el usuario no había apagado el audio y ya existe el player
                if (isAudioOn && backgroundMusic != null) {
                    startBackgroundMusic()
                }
            }
        }
    }

    private fun updateAudioIconUI(isOn: Boolean) {
        val iconRes = if (isOn) R.drawable.volume_up else R.drawable.volume_off
        binding.customToolbar.btnAudio.setImageResource(iconRes)

        // Mantiene el color naranja oficial en el nuevo ícono
        val colorNaranja = ContextCompat.getColor(requireContext(), R.color.orange)
        binding.customToolbar.btnAudio.imageTintList = ColorStateList.valueOf(colorNaranja)
    }

    // --- LÓGICA DE LA CUSTOM TOOLBAR ---

    private fun setupToolbarListeners() {
        // HU 4.0 Criterio 1: El ícono de la estrella (btnCalificar) abre la Play Store de Nequi
        binding.customToolbar.btnCalificar.setOnClickListener {
            val url = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        binding.customToolbar.btnAudio.setOnClickListener {
            val newState = !isAudioOn
            // Al actualizar el ViewModel, el observador en observeSharedAudio() 
            // se activará y llamará a updateAudioIconUI automáticamente.
            sharedAudioViewModel.setAudioOn(newState)
            
            if (newState) {
                Toast.makeText(requireContext(), "Audio: Encendido", Toast.LENGTH_SHORT).show()
                startBackgroundMusic()
            } else {
                Toast.makeText(requireContext(), "Audio: Pausado", Toast.LENGTH_SHORT).show()
                pauseBackgroundMusic()
            }
        }

        // MP1-25 Criterio 4: Ícono instrucciones navega a HU 5.0
        binding.customToolbar.btnInstrucciones.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_instructionsFragment)
        }

        // MP1-26 Criterio 5: Ícono retos navega a HU 6.0 (ChallengesFragment - Natalia)
        binding.customToolbar.btnRetos.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_challengesFragment)
        }

        // MP1-27 Criterio 6: Ícono compartir app lanza BottomSheet nativo con datos de Nequi
        binding.customToolbar.btnCompartir.setOnClickListener {
            val textoACompartir = "App pico botella\nSolo los valientes lo juegan !!\nhttps://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textoACompartir)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir aplicación vía:"))
        }
    }
}