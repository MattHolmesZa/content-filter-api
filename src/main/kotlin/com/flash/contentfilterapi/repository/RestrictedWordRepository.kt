package com.flash.contentfilterapi.repository

import com.flash.contentfilterapi.model.RestrictedWord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RestrictedWordRepository: JpaRepository<RestrictedWord, Long> {

    fun findByWord(word: String): RestrictedWord?
}