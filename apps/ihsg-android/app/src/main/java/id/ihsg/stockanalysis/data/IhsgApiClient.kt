package id.ihsg.stockanalysis.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class IhsgApiClient(
    private val baseUrl: String,
    private val client: OkHttpClient = defaultClient
) {
    suspend fun health(): ApiResult<String> = getJson("/api/v1/health") { json ->
        json.optString("status", "connected")
    }

    suspend fun dashboard(): ApiResult<DashboardSnapshot> {
        val watchlistResult = watchlist()
        if (watchlistResult is ApiResult.Err) return watchlistResult

        val symbols = (watchlistResult as ApiResult.Ok).value.ifEmpty { defaultWatchlist }
        val quotes = symbols.mapNotNull { symbol ->
            when (val quote = quote(symbol)) {
                is ApiResult.Ok -> quote.value
                is ApiResult.Err -> null
            }
        }

        val tasks = when (val taskResult = tasks()) {
            is ApiResult.Ok -> taskResult.value
            is ApiResult.Err -> emptyList()
        }
        val history = when (val historyResult = history()) {
            is ApiResult.Ok -> historyResult.value
            is ApiResult.Err -> emptyList()
        }

        return ApiResult.Ok(DashboardSnapshot(symbols, quotes, tasks, history))
    }

    suspend fun watchlist(): ApiResult<List<String>> = getJson("/api/v1/stocks/watchlist") { json ->
        json.optJSONArray("stock_codes").toStringList().filter { IhsgSymbol.isValid(IhsgSymbol.normalize(it)) }
    }

    suspend fun addWatchlist(symbol: String): ApiResult<List<String>> =
        watchlistMutation("/api/v1/stocks/watchlist/add", symbol)

    suspend fun removeWatchlist(symbol: String): ApiResult<List<String>> =
        watchlistMutation("/api/v1/stocks/watchlist/remove", symbol)

    suspend fun quote(symbol: String): ApiResult<StockQuote> =
        getJson("/api/v1/stocks/${encode(symbol)}/quote") { it.toStockQuote() }

    suspend fun analyze(symbol: String): ApiResult<TaskInfo> = postJson(
        "/api/v1/analysis",
        JSONObject()
            .put("stock_code", symbol)
            .put("report_type", "detailed")
            .put("force_refresh", false)
            .put("async_mode", true)
            .put("analysis_phase", "auto")
            .put("notify", false)
            .put("report_language", "en")
    ) { it.toTaskInfo(stockCodeFallback = symbol) }

    suspend fun marketReview(): ApiResult<TaskInfo> = postJson(
        "/api/v1/analysis/market-review",
        JSONObject()
            .put("send_notification", false)
            .put("report_language", "en")
    ) { it.toTaskInfo(stockCodeFallback = "^JKSE") }

    suspend fun tasks(): ApiResult<List<TaskInfo>> = getJson("/api/v1/analysis/tasks") { json ->
        json.optJSONArray("tasks").toTaskList()
    }

    suspend fun history(): ApiResult<List<HistoryItem>> =
        getJson("/api/v1/history?page=1&limit=12") { json ->
            json.optJSONArray("items").toHistoryList()
        }

    private suspend fun watchlistMutation(path: String, symbol: String): ApiResult<List<String>> = postJson(
        path,
        JSONObject().put("stock_code", symbol)
    ) { json ->
        json.optJSONArray("stock_codes").toStringList().filter { IhsgSymbol.isValid(IhsgSymbol.normalize(it)) }
    }

    private suspend fun <T> getJson(path: String, parser: (JSONObject) -> T): ApiResult<T> =
        requestJson(Request.Builder().url(url(path)).get().build(), parser)

    private suspend fun <T> postJson(path: String, body: JSONObject, parser: (JSONObject) -> T): ApiResult<T> {
        val requestBody = body.toString().toRequestBody(JSON)
        val request = Request.Builder().url(url(path)).post(requestBody).build()
        return requestJson(request, parser)
    }

    private suspend fun <T> requestJson(request: Request, parser: (JSONObject) -> T): ApiResult<T> =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val bodyText = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext ApiResult.Err(readError(bodyText, response.code))
                    }
                    val json = if (bodyText.isBlank()) JSONObject() else JSONObject(bodyText)
                    ApiResult.Ok(parser(json))
                }
            } catch (e: IOException) {
                ApiResult.Err("Tidak bisa terhubung ke backend IHSG: ${e.message}")
            } catch (e: Exception) {
                ApiResult.Err("Respons backend tidak bisa dibaca: ${e.message}")
            }
        }

    private fun url(path: String): String = "${baseUrl.trimEnd('/')}$path"

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
        val defaultWatchlist = listOf("BBCA.JK", "TLKM.JK", "ASII.JK")
        private val defaultClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}

private fun readError(bodyText: String, code: Int): String {
    return runCatching {
        val json = JSONObject(bodyText)
        val detail = json.opt("detail")
        when (detail) {
            is JSONObject -> detail.optString("message", detail.toString())
            is String -> detail
            else -> json.optString("message", "Backend mengembalikan HTTP $code")
        }
    }.getOrElse { "Backend mengembalikan HTTP $code" }
}

private fun JSONObject.toStockQuote(): StockQuote = StockQuote(
    stockCode = optString("stock_code"),
    stockName = optStringOrNull("stock_name"),
    currentPrice = optDoubleSafe("current_price"),
    change = optDoubleOrNull("change"),
    changePercent = optDoubleOrNull("change_percent"),
    open = optDoubleOrNull("open"),
    high = optDoubleOrNull("high"),
    low = optDoubleOrNull("low"),
    prevClose = optDoubleOrNull("prev_close"),
    volume = optDoubleOrNull("volume"),
    updateTime = optStringOrNull("update_time")
)

private fun JSONObject.toTaskInfo(stockCodeFallback: String? = null): TaskInfo = TaskInfo(
    taskId = optString("task_id", optString("trace_id", "")),
    stockCode = optString("stock_code", stockCodeFallback ?: ""),
    status = optString("status", "pending"),
    progress = optInt("progress", if (optString("status") == "completed") 100 else 0),
    message = optStringOrNull("message"),
    error = optStringOrNull("error"),
    createdAt = optStringOrNull("created_at"),
    reportText = optStringOrNull("market_review_report")
)

private fun JSONObject.toHistoryItem(): HistoryItem = HistoryItem(
    queryId = optString("query_id"),
    stockCode = optString("stock_code"),
    stockName = optStringOrNull("stock_name"),
    sentimentScore = if (has("sentiment_score") && !isNull("sentiment_score")) optInt("sentiment_score") else null,
    operationAdvice = optStringOrNull("operation_advice") ?: optStringOrNull("action_label"),
    analysisSummary = optStringOrNull("analysis_summary"),
    currentPrice = optDoubleOrNull("current_price"),
    changePct = optDoubleOrNull("change_pct"),
    createdAt = optStringOrNull("created_at")
)

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return List(length()) { index -> optString(index) }.filter { it.isNotBlank() }
}

private fun JSONArray?.toTaskList(): List<TaskInfo> {
    if (this == null) return emptyList()
    return List(length()) { index -> optJSONObject(index)?.toTaskInfo() }
        .filterNotNull()
        .take(10)
}

private fun JSONArray?.toHistoryList(): List<HistoryItem> {
    if (this == null) return emptyList()
    return List(length()) { index -> optJSONObject(index)?.toHistoryItem() }
        .filterNotNull()
        .filter { it.stockCode.endsWith(".JK") }
}

private fun JSONObject.optStringOrNull(name: String): String? =
    if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

private fun JSONObject.optDoubleOrNull(name: String): Double? =
    if (has(name) && !isNull(name)) optDouble(name) else null

private fun JSONObject.optDoubleSafe(name: String): Double =
    if (has(name) && !isNull(name)) optDouble(name) else 0.0
