package com.robosh

import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class LexAnalyzer internal constructor() {
    var textCode: String? = null
        private set
    private val mapOfLanguageTokens: MutableMap<String?, Token> = HashMap()
    private val mapIdRealInteger: MutableMap<Int, Token> = HashMap()
    private val mapOfVar: MutableMap<String?, Int> = HashMap()
    private val mapOfConst: MutableMap<String?, Int> = HashMap()
    private val errStates: MutableList<Int> = ArrayList()
    private val starStates: MutableList<Int> = ArrayList()
    private var lexeme: String? = null
    private var state = 0
    private var currentChar = 0.toChar()
    private var finalStates: MutableList<Int> = ArrayList()
    private var token: Token? = null
    private var numChar = -1
    private var numLine = 1
    private var mapOfStates: MutableMap<State, Int> = HashMap()
    private var classChar: String? = null

    @Throws(IOException::class)
    private fun readFile(path: String) {
        val stringBuilder = StringBuilder()
        FileReader(path).use { fileReader ->
            val scanner = Scanner(fileReader)
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n")
            }
        }
        textCode = stringBuilder.toString()
    }

    fun analyze(path: String) {
        try {
            readFile(path)
        } catch (e: IOException) {
            System.err.println("Error while reading file\n$e")
        }
        while (numChar < textCode!!.length - 1) {
            currentChar = nextChar()
            classChar = defineChar(currentChar)
            state = nextState(state, classChar)
            if (isFinal(state)) {
                processing()
                if (errStates.contains(state)) {
                    break
                }
            } else if (state == 0) {
                lexeme = ""
            } else {
                lexeme += currentChar
            }
        }
    }

    private fun processing() {
        if (state == 13) {
            numLine++
            state = 0
        }
        if (state == 2 || state == 6 || state == 9) {
            token = getToken(state, lexeme)
            if (token !== Token.KEYWORD) {
                val index = indexVarConst(state, lexeme)
                println("$numLine $lexeme $token $index")
                TableOfSymb.add(Symbol(numLine, lexeme, token, index))
            } else {
                println("$numLine $lexeme $token")
                TableOfSymb.add(Symbol(numLine, lexeme, token, 0))
            }
            lexeme = ""
            numChar = putCharBack(numChar)
            state = 0
        }
        if (state == 12 || state == 14) {
            lexeme += currentChar
            token = getToken(state, lexeme)
            println("$numLine $lexeme $token")
            TableOfSymb.add(Symbol(numLine, lexeme, token, 0))
            lexeme = ""
            state = 0
        }
        if (state == 120) {
            token = getToken(state, lexeme)
            println("$numLine $lexeme $token")
            TableOfSymb.add(Symbol(numLine, lexeme, token, 0))
            numChar = putCharBack(numChar)
            lexeme = ""
            state = 0
        }
    }

    private fun nextState(state: Int, classChar: String?): Int {
        var integer = mapOfStates!![State(state, classChar!!)]
        if (integer == null) {
            integer = mapOfStates!![State(state, "other")]
        }
        return integer!!
    }

    private fun indexVarConst(state: Int, lexeme: String?): Int {
        var indx = 0
        if (state == 2) {
            try {
                indx = mapOfVar[lexeme]!!
            } catch (e: Exception) {
                indx = mapOfVar.size + 1
                mapOfVar[lexeme] = indx
            }
        }
        if (state == 6) {
            try {
                indx = mapOfConst[lexeme]!!
            } catch (e: Exception) {
                indx = mapOfConst.size + 1
                mapOfConst[lexeme] = indx
            }
        }
        if (state == 9) {
            try {
                indx = mapOfConst[lexeme]!!
            } catch (e: Exception) {
                indx = mapOfConst.size + 1
                mapOfConst[lexeme] = indx
            }
        }
        return indx
    }

    private fun initLexeme() {
        lexeme = ""
    }

    private fun initState() {
        state = 0
    }

    private fun isFinal(state: Int): Boolean {
        return finalStates.contains(state)
    }

    private fun putCharBack(numChar: Int): Int {
        return numChar - 1
    }

    private fun initMapOfLanguageTokens() {
        mapOfLanguageTokens["if"] = Token.KEYWORD
        mapOfLanguageTokens["true"] = Token.KEYWORD
        mapOfLanguageTokens["false"] = Token.KEYWORD
        mapOfLanguageTokens["integer"] = Token.KEYWORD
        mapOfLanguageTokens["entry"] = Token.KEYWORD
        mapOfLanguageTokens["end"] = Token.KEYWORD
        mapOfLanguageTokens["real"] = Token.KEYWORD
        mapOfLanguageTokens["boolean"] = Token.KEYWORD
        mapOfLanguageTokens["scan"] = Token.KEYWORD
        mapOfLanguageTokens["print"] = Token.KEYWORD
        mapOfLanguageTokens["for"] = Token.KEYWORD
        mapOfLanguageTokens["to"] = Token.KEYWORD
        mapOfLanguageTokens["do"] = Token.KEYWORD
        mapOfLanguageTokens["="] = Token.ASSIGN_OP
        mapOfLanguageTokens["+"] = Token.ADD_OP
        mapOfLanguageTokens["-"] = Token.ADD_OP
        mapOfLanguageTokens["*"] = Token.MULT_OP
        mapOfLanguageTokens["/"] = Token.MULT_OP
        mapOfLanguageTokens["<"] = Token.REL_OP
        mapOfLanguageTokens[">"] = Token.REL_OP
        mapOfLanguageTokens["<="] = Token.REL_OP
        mapOfLanguageTokens[">="] = Token.REL_OP
        mapOfLanguageTokens["=="] = Token.REL_OP
        mapOfLanguageTokens["("] = Token.BRACKETS_OP
        mapOfLanguageTokens[")"] = Token.BRACKETS_OP
        mapOfLanguageTokens["."] = Token.PUNCT
        mapOfLanguageTokens[","] = Token.PUNCT
        mapOfLanguageTokens[":"] = Token.PUNCT
        mapOfLanguageTokens[";"] = Token.OP_END
        mapOfLanguageTokens["{"] = Token.START_BLOCK
        mapOfLanguageTokens["}"] = Token.END_BLOCK
    }

    private fun initMapIdRealInteger() {
        mapIdRealInteger[2] = Token.IDENT
        mapIdRealInteger[6] = Token.REALNUM
        mapIdRealInteger[9] = Token.INTNUM
    }

    private fun initStarStates() {
        starStates.add(2)
        starStates.add(6)
        starStates.add(9)
    }

    private fun initErrStates() {
        errStates.add(101)
        errStates.add(102)
    }

    private fun initFinalState() {
        finalStates.add(2)
        finalStates.add(6)
        finalStates.add(9)
        finalStates.add(12)
        finalStates.add(13)
        finalStates.add(14)
        finalStates.add(21)
        finalStates.add(101)
        finalStates.add(102)
        finalStates.add(120)
    }

    private fun initMapOfStates() {
        mapOfStates[State(0, "")] = 0
        mapOfStates[State(0, "Letter")] = 1
        mapOfStates[State(1, "Letter")] = 1
        mapOfStates[State(1, "Digit")] = 1
        mapOfStates[State(1, "other")] = 2
        mapOfStates[State(0, "Digit")] = 4
        mapOfStates[State(4, "Digit")] = 4
        mapOfStates[State(4, "Dot")] = 5
        mapOfStates[State(4, "other")] = 9
        mapOfStates[State(5, "Digit")] = 5
        mapOfStates[State(5, "other")] = 6
        mapOfStates[State(0, "=")] = 11
        mapOfStates[State(0, ">")] = 15
        mapOfStates[State(0, "<")] = 15
        mapOfStates[State(15, "=")] = 14
        mapOfStates[State(15, "other")] = 120
        mapOfStates[State(11, "=")] = 12
        mapOfStates[State(0, "ws")] = 0
        mapOfStates[State(0, "{")] = 14
        mapOfStates[State(0, "}")] = 14
        mapOfStates[State(0, "nl")] = 13
        mapOfStates[State(0, "+")] = 14
        mapOfStates[State(0, "-")] = 14
        mapOfStates[State(0, "*")] = 14
        mapOfStates[State(0, ",")] = 14
        mapOfStates[State(0, "/")] = 14
        mapOfStates[State(0, "(")] = 14
        mapOfStates[State(0, ")")] = 14
        mapOfStates[State(0, ";")] = 14
        mapOfStates[State(0, "other")] = 101
        mapOfStates[State(11, "other")] = 120
    }

    private fun getToken(state: Int, lexeme: String?): Token? {
        var token = mapOfLanguageTokens!![lexeme]
        if (token == null) {
            token = mapIdRealInteger!![state]
        }
        return token
    }

    private fun nextChar(): Char {
        numChar++
        return textCode!![numChar]
    }

    private fun defineChar(c: Char): String {
        val str = c.toString()
        var res = ""
        when {
            str.matches("[A-Za-z]".toRegex()) -> {
                res = "Letter"
            }
            str == "." -> {
                res = "Dot"
            }
            str.matches("[0-9]".toRegex()) -> {
                res = "Digit"
            }
            str == " " -> {
                res = "ws"
            }
            str == "\n" -> {
                res = "nl"
            }
            "+-*/=()><{},;".contains(str) -> {
                res = str
            }
        }
        return res
    }

    init {
        initMapOfLanguageTokens()
        initMapIdRealInteger()
        initMapOfStates()
        initFinalState()
        initErrStates()
        initStarStates()
        initLexeme()
        initState()
    }
}
