package com.example.engine

import kotlin.math.*

class ShadowEvaluator(input: String, private val isRadians: Boolean = false) {
    private val normalized = normalize(input)
    private var pos = -1
    private var ch = 0

    init {
        // Validate unbalanced parentheses at start if any
        var openCount = 0
        for (char in normalized) {
            if (char == '(') openCount++
            else if (char == ')') {
                openCount--
                if (openCount < 0) {
                    throw IllegalArgumentException("Mismatching parentheses")
                }
            }
        }
        if (openCount != 0) {
            throw IllegalArgumentException("Unbalanced parentheses")
        }
    }

    private fun nextChar() {
        pos++
        ch = if (pos < normalized.length) normalized[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        if (normalized.trim().isEmpty()) return 0.0
        nextChar()
        val x = parseExpression()
        if (pos < normalized.length) throw IllegalArgumentException("Unexpected character near: " + ch.toChar())
        return if (x.isNaN()) {
            throw ArithmeticException("Result is not a number")
        } else if (x.isInfinite()) {
            throw ArithmeticException("Division by zero or overflow")
        } else {
            x
        }
    }

    // expression = term | expression `+` term | expression `-` term
    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) {
                x += parseTerm()
            } else if (eat('-'.code)) {
                x -= parseTerm()
            } else {
                break
            }
        }
        return x
    }

    // term = factor | term `*` factor | term `/` factor
    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) {
                x *= parseFactor()
            } else if (eat('/'.code)) {
                val next = parseFactor()
                if (next == 0.0) throw ArithmeticException("Division by zero")
                x /= next
            } else {
                break
            }
        }
        return x
    }

    // factor = unary | factor `^` unary
    private fun parseFactor(): Double {
        var x = parseUnary()
        while (true) {
            if (eat('^'.code)) {
                x = x.pow(parseUnary())
            } else {
                break
            }
        }
        return x
    }

    // unary = `+` unary | `-` unary | base
    private fun parseUnary(): Double {
        if (eat('+'.code)) return parseUnary()
        if (eat('-'.code)) return -parseUnary()
        return parseBase()
    }

    // base = number | `(` expression `)` | function | math constants
    private fun parseBase(): Double {
        val startPos = pos
        var value = 0.0

        if (eat('('.code)) {
            value = parseExpression()
            if (!eat(')'.code)) throw IllegalArgumentException("Missing closing parenthesis")
        } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
            while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
            val numStr = normalized.substring(startPos, pos)
            value = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
        } else if (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch == '√'.code || ch == 'π'.code) {
            if (eat('π'.code)) {
                value = PI
            } else if (eat('e'.code)) {
                value = E
            } else {
                val isSqrtSymbol = eat('√'.code)
                val funcName: String
                if (isSqrtSymbol) {
                    funcName = "sqrt"
                } else {
                    while (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code) nextChar()
                    funcName = normalized.substring(startPos, pos)
                }

                // Now evaluate arguments
                val arg: Double
                if (eat('('.code)) {
                    arg = parseExpression()
                    if (!eat(')'.code)) throw IllegalArgumentException("Missing closing parenthesis for $funcName")
                } else {
                    // Constant identifier or single trailing unary factor
                    if (funcName == "pi") {
                        value = PI
                        return handlePostfix(value)
                    } else if (funcName == "e") {
                        value = E
                        return handlePostfix(value)
                    } else if (funcName == "p") {
                        // could be part of pi
                        value = PI
                        return handlePostfix(value)
                    } else {
                        arg = parseUnary()
                    }
                }

                value = when (funcName) {
                    "sin" -> {
                        val radians = if (isRadians) arg else Math.toRadians(arg)
                        // Round small values to 0 for exact display (e.g. sin(180) or sin(2*pi) ~= 0)
                        val s = sin(radians)
                        if (abs(s) < 1e-15) 0.0 else s
                    }
                    "cos" -> {
                        val radians = if (isRadians) arg else Math.toRadians(arg)
                        val c = cos(radians)
                        if (abs(c) < 1e-15) 0.0 else c
                    }
                    "tan" -> {
                        val radians = if (isRadians) arg else Math.toRadians(arg)
                        val c = cos(radians)
                        if (abs(c) < 1e-15) throw ArithmeticException("Undefined (Tangent asymptote)")
                        val t = tan(radians)
                        if (abs(t) < 1e-15) 0.0 else t
                    }
                    "log" -> {
                        if (arg <= 0.0) throw IllegalArgumentException("Log domain error")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0.0) throw IllegalArgumentException("Ln domain error")
                        ln(arg)
                    }
                    "sqrt" -> {
                        if (arg < 0.0) throw IllegalArgumentException("Sqrt domain error")
                        sqrt(arg)
                    }
                    else -> throw IllegalArgumentException("Unknown function: $funcName")
                }
            }
        } else {
            throw IllegalArgumentException("Invalid token")
        }

        return handlePostfix(value)
    }

    private fun handlePostfix(initialVal: Double): Double {
        var value = initialVal
        while (true) {
            if (eat('!'.code)) {
                if (value < 0 || value != floor(value)) {
                    throw IllegalArgumentException("Factorial needs positive integer")
                }
                value = factorial(value.toInt()).toDouble()
            } else if (eat('%'.code)) {
                value /= 100.0
            } else {
                break
            }
        }
        return value
    }

    private fun factorial(n: Int): Long {
        if (n < 0 || n > 20) throw IllegalArgumentException("Factorial range error (0-20 support)")
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    private fun normalize(str: String): String {
        var text = str
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "π")

        // 1. Number followed by parenthesis, constant, or function: e.g., '2(' -> '2*(', '2sin' -> '2*sin', '2π' -> '2*π'
        text = text.replace(Regex("(\\d)\\("), "$1*(")
        text = text.replace(Regex("(\\d)(sin|cos|tan|log|ln|sqrt|√|π|e)"), "$1*$2")

        // 2. Parenthesis/constant/suffix followed by number: e.g., ')2' -> ')*2', 'π2' -> 'π*2', '!2' -> '!*2'
        text = text.replace(Regex("\\)(\\d)"), ")*$1")
        text = text.replace(Regex("(π|e|!)(\\d)"), "$1*$2")

        // 3. Close parenthesis followed by open parenthesis: ')' '(' -> ')*('
        text = text.replace(Regex("\\)\\("), ")*(")

        // 4. Constants followed by functions: e.g. 'πsin' -> 'π*sin'
        text = text.replace(Regex("(π|e)(sin|cos|tan|log|ln|sqrt|√|π|e)"), "$1*$2")

        return text
    }
}
