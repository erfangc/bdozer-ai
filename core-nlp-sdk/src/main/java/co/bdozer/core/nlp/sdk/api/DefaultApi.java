package co.bdozer.core.nlp.sdk.api;

import co.bdozer.core.nlp.sdk.ApiClient;
import co.bdozer.core.nlp.sdk.EncodingUtils;
import co.bdozer.core.nlp.sdk.model.ApiResponse;

import co.bdozer.core.nlp.sdk.model.CrossEncodeInput;
import co.bdozer.core.nlp.sdk.model.DocInput;
import co.bdozer.core.nlp.sdk.model.HTTPValidationError;
import co.bdozer.core.nlp.sdk.model.ScoredSentence;
import co.bdozer.core.nlp.sdk.model.Sentences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import feign.*;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-03-13T17:04:42.570-04:00[America/New_York]")
public interface DefaultApi extends ApiClient.Api {


  /**
   * Cross Encode
   * Cross Encoder
   * @param crossEncodeInput  (required)
   * @return List&lt;ScoredSentence&gt;
   */
  @RequestLine("POST /cross_encode")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  List<ScoredSentence> crossEncode(CrossEncodeInput crossEncodeInput);

  /**
   * Cross Encode
   * Similar to <code>crossEncode</code> but it also returns the http response headers .
   * Cross Encoder
   * @param crossEncodeInput  (required)
   * @return A ApiResponse that wraps the response boyd and the http headers.
   */
  @RequestLine("POST /cross_encode")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  ApiResponse<List<ScoredSentence>> crossEncodeWithHttpInfo(CrossEncodeInput crossEncodeInput);



  /**
   * Sentence Producer
   * Turns a input document into sentences using &#x60;en_core_web_trf&#x60; transformer models
   * @param docInput  (required)
   * @return Sentences
   */
  @RequestLine("POST /sentence_producer")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  Sentences getSentences(DocInput docInput);

  /**
   * Sentence Producer
   * Similar to <code>getSentences</code> but it also returns the http response headers .
   * Turns a input document into sentences using &#x60;en_core_web_trf&#x60; transformer models
   * @param docInput  (required)
   * @return A ApiResponse that wraps the response boyd and the http headers.
   */
  @RequestLine("POST /sentence_producer")
  @Headers({
    "Content-Type: application/json",
    "Accept: application/json",
  })
  ApiResponse<Sentences> getSentencesWithHttpInfo(DocInput docInput);


}
