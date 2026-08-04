package id.ihsg.stockanalysis.data

data class StockQuote(
    val stockCode: String,
    val stockName: String?,
    val currentPrice: Double,
    val change: Double?,
    val changePercent: Double?,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val prevClose: Double?,
    val volume: Double?,
    val updateTime: String?
)

data class TaskInfo(
    val taskId: String,
    val stockCode: String,
    val status: String,
    val progress: Int,
    val message: String?,
    val error: String?,
    val createdAt: String?,
    val reportText: String?
)

data class HistoryItem(
    val queryId: String,
    val stockCode: String,
    val stockName: String?,
    val sentimentScore: Int?,
    val operationAdvice: String?,
    val analysisSummary: String?,
    val currentPrice: Double?,
    val changePct: Double?,
    val createdAt: String?
)

data class DashboardSnapshot(
    val watchlist: List<String>,
    val quotes: List<StockQuote>,
    val tasks: List<TaskInfo>,
    val history: List<HistoryItem>
)

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val message: String) : ApiResult<Nothing>()
}
