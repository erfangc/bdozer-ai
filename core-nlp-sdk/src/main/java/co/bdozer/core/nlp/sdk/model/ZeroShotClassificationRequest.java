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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * ZeroShotClassificationRequest
 */
@JsonPropertyOrder({
  ZeroShotClassificationRequest.JSON_PROPERTY_CANDIDATE_LABELS,
  ZeroShotClassificationRequest.JSON_PROPERTY_SENTENCE
})
@JsonTypeName("ZeroShotClassificationRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-04-03T13:56:25.544-04:00[America/New_York]")
public class ZeroShotClassificationRequest {
  public static final String JSON_PROPERTY_CANDIDATE_LABELS = "candidate_labels";
  private List<String> candidateLabels = new ArrayList<String>();

  public static final String JSON_PROPERTY_SENTENCE = "sentence";
  private String sentence;

  public ZeroShotClassificationRequest() { 
  }

  public ZeroShotClassificationRequest candidateLabels(List<String> candidateLabels) {
    
    this.candidateLabels = candidateLabels;
    return this;
  }

  public ZeroShotClassificationRequest addCandidateLabelsItem(String candidateLabelsItem) {
    this.candidateLabels.add(candidateLabelsItem);
    return this;
  }

   /**
   * Get candidateLabels
   * @return candidateLabels
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_CANDIDATE_LABELS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<String> getCandidateLabels() {
    return candidateLabels;
  }


  @JsonProperty(JSON_PROPERTY_CANDIDATE_LABELS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCandidateLabels(List<String> candidateLabels) {
    this.candidateLabels = candidateLabels;
  }


  public ZeroShotClassificationRequest sentence(String sentence) {
    
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZeroShotClassificationRequest zeroShotClassificationRequest = (ZeroShotClassificationRequest) o;
    return Objects.equals(this.candidateLabels, zeroShotClassificationRequest.candidateLabels) &&
        Objects.equals(this.sentence, zeroShotClassificationRequest.sentence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(candidateLabels, sentence);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ZeroShotClassificationRequest {\n");
    sb.append("    candidateLabels: ").append(toIndentedString(candidateLabels)).append("\n");
    sb.append("    sentence: ").append(toIndentedString(sentence)).append("\n");
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

