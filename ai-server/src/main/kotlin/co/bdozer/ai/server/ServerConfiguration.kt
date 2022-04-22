package co.bdozer.ai.server

import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.CoreNlpApi
import co.bdozer.core.nlp.sdk.api.DefaultApi
import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class ServerConfiguration {

    @Bean
    fun restHighLevelClient(): RestHighLevelClient {
        val elasticsearchEndpoint = System.getenv("ELASTICSEARCH_ENDPOINT") ?: "http://localhost:9200"
        val elasticsearchCredential = System.getenv("ELASTICSEARCH_CREDENTIAL") ?: ""
        
        val uri = URI.create(elasticsearchEndpoint)
        val httpHost = HttpHost(uri.host, uri.port, uri.scheme)
        val headers = arrayOf(BasicHeader("Authorization", "Basic $elasticsearchCredential"))
        
        val builder = RestClient
            .builder(httpHost)
            .setDefaultHeaders(headers)
        return RestHighLevelClient(builder)
    }

    @Bean
    fun coreNLP(): CoreNlpApi {
        val apiClient = ApiClient()
        apiClient.basePath = System.getenv("CORE_NLP_ENDPOINT") ?: "http://localhost:8000"
        return apiClient.buildClient(CoreNlpApi::class.java)
    }

}