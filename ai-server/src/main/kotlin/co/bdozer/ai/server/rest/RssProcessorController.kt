package co.bdozer.ai.server.rest

import co.bdozer.ai.server.services.rss.RssProcessor
import co.bdozer.ai.server.services.rss.models.Entry
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RssProcessorController(
    private val rssProcessor: RssProcessor,
) {
    @GetMapping("entries")
    fun entries(
        @RequestParam start: Int? = null,
        @RequestParam count: Int? = null,
    ): List<Entry> {
        return rssProcessor.entries(start, count)
    }
}