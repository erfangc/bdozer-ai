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
import co.bdozer.core.nlp.sdk.model.SingleLabelClassificationResult;
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
 * ZeroShotClassificationResponse
 */
@JsonPropertyOrder({
  ZeroShotClassificationResponse.JSON_PROPERTY_RESULT
})
@JsonTypeName("ZeroShotClassificationResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-03-16T21:02:09.915-04:00[America/New_York]")
public class ZeroShotClassificationResponse {
  public static final String JSON_PROPERTY_RESULT = "result";
  private List<SingleLabelClassificationResult> result = new ArrayList<SingleLabelClassificationResult>();

  public ZeroShotClassificationResponse() { 
  }

  public ZeroShotClassificationResponse result(List<SingleLabelClassificationResult> result) {
    
    this.result = result;
    return this;
  }

  public ZeroShotClassificationResponse addResultItem(SingleLabelClassificationResult resultItem) {
    this.result.add(resultItem);
    return this;
  }

   /**
   * Get result
   * @return result
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_RESULT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<SingleLabelClassificationResult> getResult() {
    return result;
  }


  @JsonProperty(JSON_PROPERTY_RESULT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setResult(List<SingleLabelClassificationResult> result) {
    this.result = result;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZeroShotClassificationResponse zeroShotClassificationResponse = (ZeroShotClassificationResponse) o;
    return Objects.equals(this.result, zeroShotClassificationResponse.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ZeroShotClassificationResponse {\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

