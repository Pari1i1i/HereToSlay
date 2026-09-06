package com.fachri.heretoslay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fachri.heretoslay.data.model.Card
import com.fachri.heretoslay.data.model.Monster
import com.fachri.heretoslay.data.model.Player
import com.fachri.heretoslay.data.model.RoomState
import com.fachri.heretoslay.data.model.RoomStatus
import com.fachri.heretoslay.data.repository.AttackResult
import com.fachri.heretoslay.data.repository.AuthRepository
import com.fachri.heretoslay.data.repository.CardRepository
import com.fachri.heretoslay.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val roomCode: String = "",
    val roomState: RoomState? = null,
    val localUid: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedCardId: String? = null,
    val attackResult: AttackResult? = null,
    val isPerformingAction: Boolean = false,
) {
    val myPlayer: Player?
        get() = roomState?.players?.firstOrNull { it.uid == localUid }

    val isMyTurn: Boolean
        get() = roomState?.currentPlayer?.uid == localUid

    val opponents: List<Player>
        get() = roomState?.players?.filter { it.uid != localUid } ?: emptyList()

    val actionPointsRemaining: Int
        get() = myPlayer?.actionPointsRemaining ?: 0

    val isGameOver: Boolean
        get() = roomState?.status == RoomStatus.FINISHED
}

class GameViewModel(
    private val roomCode: String,
    private val gameRepository: GameRepository = GameRepository(),
    private val cardRepository: CardRepository = CardRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState(roomCode = roomCode))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        observeGame()
    }

    private fun observeGame() {
        viewModelScope.launch {
            val uid = authRepository.getOrSignIn()
            _uiState.update { it.copy(localUid = uid) }

            gameRepository.observeGame(roomCode, uid)
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
                    // If it's a Bot's turn and local user is Host, auto-play bot turn after small delay
                    checkAndRunBotTurn(state)
                }
                .launchIn(viewModelScope)
        }
    }

    private fun checkAndRunBotTurn(state: RoomState?) {
        if (state == null || state.status != RoomStatus.PLAYING) return
        val current = state.currentPlayer ?: return
        val isHost = state.hostId == _uiState.value.localUid
        if (current.uid.startsWith("bot_") && isHost) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200) // Realistic turn delay
                // Bot does draw card or attack or end turn
                if (current.actionPointsRemaining >= 1) {
                    gameRepository.drawCard(roomCode, current.uid)
                    kotlinx.coroutines.delay(800)
                }
                gameRepository.endTurn(roomCode, current.uid)
            }
        }
    }

    fun selectCard(cardId: String) {
        _uiState.update {
            it.copy(selectedCardId = if (it.selectedCardId == cardId) null else cardId)
        }
    }

    fun drawCard() {
        if (!_uiState.value.isMyTurn || _uiState.value.actionPointsRemaining < 1) return
        _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }

        viewModelScope.launch {
            val result = gameRepository.drawCard(roomCode, _uiState.value.localUid)
            _uiState.update {
                it.copy(
                    isPerformingAction = false,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun playSelectedCard() {
        val cardId = _uiState.value.selectedCardId ?: return
        if (!_uiState.value.isMyTurn) return
        val card = cardRepository.findCard(cardId) ?: return
        if (_uiState.value.actionPointsRemaining < card.category.apCost) return

        _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }

        viewModelScope.launch {
            val result = gameRepository.playCard(roomCode, _uiState.value.localUid, cardId)
            _uiState.update {
                it.copy(
                    isPerformingAction = false,
                    selectedCardId = if (result.isSuccess) null else it.selectedCardId,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun attackMonster(monsterId: String) {
        if (!_uiState.value.isMyTurn || _uiState.value.actionPointsRemaining < 2) return
        _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }

        viewModelScope.launch {
            val d1 = (1..6).random()
            val d2 = (1..6).random()
            val result = gameRepository.attackMonster(roomCode, _uiState.value.localUid, monsterId, d1, d2)
            _uiState.update {
                it.copy(
                    isPerformingAction = false,
                    attackResult = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun endTurn() {
        if (!_uiState.value.isMyTurn) return
        _uiState.update { it.copy(isPerformingAction = true, errorMessage = null) }

        viewModelScope.launch {
            val result = gameRepository.endTurn(roomCode, _uiState.value.localUid)
            _uiState.update {
                it.copy(
                    isPerformingAction = false,
                    selectedCardId = null,
                    errorMessage = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun dismissAttackResult() {
        _uiState.update { it.copy(attackResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Resolvers for UI
    fun resolveCard(id: String): Card? = cardRepository.findCard(id)
    fun resolveCards(ids: List<String>): List<Card> = cardRepository.findCards(ids)
    fun resolveMonster(id: String): Monster? = cardRepository.findMonster(id)
    fun resolveMonsters(ids: List<String>): List<Monster> = cardRepository.findMonsters(ids)
}
