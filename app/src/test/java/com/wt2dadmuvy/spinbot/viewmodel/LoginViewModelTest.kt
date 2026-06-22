package com.wt2dadmuvy.spinbot.viewmodel

import com.wt2dadmuvy.spinbot.util.InstantTaskExecutorRule
import com.wt2dadmuvy.spinbot.repository.AuthRepository
import com.wt2dadmuvy.spinbot.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    // Hace que LiveData ejecute sus observers de forma síncrona en tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Reemplaza Dispatchers.Main para que viewModelScope funcione sin Android
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(authRepository)
    }

    // --- Tests de validación de campos ---

    @Test
    fun `onPasswordChanged con menos de 6 digitos establece mensaje de error`() {
        viewModel.onPasswordChanged("123")

        assertEquals(LoginViewModel.MIN_PASSWORD_MESSAGE, viewModel.passwordError.value)
    }

    @Test
    fun `onPasswordChanged con 6 o mas digitos limpia el error`() {
        viewModel.onPasswordChanged("123456")

        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun `onPasswordChanged con campo vacio no muestra error`() {
        viewModel.onPasswordChanged("")

        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun `actionsEnabled es true cuando email y password tienen datos`() {
        viewModel.onEmailChanged("usuario@test.com")
        viewModel.onPasswordChanged("123456")

        assertTrue(viewModel.actionsEnabled.value == true)
    }

    @Test
    fun `actionsEnabled es false cuando el email esta vacio`() {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("123456")

        assertEquals(false, viewModel.actionsEnabled.value)
    }

    @Test
    fun `actionsEnabled es false cuando el password esta vacio`() {
        viewModel.onEmailChanged("usuario@test.com")
        viewModel.onPasswordChanged("")

        assertEquals(false, viewModel.actionsEnabled.value)
    }

    // --- Tests de visibilidad de contraseña ---

    @Test
    fun `togglePasswordVisibility cambia passwordVisible de false a true`() {
        assertEquals(false, viewModel.passwordVisible.value)

        viewModel.togglePasswordVisibility()

        assertEquals(true, viewModel.passwordVisible.value)
    }

    @Test
    fun `togglePasswordVisibility dos veces regresa al estado original`() {
        viewModel.togglePasswordVisibility()
        viewModel.togglePasswordVisibility()

        assertEquals(false, viewModel.passwordVisible.value)
    }

    // --- Tests de autenticación ---

    @Test
    fun `loginUser con credenciales validas emite authState exitoso`() = runTest {
        whenever(authRepository.loginUser("test@test.com", "123456"))
            .thenReturn(flow { emit(Result.success(Unit)) })

        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("123456")
        viewModel.loginUser()

        assertTrue(viewModel.authState.value!!.isSuccess)
    }

    @Test
    fun `loginUser con credenciales invalidas emite authState con fallo`() = runTest {
        val exception = Exception("Login incorrecto")
        whenever(authRepository.loginUser("bad@test.com", "000000"))
            .thenReturn(flow { emit(Result.failure(exception)) })

        viewModel.onEmailChanged("bad@test.com")
        viewModel.onPasswordChanged("000000")
        viewModel.loginUser()

        assertTrue(viewModel.authState.value!!.isFailure)
    }

    @Test
    fun `registerUser exitoso emite authState exitoso`() = runTest {
        whenever(authRepository.registerUser("nuevo@test.com", "123456"))
            .thenReturn(flow { emit(Result.success(Unit)) })

        viewModel.onEmailChanged("nuevo@test.com")
        viewModel.onPasswordChanged("123456")
        viewModel.registerUser()

        assertTrue(viewModel.authState.value!!.isSuccess)
    }

    @Test
    fun `registerUser con email existente emite authState con fallo`() = runTest {
        val exception = Exception("Error en el registro")
        whenever(authRepository.registerUser("existente@test.com", "123456"))
            .thenReturn(flow { emit(Result.failure(exception)) })

        viewModel.onEmailChanged("existente@test.com")
        viewModel.onPasswordChanged("123456")
        viewModel.registerUser()

        assertTrue(viewModel.authState.value!!.isFailure)
    }

    @Test
    fun `logoutUser llama al repositorio y activa logoutState`() {
        viewModel.logoutUser()

        verify(authRepository).logoutUser()
        assertTrue(viewModel.logoutState.value)
    }
}
