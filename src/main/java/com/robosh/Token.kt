package com.robosh

enum class Token {
    IDENT,
    INTNUM,
    REALNUM,
    BOOLVAL,
    KEYWORD,
    ASSIGN_OP,
    ADD_OP,
    MULT_OP,
    REL_OP,
    BRACKETS_OP,
    PUNCT,
    WS,
    EOL,
    EOF,
    START_BLOCK,
    END_BLOCK,
    OP_END
}
