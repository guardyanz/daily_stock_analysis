package id.ihsg.stockanalysis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.ihsg.stockanalysis.data.ApiResult
import id.ihsg.stockanalysis.data.HistoryItem
import id.ihsg.stockanalysis.data.IhsgApiClient
import id.ihsg.stockanalysis.data.IhsgSymbol
import id.ihsg.stockanalysis.data.SettingsStore
import id.ihsg.stockanalysis.data.StockQuote
import id.ihsg.stockanalysis.data.TaskInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class IhsgUiState(
    val baseUrl: String = "",
    val symbolInput: String = "",
    val watchlist: List<String> = IhsgApiClient.defaultWatchlist,
    val quotes: List<StockQuote> = emptyList(),
    val tasks: List<TaskInfo> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val connectionStatus: String = "Belum dicek",
    val busy: Boolean = false,
    val error: String? = null,
    val selectedReport: String? = null
)

class IhsgViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = SettingsStore(application)
    private val _uiState = MutableStateFlow(IhsgUiState(baseUrl = settings.getBaseUrl()))
    val uiState: StateFlow<IhsgUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        refresh()
        startPollingTasks()
    }

    fun onBaseUrlChanged(value: String) {
        _uiState.update { it.copy(baseUrl = value, error = null) }
    }

    fun saveBaseUrl() {
        val normalized = uiState.value.baseUrl.trim().trimEnd('/')
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            _uiState.update { it.copy(error = "URL backend harus diawali http:// atau https://") }
            return
        }
        settings.setBaseUrl(normalized)
        _uiState.update { it.copy(baseUrl = normalized) }
        refresh()
    }

    fun onSymbolInputChanged(value: String) {
        _uiState.update { it.copy(symbolInput = value.uppercase(), error = null) }
    }

    fun addSymbol() {
        val symbol = IhsgSymbol.normalize(uiState.value.symbolInput)
        if (!IhsgSymbol.isValid(symbol)) {
            _uiState.update { it.copy(error = "Kode harus saham IDX, contoh BBCA atau BBCA.JK") }
            return
        }
        launchBusy {
            when (val result = api().addWatchlist(symbol)) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(watchlist = result.value.ifEmpty { IhsgApiClient.defaultWatchlist }, symbolInput = "")
                }
                is ApiResult.Err -> _uiState.update { it.copy(error = result.message) }
            }
            refresh()
        }
    }

    fun removeSymbol(symbol: String) {
        launchBusy {
            when (val result = api().removeWatchlist(symbol)) {
                is ApiResult.Ok -> _uiState.update { it.copy(watchlist = result.value.ifEmpty { IhsgApiClient.defaultWatchlist }) }
                is ApiResult.Err -> _uiState.update { it.copy(error = result.message) }
            }
            refresh()
        }
    }

    fun refresh() {
        launchBusy {
            when (val health = api().health()) {
                is ApiResult.Ok -> _uiState.update { it.copy(connectionStatus = "Backend aktif: ${health.value}", error = null) }
                is ApiResult.Err -> _uiState.update { it.copy(connectionStatus = "Backend belum tersambung", error = health.message) }
            }

            when (val dashboard = api().dashboard()) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(
                        watchlist = dashboard.value.watchlist,
                        quotes = dashboard.value.quotes,
                        tasks = dashboard.value.tasks,
                        history = dashboard.value.history
                    )
                }
                is ApiResult.Err -> _uiState.update { it.copy(error = dashboard.message) }
            }
        }
    }

    fun analyze(symbol: String) {
        launchBusy {
            when (val result = api().analyze(symbol)) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(tasks = listOf(result.value) + it.tasks, error = null)
                }
                is ApiResult.Err -> _uiState.update { it.copy(error = result.message) }
            }
            refreshTasks()
        }
    }

    fun marketReview() {
        launchBusy {
            when (val result = api().marketReview()) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(tasks = listOf(result.value) + it.tasks, error = null)
                }
                is ApiResult.Err -> _uiState.update { it.copy(error = result.message) }
            }
            refreshTasks()
        }
    }

    fun selectReport(report: String?) {
        _uiState.update { it.copy(selectedReport = report) }
    }

    private fun startPollingTasks() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(15_000)
                refreshTasks()
            }
        }
    }

    private suspend fun refreshTasks() {
        when (val result = api().tasks()) {
            is ApiResult.Ok -> _uiState.update { it.copy(tasks = result.value) }
            is ApiResult.Err -> Unit
        }
    }

    private fun launchBusy(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, error = null) }
            try {
                block()
            } finally {
                _uiState.update { it.copy(busy = false) }
            }
        }
    }

    private fun api(): IhsgApiClient = IhsgApiClient(uiState.value.baseUrl)
}
