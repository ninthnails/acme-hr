package com.ninthnails.acme.hierarchy.domain

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class ValidationException(s : String) : IllegalArgumentException(s)