package co.bdozer.ai.server

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElasticConfiguration {

    @Bean
    fun restHighlevelClient(): RestHighLevelClient {
        val scheme = System.getenv("ELASTIC_SCHEME") ?: "http"
        val hostname = System.getenv("ELASTIC_HOST") ?: "localhost"
        val port = (System.getenv("ELASTIC_PORT") ?: "9200").toInt()
        return RestHighLevelClient(
            RestClient.builder(HttpHost(hostname, port, scheme))
        )
    }
    
}