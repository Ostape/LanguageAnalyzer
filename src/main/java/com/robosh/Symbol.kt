package com.robosh

data class Symbol(
    var numLine:Int = 0,
    var lexeme: String? = null,
    var token: Token? = null,
    var index:Int = 0
)
