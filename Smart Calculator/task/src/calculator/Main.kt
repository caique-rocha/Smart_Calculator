package calculator

import java.math.BigInteger
import java.util.*

val memory = mutableMapOf<String, BigInteger>()
fun main() {

    val mathRegex = Regex("^([a-zA-Z]+|\\d+)(?:( *[-+*/]+ *\\(*([a-zA-Z]+|\\d+\\)*))+)\$")
    val expressionRegex = Regex("^[a-zA-Z]+ *= *-*([a-zA-Z]+|\\d+)\$")
    val identifierRegex = Regex("[a-zA-Z]+$|\\d+")
    val scanner = Scanner(System.`in`)
    //println(calcPosFix(infixToPosFix("3 + 8 *((4 + 3) * 2 + 1) - 6 /(2 + 1)")))

    while (true) {

        val input = scanner.nextLine().trim()
        val arrayInput = input.replace("=", " ").split(" ").toTypedArray()


        if (input.isEmpty()) continue

        if (input == "/exit") {
            println("Bye!")
            break
        }

        if (input == "/help") {
            println("The program calculates the sum of numbers!")
            continue
        }

        if (input.matches(Regex("^/.*$"))) {
            println("Unknown command")
            continue
        }

        if (input.matches(mathRegex)) {

            println(calcPosFix(infixToPosFix(input)))

        } else if (input.matches(expressionRegex)) {

            if (arrayInput.last().matches(Regex("-*\\d+$"))) {

                memory[arrayInput.first()] = arrayInput.last().toBigInteger()
                //println(memory)
            } else {

                if (memory.containsKey(arrayInput.last())) {

                    memory[arrayInput.first()] = memory.getValue(arrayInput.last())
                } else {

                    println("Unknown variable")
                }
            }

        } else if (arrayInput.first().matches(identifierRegex) && arrayInput.size == 1) {

            if (memory.containsKey(arrayInput.first())) {
                println(memory[arrayInput.first()])
            } else {
                println("Unknown variable")
            }

        } else if (!arrayInput.first().matches(identifierRegex)) {

            println("Invalid identifier")
        } else {
            println("Invalid assignment")
        }
    }
}

fun calc(op: String, value1: BigInteger, value2: BigInteger): BigInteger {

    return when (op) {
        "+" -> value2 + value1
        "-" -> value2 - value1
        "/" -> value2 / value1
        else -> value2 * value1
    }
}

fun calcPosFix(posFix: String): String {

    if (posFix == "Invalid expression") return "Invalid expression"
    val expression = posFix.split(" ")
    val stack = Stack<String>()

    for (term in expression) {

        if (isInteger(term)) {

            stack.push(term)
        } else {

            if (stack.size > 1)
                stack.push(calc(term, stack.pop().toBigInteger(), stack.pop().toBigInteger()).toString())


        }
    }
    return stack.peek()
}

fun infixToPosFix(infix: String): String {

    val expression = correctExpression(infix)
    var posFix = ""

    val stack = Stack<String>()

    if (expression == null) return "Invalid expression"

    for (term in expression) {

        when {
            isInteger(term) -> {
                posFix += "$term "
            }
            memory.containsKey(term) -> {

                posFix += "${memory[term]} "
            }
            term == "(" -> {

                stack.push(term)
            }
            term == ")" -> {

                try {
                    while (!stack.empty() && stack.peek() != "(") {

                        posFix += "${stack.pop()} "
                    }
                    stack.pop()
                } catch (e: EmptyStackException){return "Invalid expression"}

            }
            else -> {

                while (!stack.empty() &&
                    getPrecedence(term) <= getPrecedence(stack.peek())
                ) {

                    posFix += "${stack.pop()} "
                }
                stack.push(term)
            }
        }

    }
    while (!stack.empty()) {

        if (stack.peek() == "(") return "Invalid expression"
        posFix += "${stack.pop()} "

    }
    return posFix
}

fun isInteger(str: String?) = str?.toBigIntegerOrNull()?.let { true } ?: false

fun getPrecedence(ch: String): Int {
    return if (ch == "+" || ch == "-") 1
    else if (ch == "*" || ch == "/") 2
    else if (ch == "^") 3
    else -1
}

fun correctExpression(expression: String): List<String>? {

    val correct =
        expression.replace("(", "( ").
        replace(")", " )").
        split(Regex(" ")).toMutableList()

    //println(correct)
    for (i in correct.indices) {
        if (correct[i].matches(Regex("([*/]){2,}|[^/*+\\-a-zA-Z\\d()]")) || correct[i].isEmpty()) return null
        if (memory.containsKey(correct[i])) correct[i] = memory[correct[i]].toString()
        if (!isInteger(correct[i]) && correct[i] != " " &&
            correct[i] != "(" && correct[i] != ")" &&
            correct[i] != "*" && correct[i] != "/"
        ) correct[i] = checkOp(correct[i])
    }
    return correct.toList()
}

fun checkOp(op: String): String {

    var minus = 0
    for (i in op) {
        if (i == '-') minus++
    }

    return if (minus % 2 == 0) {
        "+"
    } else {
        "-"
    }
}