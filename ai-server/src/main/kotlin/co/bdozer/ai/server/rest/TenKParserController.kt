package co.bdozer.ai.server.rest

import co.bdozer.ai.server.services.tenkparser.TenKParser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors

@RestController
class TenKParserController(private val tenKParser: TenKParser) {

    private val executor = Executors.newCachedThreadPool()

    @PostMapping("parse-10k")
    fun parse10k(@RequestParam cik: String, @RequestParam ash: String): ParseTenKAcknowledgement {
        val requestId = UUID.randomUUID().toString()
        executor.execute { tenKParser.parse10k(cik = cik, ash = ash) }
        return ParseTenKAcknowledgement(
            status = "submitted",
            timestamp = Instant.now(),
            requestId = requestId,
        )
    }

}