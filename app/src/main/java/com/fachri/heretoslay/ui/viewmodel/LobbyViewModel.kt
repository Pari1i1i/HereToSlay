package com.fachri.heretoslay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fachri.heretoslay.data.model.RoomState
import com.fachri.heretoslay.data.model.RoomStatus
import com.fachri.heretoslay.data.remote.FirestoreSchema
import com.fachri.heretoslay.data.repository.AuthRepository
import com.fachri.heretoslay.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LobbyUiState(
    val roomCode: String = "",
    val roomState: RoomState? = null,
    val localUid: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isStartingGame: Boolean = false,
) {
    val isHost: Boolean
        get() = roomState?.hostId == localUid

    val isLocalPlayerReady: Boolean
        get() = roomState?.players?.firstOrNull { it.uid == localUid }?.isReady == true

    val canStartGame: Boolean
        get() {
            val players = roomState?.players ?: return false
            return isHost && players.size >= 2 && players.all { it.isReady }
        }
}

class LobbyViewModel(
    private val roomCode: String,
    private val roomRepository: RoomRepository = RoomRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState(roomCode = roomCode))
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        observeLobby()
    }

    private fun observeLobby() {
        viewModelScope.launch {
            val uid = authRepository.getOrSignIn()
            _uiState.update { it.copy(localUid = uid) }

            roomRepository.observeRoom(roomCode, uid)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .onEach { state ->
                    _uiState.update {
                        it.copy(
                            roomState = state,
                            isLoading = false,
                            errorMessage = if (state == null) "Room tidak ditemukan atau telah ditutup." else null
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun toggleReady() {
        val currentReady = _uiState.value.isLocalPlayerReady
        val uid = _uiState.value.localUid
        viewModelScope.launch {
            val result = roomRepository.setPlayerReady(roomCode, uid, !currentReady)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun toggleHouseRule(ruleField: String, currentState: Boolean) {
        if (!_uiState.value.isHost) return
        viewModelScope.launch {
            val result = roomRepository.updateHouseRule(roomCode, ruleField, !currentState)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun setTurnDuration(seconds: Int) {
        if (!_uiState.value.isHost) return
        viewModelScope.launch {
            val result = roomRepository.updateTurnDuration(roomCode, seconds)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun startGame(onSuccess: () -> Unit) {
        if (!_uiState.value.canStartGame) return
        _uiState.update { it.copy(isStartingGame = true, errorMessage = null) }
        viewModelScope.launch {
            val result = roomRepository.startGame(roomCode)
            _uiState.update { it.copy(isStartingGame = false) }
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun leaveRoom(onLeft: () -> Unit) {
        val uid = _uiState.value.localUid
        viewModelScope.launch {
            roomRepository.leaveRoom(roomCode, uid)
            onLeft()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
