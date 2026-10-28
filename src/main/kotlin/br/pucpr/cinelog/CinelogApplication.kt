package br.pucpr.cinelog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CinelogApplication

fun main(args: Array<String>) {
    runApplication<CinelogApplication>(*args)
}
