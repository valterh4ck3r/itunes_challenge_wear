package com.valternegreiros.itunes_challenge_wear.ui.core.connectivity

import com.valternegreiros.itunes_challenge_wear.MainDispatcherRule
import com.valternegreiros.itunes_challenge_wear.data.connectivity.NetworkConnectivityObserver
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observer: NetworkConnectivityObserver = mockk()
    private val isConnectedFlow = MutableStateFlow(true)
    private lateinit var viewModel: ConnectivityViewModel

    @Before
    fun setUp() {
        every { observer.isConnected } throws Exception("Mock is not initialized")
    }

    @Test
    fun `should start with HIDDEN when initially connected`() = runTest {
        // Given
        every { observer.isConnected } returns MutableStateFlow(true)

        // When
        viewModel = ConnectivityViewModel(observer)

        // Then
        assertEquals(ConnectivityStatus.HIDDEN, viewModel.status.value)
    }

    @Test
    fun `should show DISCONNECTED when connection is lost`() = runTest {
        // Given
        val flow = MutableStateFlow(true)
        every { observer.isConnected } returns flow
        viewModel = ConnectivityViewModel(observer)

        // When
        flow.value = false

        // Then
        assertEquals(ConnectivityStatus.DISCONNECTED, viewModel.status.value)
    }

    @Test
    fun `should show CONNECTED then HIDDEN after 3s when connection is restored`() = runTest {
        // Given
        val flow = MutableStateFlow(true)
        every { observer.isConnected } returns flow
        viewModel = ConnectivityViewModel(observer)
        
        // Go offline
        flow.value = false
        assertEquals(ConnectivityStatus.DISCONNECTED, viewModel.status.value)

        // When: Restore connection
        flow.value = true

        // Then: Should be CONNECTED immediately
        assertEquals(ConnectivityStatus.CONNECTED, viewModel.status.value)

        // And: Should be HIDDEN after 3 seconds
        advanceTimeBy(3001)
        assertEquals(ConnectivityStatus.HIDDEN, viewModel.status.value)
    }
}
