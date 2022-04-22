package co.bdozer.ai.server

import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.DefaultApi
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class ServerConfiguration {

    @Bean
    fun restHighLevelClient(): RestHighLevelClient {
        val uri = URI.create(
            System.getenv("ELASTICSEARCH_ENDPOINT") ?: "http://localhost:9200"
        )
        val httpHost = HttpHost(uri.host, uri.port, uri.scheme)
        return RestHighLevelClient(RestClient.builder(httpHost))
    }

    @Bean
    fun coreNLP(): DefaultApi {
        val apiClient = ApiClient()
        apiClient.basePath = System.getenv("CORE_NLP_ENDPOINT") ?: "http://localhost:8000"
        return apiClient.buildClient(DefaultApi::class.java)
    }
    
}