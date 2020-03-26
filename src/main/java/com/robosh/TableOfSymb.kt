package com.robosh

import java.util.*


object TableOfSymb {
    var map: MutableMap<Int, Symbol> = HashMap()
    fun mapToString(): String {
        val builder = StringBuilder()
        for ((key, value) in map) {
            builder.append("""$key : $value""")
        }
        return builder.toString()
    }

    fun add(symbol: Symbol) {
        map[map.size + 1] = symbol
    }
}
