package co.bdozer.indexer

import co.bdozer.utils.Beans
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.common.xcontent.XContentType

object Indexer {

    private val objectMapper = Beans.objectMapper()
    private val restHighLevelClient = Beans.restHighLevelClient()

    fun index(id: String, obj: Any): IndexResponse {
        val idx = obj::class.java.name
        val json = objectMapper.writeValueAsString(obj)
        return restHighLevelClient.index(
            IndexRequest(idx)
                .id(id)
                .source(json, XContentType.JSON),
            RequestOptions.DEFAULT
        )
    }
}