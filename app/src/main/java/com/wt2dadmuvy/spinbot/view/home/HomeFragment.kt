package com.wt2dadmuvy.spinbot.view.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wt2dadmuvy.spinbot.R
import com.wt2dadmuvy.spinbot.databinding.FragmentHomeBinding
import com.wt2dadmuvy.spinbot.view.dialog.RandomChallengeDialogFragment
import com.wt2dadmuvy.spinbot.viewmodel.HomeViewModel
import com.wt2dadmuvy.spinbot.viewmodel.SharedAudioViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    private var backgroundMusic: MediaPlayer? = null
    private var spinSound: MediaPlayer? = null
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
        setupToolbarListeners()
        configureAnimations()
        observeViewModel()
        setupRandomChallengeDialogObserver()
        homeViewModel.startCountdownPreview()
        observeSharedAudio()
        setupSpinButton()
    }

    override fun onResume() {
        super.onResume()
        if (isAudioOn) startBackgroundMusic()
        initSpinSound()
    }

    override fun onPause() {
        stopSpinSound()
        pauseBackgroundMusic()
        super.onPause()
    }

    override fun onDestroyView() {
        releaseSpinSound()
        releaseBackgroundMusic()
        _binding = null
        super.onDestroyView()
    }

    private fun configureAnimations() {
        val pulseAnimation1 = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_ripple)
        val pulseAnimation2 = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_ripple)
        pulseAnimation2.startOffset = 1000
        binding.viewPulse1.startAnimation(pulseAnimation1)
        binding.viewPulse2.startAnimation(pulseAnimation2)
    }

    private fun observeViewModel() {
        homeViewModel.countdown.observe(viewLifecycleOwner) { number ->
            binding.tvCountdown.text = number.toString()
        }

        homeViewModel.estadoJuego.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                HomeViewModel.EstadoJuego.GIRANDO          -> onBottellaGirando()
                HomeViewModel.EstadoJuego.CUENTA_REGRESIVA -> onCuentaRegresiva()
                HomeViewModel.EstadoJuego.ESPERANDO_RETO   -> onEsperandoReto()
                HomeViewModel.EstadoJuego.INACTIVO         -> onJuegoInactivo()
            }
        }
    }

    private fun setupSpinButton() {
        binding.btnPressMe.setOnClickListener {
            // Audio directo en el click para sincronía inmediata con la animación
            if (isAudioOn) {
                pauseBackgroundMusic()
                playSpinSound()
            }
            homeViewModel.iniciarGiro()
        }
    }

    private fun onBottellaGirando() {
        binding.btnPressMe.clearAnimation()
        binding.btnPressMe.visibility = View.INVISIBLE
        animarBotella(homeViewModel.deltaGiro.value ?: 0f)
    }

    private fun onCuentaRegresiva() {
        stopSpinSound()
    }

    private fun onEsperandoReto() {
        homeViewModel.cargarRetoAleatorioParaDialogo()
    }

    private fun setupRandomChallengeDialogObserver() {
        childFragmentManager.setFragmentResultListener(
            RandomChallengeDialogFragment.REQUEST_KEY_CLOSED,
            viewLifecycleOwner
        ) { _, _ ->
            homeViewModel.reiniciarJuego()
        }

        homeViewModel.randomChallengeDialogData.observe(viewLifecycleOwner) { dialogData ->
            dialogData ?: return@observe

            if (childFragmentManager.findFragmentByTag(RANDOM_CHALLENGE_DIALOG_TAG) == null) {
                RandomChallengeDialogFragment.newInstance(
                    challengeText = dialogData.challengeDescription,
                    pokemonImageUrl = dialogData.pokemonImageUrl
                ).show(childFragmentManager, RANDOM_CHALLENGE_DIALOG_TAG)
            }

            homeViewModel.limpiarDatosDialogoReto()
        }
    }

    private fun onJuegoInactivo() {
        binding.btnPressMe.visibility = View.VISIBLE
        configureAnimations()
        if (isAudioOn && backgroundMusic != null) startBackgroundMusic()
    }

    // Rota la botella desde su posición actual con desaceleración progresiva
    private fun animarBotella(delta: Float) {
        val rotacionActual = binding.imgBottle.rotation
        ObjectAnimator.ofFloat(binding.imgBottle, "rotation", rotacionActual, rotacionActual + delta).apply {
            duration = HomeViewModel.DURACION_GIRO_MS
            interpolator = DecelerateInterpolator(2f)
            start()
        }
    }

    private fun startBackgroundMusic() {
        if (backgroundMusic == null) {
            backgroundMusic = MediaPlayer.create(requireContext(), R.raw.game_background).apply {
                isLooping = true
                setVolume(0.45f, 0.45f)
            }
        }
        backgroundMusic?.let { if (!it.isPlaying) it.start() }
    }

    private fun pauseBackgroundMusic() {
        backgroundMusic?.let { if (it.isPlaying) it.pause() }
    }

    private fun releaseBackgroundMusic() {
        backgroundMusic?.release()
        backgroundMusic = null
    }

    // Pre-cargado en onResume para que esté listo al instante cuando el usuario presione el botón
    private fun initSpinSound() {
        if (spinSound == null) {
            spinSound = MediaPlayer.create(requireContext(), R.raw.spin_sound)?.apply {
                isLooping = false
                setVolume(0.8f, 0.8f)
            }
        }
    }

    private fun playSpinSound() {
        spinSound?.apply { seekTo(0); start() }
    }

    private fun stopSpinSound() {
        spinSound?.apply { if (isPlaying) { pause(); seekTo(0) } }
    }

    private fun releaseSpinSound() {
        spinSound?.release()
        spinSound = null
    }

    private fun observeSharedAudio() {
        sharedAudioViewModel.isAudioOn.observe(viewLifecycleOwner) { isOn ->
            isAudioOn = isOn
            updateAudioIconUI(isOn)
        }

        sharedAudioViewModel.pauseRequested.observe(viewLifecycleOwner) { shouldPause ->
            if (shouldPause) pauseBackgroundMusic()
            else if (isAudioOn && backgroundMusic != null) startBackgroundMusic()
        }
    }

    private fun updateAudioIconUI(isOn: Boolean) {
        binding.customToolbar.btnAudio.setImageResource(
            if (isOn) R.drawable.volume_up else R.drawable.volume_off
        )
        binding.customToolbar.btnAudio.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.orange)
        )
    }

    private fun setupToolbarListeners() {
        binding.customToolbar.btnCalificar.setOnClickListener {
            val url = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        binding.customToolbar.btnAudio.setOnClickListener {
            val newState = !isAudioOn
            sharedAudioViewModel.setAudioOn(newState)
            if (newState) {
                Toast.makeText(requireContext(), "Audio: Encendido", Toast.LENGTH_SHORT).show()
                startBackgroundMusic()
            } else {
                Toast.makeText(requireContext(), "Audio: Pausado", Toast.LENGTH_SHORT).show()
                pauseBackgroundMusic()
                stopSpinSound()
            }
        }

        binding.customToolbar.btnInstrucciones.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_instructionsFragment)
        }

        binding.customToolbar.btnRetos.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_challengesFragment)
        }

        binding.customToolbar.btnCompartir.setOnClickListener {
            val texto = "App pico botella\nSolo los valientes lo juegan !!\nhttps://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir aplicación vía:"))
        }
    }


    companion object {
        private const val RANDOM_CHALLENGE_DIALOG_TAG = "RandomChallengeDialog"
    }
}
