package com.wt2dadmuvy.spinbot.viewmodel

import com.wt2dadmuvy.spinbot.util.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.wt2dadmuvy.spinbot.model.Challenge
import com.wt2dadmuvy.spinbot.repository.ChallengeRepository
import com.wt2dadmuvy.spinbot.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ChallengesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var repository: ChallengeRepository

    private lateinit var viewModel: ChallengesViewModel

    private val challengeEjemplo = Challenge(id = "abc123", name = "Reto 1", description = "Descripción del reto")

    @Before
    fun setUp() {
        // allChallenges se accede en el init del ViewModel, debe devolver algo válido
        whenever(repository.allChallenges).thenReturn(MutableLiveData(emptyList()))
        viewModel = ChallengesViewModel(repository)
    }

    // --- Tests de insert ---

    @Test
    fun `insert llama a addRemoteChallenge con el reto correcto`() = runTest {
        val challengeConId = challengeEjemplo.copy(id = "firestore-id-generado")
        whenever(repository.addRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.success(challengeConId)) })

        viewModel.insert(challengeEjemplo)

        verify(repository).addRemoteChallenge(challengeEjemplo)
    }

    @Test
    fun `insert en exito llama a insertLocal con el challenge que retorna Firestore`() = runTest {
        val challengeConId = challengeEjemplo.copy(id = "firestore-id-generado")
        whenever(repository.addRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.success(challengeConId)) })

        viewModel.insert(challengeEjemplo)

        verify(repository).insertLocal(challengeConId)
    }

    @Test
    fun `insert con fallo en Firestore no llama a insertLocal`() = runTest {
        whenever(repository.addRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.failure(Exception("Error de red"))) })

        viewModel.insert(challengeEjemplo)

        verify(repository).addRemoteChallenge(challengeEjemplo)
        verify(repository, never()).insertLocal(any())
    }

    // --- Tests de update ---

    @Test
    fun `update llama a updateLocal con el reto correcto`() = runTest {
        whenever(repository.updateRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.success(Unit)) })

        viewModel.update(challengeEjemplo)

        verify(repository).updateLocal(challengeEjemplo)
    }

    @Test
    fun `update llama a updateRemoteChallenge con el reto correcto`() = runTest {
        whenever(repository.updateRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.success(Unit)) })

        viewModel.update(challengeEjemplo)

        verify(repository).updateRemoteChallenge(challengeEjemplo)
    }

    // --- Tests de delete ---

    @Test
    fun `delete llama a deleteLocal con el reto correcto`() = runTest {
        whenever(repository.deleteRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.success(Unit)) })

        viewModel.delete(challengeEjemplo)

        verify(repository).deleteLocal(challengeEjemplo)
    }

    @Test
    fun `delete llama a deleteRemoteChallenge con el reto correcto`() = runTest {
        whenever(repository.deleteRemoteChallenge(challengeEjemplo))
            .thenReturn(flow { emit(Result.success(Unit)) })

        viewModel.delete(challengeEjemplo)

        verify(repository).deleteRemoteChallenge(challengeEjemplo)
    }

    // --- Test de allChallenges ---

    @Test
    fun `allChallenges expone la lista del repositorio`() {
        val listaEsperada = listOf(challengeEjemplo)
        whenever(repository.allChallenges).thenReturn(MutableLiveData(listaEsperada))

        viewModel = ChallengesViewModel(repository)

        assertEquals(listaEsperada, viewModel.allChallenges.value)
    }
}
