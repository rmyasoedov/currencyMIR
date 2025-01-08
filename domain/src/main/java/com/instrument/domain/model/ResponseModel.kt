package com.instrument.domain.model

data class ResponseModel <T>(
    var error: Boolean = false,
    var errorMessage: String?=null,
    var code: Int?=null,
    var body: T?=null
)
