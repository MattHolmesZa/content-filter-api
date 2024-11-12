package com.flash.contentfilterapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
@Schema(description = "Entity representing a restricted word.")
class RestrictedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonIgnore
    var id: Long? = null

    @Column(name = "word", unique = true, nullable = false)
    @Schema(description = "Restricted word to be sanitized.", example = "exampleWord", required = true)
    var word: String = ""
}