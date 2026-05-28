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
import com.wt2dadmuvy.spinbot.viewmodel.HomeViewModel
import com.wt2dadmuvy.spinbot.viewmodel.SharedAudioViewModel

class HomeFragment : Fragment() {

    // View Binding: acceso seguro a las vistas del layout sin findViewById
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel propio: maneja la lógica del juego y el countdown
    private val homeViewModel: HomeViewModel by viewModels()

    // ViewModel compartido con ChallengesFragment (HU 6) e InstructionsFragment (HU 5).
    // Permite que esos fragments sepan si el audio estaba ON y soliciten pausarlo/reanudarlo.
    private val sharedAudioViewModel: SharedAudioViewModel by activityViewModels()

    private var backgroundMusic: MediaPlayer? = null

    // HU 11 Criterio 2: sonido corto que acompaña la animación de giro
    private var spinSound: MediaPlayer? = null

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

        // 3. Observar solicitudes de pausa de audio desde HU 5 (Instrucciones) y HU 6 (Retos)
        observeSharedAudio()

        // 4. HU 11: conectar botón "Presióname" con la lógica de giro
        setupSpinButton()
    }

    override fun onResume() {
        super.onResume()
        // Solo reproduce música al volver si el usuario no la ha silenciado manualmente
        if (isAudioOn) {
            startBackgroundMusic()
        }
        // Pre-inicializar el sonido de giro para que esté listo al instante cuando se necesite
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
        _binding = null // Evitamos fugas de memoria (Memory Leaks)
        super.onDestroyView()
    }

    // -------------------------------------------------------------------------
    // ANIMACIONES Y OBSERVERS BASE
    // -------------------------------------------------------------------------

    private fun configureAnimations() {
        val blinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink_button)
        binding.btnPressMe.startAnimation(blinkAnimation)
    }

    private fun observeViewModel() {
        // Actualizar el número del countdown en pantalla
        homeViewModel.countdown.observe(viewLifecycleOwner) { number ->
            binding.tvCountdown.text = number.toString()
        }

        // HU 11: reaccionar a los cambios de estado del juego
        homeViewModel.estadoJuego.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                HomeViewModel.EstadoJuego.GIRANDO          -> onBottellaGirando()
                HomeViewModel.EstadoJuego.CUENTA_REGRESIVA -> onCuentaRegresiva()
                HomeViewModel.EstadoJuego.ESPERANDO_RETO   -> onEsperandoReto()
                HomeViewModel.EstadoJuego.INACTIVO         -> onJuegoInactivo()
            }
        }
    }

    // -------------------------------------------------------------------------
    // HU 11: LÓGICA DEL GIRO
    // -------------------------------------------------------------------------

    /**
     * Conecta el botón "Presióname" con la función de giro del ViewModel.
     * HU 11 Criterio 7: el botón desaparece al presionarlo (se oculta en onBottellaGirando).
     */
    private fun setupSpinButton() {
        binding.btnPressMe.setOnClickListener {
            homeViewModel.iniciarGiro()
        }
    }

    /**
     * Se ejecuta cuando el estado cambia a GIRANDO.
     * HU 11 Criterio 1: inicia la animación de giro de la botella.
     * HU 11 Criterio 2: reproduce el sonido de giro mientras la botella está en movimiento.
     * HU 11 Criterio 7: oculta el botón "Presióname" mientras la botella gira.
     * HU 11 Criterio 8: pausa el audio de fondo durante la partida.
     */
    private fun onBottellaGirando() {
        // Criterio 7: ocultar botón y detener animación de parpadeo
        binding.btnPressMe.clearAnimation()
        binding.btnPressMe.visibility = View.INVISIBLE

        // Criterio 8: pausar audio de fondo si estaba encendido
        if (isAudioOn) pauseBackgroundMusic()

        // Criterio 2: reproducir sonido de giro si el audio está activo
        if (isAudioOn) playSpinSound()

        // Criterio 1 y 3: animar la botella con rotación aleatoria
        val delta = homeViewModel.deltaGiro.value ?: 0f
        animarBotella(delta)
    }

    /**
     * Se ejecuta cuando el estado cambia a CUENTA_REGRESIVA.
     * La botella ya se detuvo. El countdown del ViewModel empieza a contar 3→0.
     * El TextView tvCountdown se actualiza automáticamente via observeViewModel().
     * HU 11 Criterio 2: detiene el sonido de giro porque la botella ya está quieta.
     * HU 11 Criterio 5: el countdown ya es visible en el centro de la botella.
     */
    private fun onCuentaRegresiva() {
        // Criterio 2: detener sonido de giro — la botella ya se detuvo
        stopSpinSound()
        // El countdown se muestra automáticamente — el ViewModel actualiza _countdown
        // y observeViewModel() lo pone en tvCountdown. No hay acción visual adicional.
    }

    /**
     * Se ejecuta cuando el countdown llega a 0.
     * HU 11 Criterio 6: aquí se debe mostrar el diálogo de HU 12 (Mostrar reto aleatorio).
     * HU 11 Criterio 7: el botón vuelve a aparecer.
     * HU 11 Criterio 8: el audio se reactiva cuando el jugador ve el reto.
     *
     * TODO: Cuando alguien implemente HU 12, reemplazar el Toast por:
     *   ShowRandomChallengeDialog().show(childFragmentManager, "RandomChallengeDialog")
     *   El diálogo debe llamar homeViewModel.reiniciarJuego() al cerrar.
     */
    private fun onEsperandoReto() {
        // TODO HU 12: mostrar diálogo con reto aleatorio
        Toast.makeText(
            requireContext(),
            "¡Tiempo! Aquí va el diálogo de reto aleatorio (HU 12 - pendiente de asignación)",
            Toast.LENGTH_LONG
        ).show()

        // Por ahora, reiniciar el juego inmediatamente hasta que exista HU 12
        homeViewModel.reiniciarJuego()
    }

    /**
     * Se ejecuta cuando el juego vuelve al estado INACTIVO.
     * HU 11 Criterio 7: el botón reaparece con animación de parpadeo.
     * HU 11 Criterio 8: el audio de fondo se reanuda si estaba encendido.
     */
    private fun onJuegoInactivo() {
        // Criterio 7: mostrar botón y reactivar parpadeo
        binding.btnPressMe.visibility = View.VISIBLE
        configureAnimations()

        // Criterio 8: reanudar audio si el usuario no lo había apagado manualmente
        if (isAudioOn && backgroundMusic != null) {
            startBackgroundMusic()
        }
    }

    /**
     * Anima la botella con una rotación suave que desacelera al final.
     *
     * HU 11 Criterio 1: la animación dura DURACION_GIRO_MS (4 segundos).
     * HU 11 Criterio 3: el delta viene del ViewModel con un ángulo aleatorio,
     *                    así cada giro para en una dirección diferente.
     * HU 11 Criterio 4: usamos binding.imgBottle.rotation como punto de partida,
     *                    que ya tiene el valor de la última parada — así la botella
     *                    siempre arranca desde donde se quedó.
     *
     * @param delta Cuántos grados adicionales debe girar la botella.
     */
    private fun animarBotella(delta: Float) {
        val rotacionActual = binding.imgBottle.rotation
        val rotacionFinal = rotacionActual + delta

        ObjectAnimator.ofFloat(binding.imgBottle, "rotation", rotacionActual, rotacionFinal).apply {
            duration = HomeViewModel.DURACION_GIRO_MS
            // DecelerateInterpolator: empieza rápido y desacelera suavemente al final
            // El "2f" hace que la desaceleración sea más pronunciada (más realista)
            interpolator = DecelerateInterpolator(2f)
            start()
        }
    }

    // -------------------------------------------------------------------------
    // MÚSICA DE FONDO
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // SONIDO DE GIRO (HU 11 Criterio 2)
    // -------------------------------------------------------------------------

    /**
     * Pre-inicializa el MediaPlayer del sonido de giro sin reproducirlo todavía.
     * Se llama en onResume() para que el archivo ya esté decodificado en memoria
     * y el inicio sea instantáneo cuando el usuario presione el botón.
     */
    private fun initSpinSound() {
        if (spinSound == null) {
            spinSound = MediaPlayer.create(requireContext(), R.raw.spin_sound)?.apply {
                isLooping = true
                setVolume(0.8f, 0.8f)
            }
        }
    }

    /**
     * Inicia el sonido de giro desde el principio.
     * El MediaPlayer ya fue preparado en [initSpinSound], así que start() es inmediato
     * y queda perfectamente sincronizado con el inicio de la animación de la botella.
     * Solo se llama si [isAudioOn] es true.
     */
    private fun playSpinSound() {
        spinSound?.apply {
            seekTo(0)
            start()
        }
    }

    /**
     * Pausa el sonido de giro y regresa al inicio (listo para el próximo giro).
     * Se llama cuando la botella se detiene (CUENTA_REGRESIVA) o al salir de la pantalla.
     */
    private fun stopSpinSound() {
        spinSound?.apply {
            if (isPlaying) {
                pause()
                seekTo(0)
            }
        }
    }

    /**
     * Libera completamente el MediaPlayer del sonido de giro.
     * Solo se llama en onDestroyView() para evitar fugas de memoria.
     */
    private fun releaseSpinSound() {
        spinSound?.release()
        spinSound = null
    }

    // -------------------------------------------------------------------------
    // AUDIO COMPARTIDO (HU 5 y HU 6)
    // -------------------------------------------------------------------------

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
        sharedAudioViewModel.pauseRequested.observe(viewLifecycleOwner) { shouldPause ->
            if (shouldPause) {
                pauseBackgroundMusic()
            } else {
                if (isAudioOn && backgroundMusic != null) {
                    startBackgroundMusic()
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // LÓGICA DE LA CUSTOM TOOLBAR
    // -------------------------------------------------------------------------

    private fun setupToolbarListeners() {
        // MP1-23 Criterio 2: Ícono estrella manda a simulación de tienda (Nequi)
        binding.customToolbar.btnCalificar.setOnClickListener {
            val playStoreUrl = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            startActivity(intent)
        }

        // MP1-24 Criterio 3: Interruptor del audio de fondo (ON / OFF) con cambio visual y de MediaPlayer
        binding.customToolbar.btnAudio.setOnClickListener {
            isAudioOn = !isAudioOn
            // Sincronizar el estado con SharedAudioViewModel para que HU 5 y HU 6 lo lean
            sharedAudioViewModel.setAudioOn(isAudioOn)
            if (isAudioOn) {
                binding.customToolbar.btnAudio.setImageResource(R.drawable.volume_up)
                Toast.makeText(requireContext(), "Audio: Encendido", Toast.LENGTH_SHORT).show()
                startBackgroundMusic()
            } else {
                binding.customToolbar.btnAudio.setImageResource(R.drawable.volume_off)
                Toast.makeText(requireContext(), "Audio: Pausado", Toast.LENGTH_SHORT).show()
                pauseBackgroundMusic()
                // Si la botella estaba girando, también detener su sonido
                stopSpinSound()
            }
            val colorNaranja = ContextCompat.getColor(requireContext(), R.color.orange)
            binding.customToolbar.btnAudio.imageTintList = ColorStateList.valueOf(colorNaranja)
        }

        // MP1-25 Criterio 4: Ícono instrucciones navega a HU 5.0
        binding.customToolbar.btnInstrucciones.setOnClickListener {
            Toast.makeText(requireContext(), "Navegación a Instrucciones (Pendiente por el equipo)", Toast.LENGTH_SHORT).show()
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
