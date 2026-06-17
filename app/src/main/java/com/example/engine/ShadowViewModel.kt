package com.example.engine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HistoryEntity
import com.example.data.HistoryRepository
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShadowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HistoryRepository(database.historyDao)
    }

    // Calculation states
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _isRadians = MutableStateFlow(false) // Default is DEG
    val isRadians: StateFlow<Boolean> = _isRadians.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Premium black/gold is default
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _memory = MutableStateFlow(0.0)
    val memory: StateFlow<Double> = _memory.asStateFlow()

    // Hidden section triggers
    private val _logoTaps = MutableStateFlow(0)
    val logoTaps: StateFlow<Int> = _logoTaps.asStateFlow()

    private val _isSecretVisible = MutableStateFlow(false)
    val isSecretVisible: StateFlow<Boolean> = _isSecretVisible.asStateFlow()

    // History stream from Room DB
    val history: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onLogoClicked() {
        viewModelScope.launch {
            val nextC = _logoTaps.value + 1
            _logoTaps.value = nextC
            if (nextC >= 5) {
                _isSecretVisible.value = true
                _logoTaps.value = 0
            }
        }
    }

    fun closeSecretSection() {
        _isSecretVisible.value = false
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleAngleUnit() {
        _isRadians.value = !_isRadians.value
        updatePreview()
    }

    fun appendText(txt: String) {
        viewModelScope.launch {
            val cur = _expression.value
            // Prevent double decimals, or consecutive operators depending on input
            if (isOperator(txt) && cur.isNotEmpty() && isOperator(cur.last().toString())) {
                _expression.value = cur.dropLast(1) + txt
            } else {
                _expression.value = cur + txt
            }
            updatePreview()
        }
    }

    fun appendFunction(func: String) {
        viewModelScope.launch {
            _expression.value = _expression.value + func + "("
            updatePreview()
        }
    }

    fun clearAll() {
        _expression.value = ""
        _result.value = ""
        _previewResult.value = ""
    }

    fun deleteLast() {
        viewModelScope.launch {
            val expr = _expression.value
            if (expr.isNotEmpty()) {
                // If it ends with letters + '(' e.g. "sin(", delete whole function
                val functions = listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
                var deleted = false
                for (func in functions) {
                    if (expr.endsWith(func)) {
                        _expression.value = expr.substring(0, expr.length - func.length)
                        deleted = true
                        break
                    }
                }
                if (!deleted) {
                    _expression.value = expr.dropLast(1)
                }
                updatePreview()
            }
        }
    }

    fun handleMemory(action: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val valueToUse = if (_result.value.isNotEmpty() && _result.value != "Error") {
                    _result.value.toDoubleOrNull() ?: 0.0
                } else if (_expression.value.isNotEmpty()) {
                    val eval = ShadowEvaluator(_expression.value, _isRadians.value).parse()
                    eval
                } else {
                    0.0
                }

                when (action) {
                    "MC" -> _memory.value = 0.0
                    "MR" -> {
                        // Recall memory by appending to calculation
                        val formatted = formatDouble(_memory.value)
                        _expression.value = _expression.value + formatted
                        updatePreview()
                    }
                    "M+" -> {
                        _memory.value = _memory.value + valueToUse
                    }
                    "M-" -> {
                        _memory.value = _memory.value - valueToUse
                    }
                }
            } catch (e: Exception) {
                // Silent catch on invalid memory calc
            }
        }
    }

    fun performCalculation() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentExpr = _expression.value
            if (currentExpr.isEmpty()) return@launch

            try {
                val evaluator = ShadowEvaluator(currentExpr, _isRadians.value)
                val rawResult = evaluator.parse()
                val formatted = formatDouble(rawResult)

                _result.value = formatted
                _previewResult.value = ""

                // Save calculation history into secure, local database
                repository.insertHistory(currentExpr, formatted)
            } catch (e: ArithmeticException) {
                _result.value = e.message ?: "Arithmetic Error"
            } catch (e: IllegalArgumentException) {
                _result.value = "Format Error"
            } catch (e: Exception) {
                _result.value = "Error"
            }
        }
    }

    private fun updatePreview() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentExpr = _expression.value
            if (currentExpr.isEmpty()) {
                _previewResult.value = ""
                return@launch
            }
            try {
                // Keep input validation live to preview calculation on the fly
                val evaluator = ShadowEvaluator(currentExpr, _isRadians.value)
                val rawResult = evaluator.parse()
                _previewResult.value = "= " + formatDouble(rawResult)
            } catch (e: Exception) {
                _previewResult.value = "" // Silently hide live preview if incomplete or contains format error
            }
        }
    }

    fun clearDatabaseHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun useHistoryItem(expr: String) {
        _expression.value = expr
        updatePreview()
    }

    private fun isOperator(s: String): Boolean {
        return s == "+" || s == "-" || s == "−" || s == "*" || s == "×" || s == "/" || s == "÷" || s == "^"
    }

    private fun formatDouble(d: Double): String {
        if (d.isInfinite() || d.isNaN()) return "Error"
        // Return beautiful scientific representation or standard decimals
        if (abs(d) >= 1e12 || (abs(d) < 1e-6 && d != 0.0)) {
            return String.format("%.6e", d)
        }
        val longVal = d.toLong()
        return if (d == longVal.toDouble()) {
            longVal.toString()
        } else {
            // Trim trailing zeros gracefully
            var res = String.format("%.8f", d)
            while (res.endsWith("0")) {
                res = res.dropLast(1)
            }
            if (res.endsWith(".")) {
                res = res.dropLast(1)
            }
            res
        }
    }
}
