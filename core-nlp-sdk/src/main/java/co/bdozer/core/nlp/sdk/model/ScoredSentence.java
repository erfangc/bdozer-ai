/*
 * FastAPI
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package co.bdozer.core.nlp.sdk.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * ScoredSentence
 */
@JsonPropertyOrder({
  ScoredSentence.JSON_PROPERTY_SENTENCE,
  ScoredSentence.JSON_PROPERTY_SCORE
})
@JsonTypeName("ScoredSentence")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-03-13T20:35:04.937-04:00[America/New_York]")
public class ScoredSentence {
  public static final String JSON_PROPERTY_SENTENCE = "sentence";
  private String sentence;

  public static final String JSON_PROPERTY_SCORE = "score";
  private BigDecimal score;

  public ScoredSentence() { 
  }

  public ScoredSentence sentence(String sentence) {
    
    this.sentence = sentence;
    return this;
  }

   /**
   * Get sentence
   * @return sentence
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_SENTENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getSentence() {
    return sentence;
  }


  @JsonProperty(JSON_PROPERTY_SENTENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSentence(String sentence) {
    this.sentence = sentence;
  }


  public ScoredSentence score(BigDecimal score) {
    
    this.score = score;
    return this;
  }

   /**
   * Get score
   * @return score
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_SCORE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public BigDecimal getScore() {
    return score;
  }


  @JsonProperty(JSON_PROPERTY_SCORE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScore(BigDecimal score) {
    this.score = score;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScoredSentence scoredSentence = (ScoredSentence) o;
    return Objects.equals(this.sentence, scoredSentence.sentence) &&
        Objects.equals(this.score, scoredSentence.score);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sentence, score);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScoredSentence {\n");
    sb.append("    sentence: ").append(toIndentedString(sentence)).append("\n");
    sb.append("    score: ").append(toIndentedString(score)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

