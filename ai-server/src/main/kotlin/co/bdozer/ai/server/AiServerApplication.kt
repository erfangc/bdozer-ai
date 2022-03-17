package co.bdozer.ai.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AiServerApplication

fun main(args: Array<String>) {
    runApplication<AiServerApplication>(*args)
}
