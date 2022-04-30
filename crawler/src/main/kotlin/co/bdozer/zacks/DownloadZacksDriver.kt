package co.bdozer.zacks

import co.bdozer.utils.Beans
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.zip.ZipInputStream

private val downloadLinks = mapOf(
    "fc" to URI.create("https://www.quandl.com/api/v3/datatables/ZACKS/MT?api_key=sxfxY8ZT3ghJxx9S-Thu&qopts.export=true"),
    "fr" to URI.create("https://www.quandl.com/api/v3/datatables/ZACKS/MT?api_key=sxfxY8ZT3ghJxx9S-Thu&qopts.export=true"),
    "mt" to URI.create("https://www.quandl.com/api/v3/datatables/ZACKS/MT?api_key=sxfxY8ZT3ghJxx9S-Thu&qopts.export=true"),
    "shrs" to URI.create("https://www.quandl.com/api/v3/datatables/ZACKS/MT?api_key=sxfxY8ZT3ghJxx9S-Thu&qopts.export=true"),
    "mktv" to URI.create("https://www.quandl.com/api/v3/datatables/ZACKS/MT?api_key=sxfxY8ZT3ghJxx9S-Thu&qopts.export=true"),
)
private val httpClient = HttpClient
    .newBuilder()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .build()
private val outputDir = "."
private val objectMapper = Beans.objectMapper()

fun main() {
    val mt = downloadLinks["mt"]!!
    val zacksDownloadLink = zacksDownloadLink(mt)
    val link = zacksDownloadLink.datatable_bulk_download.file.link
    println("Download $link")
    val httpResponse = httpClient
        .send(
            HttpRequest
                .newBuilder(URI.create(link))
                .build(),
            HttpResponse.BodyHandlers.ofInputStream(),
        )
    println("Response headers=${httpResponse.headers()}")
    val body = httpResponse.body()
    val zipInputStream = ZipInputStream(body)

    zipInputStream.nextEntry?.let {
        println("${it.name} ${it.size}")
        zipInputStream
            .bufferedReader()
            .lines()
            .forEach { line ->
                println(line)
            }
    }

    zipInputStream.close()
}

private fun zacksDownloadLink(mt: URI): ZacksDownloadLink {
    val bytes = httpClient
        .send(
            HttpRequest
                .newBuilder(mt)
                .GET()
                .header("user-agent", "curl/7.79.1")
                .header("accept", "*/*")
                .build(),
            HttpResponse.BodyHandlers.ofByteArray()
        ).body()
    return objectMapper.readValue(bytes)
}
