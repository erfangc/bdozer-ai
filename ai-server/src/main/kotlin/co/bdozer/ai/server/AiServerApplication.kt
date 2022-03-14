package co.bdozer.ai.server

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AiServerApplication(private val edgar10KParser: Edgar10KParser) : CommandLineRunner {
	override fun run(vararg args: String?) {
		edgar10KParser.readLatestFilingRss()
	}

}

fun main(args: Array<String>) {
	runApplication<AiServerApplication>(*args)
}
