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
 * CrossEncodeInput
 */
@JsonPropertyOrder({
  CrossEncodeInput.JSON_PROPERTY_REFERENCE,
  CrossEncodeInput.JSON_PROPERTY_COMPARISONS
})
@JsonTypeName("CrossEncodeInput")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-04-04T19:25:20.493-04:00[America/New_York]")
public class CrossEncodeInput {
  public static final String JSON_PROPERTY_REFERENCE = "reference";
  private String reference;

  public static final String JSON_PROPERTY_COMPARISONS = "comparisons";
  private List<String> comparisons = new ArrayList<String>();

  public CrossEncodeInput() { 
  }

  public CrossEncodeInput reference(String reference) {
    
    this.reference = reference;
    return this;
  }

   /**
   * Get reference
   * @return reference
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_REFERENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getReference() {
    return reference;
  }


  @JsonProperty(JSON_PROPERTY_REFERENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReference(String reference) {
    this.reference = reference;
  }


  public CrossEncodeInput comparisons(List<String> comparisons) {
    
    this.comparisons = comparisons;
    return this;
  }

  public CrossEncodeInput addComparisonsItem(String comparisonsItem) {
    this.comparisons.add(comparisonsItem);
    return this;
  }

   /**
   * Get comparisons
   * @return comparisons
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_COMPARISONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<String> getComparisons() {
    return comparisons;
  }


  @JsonProperty(JSON_PROPERTY_COMPARISONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComparisons(List<String> comparisons) {
    this.comparisons = comparisons;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CrossEncodeInput crossEncodeInput = (CrossEncodeInput) o;
    return Objects.equals(this.reference, crossEncodeInput.reference) &&
        Objects.equals(this.comparisons, crossEncodeInput.comparisons);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reference, comparisons);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CrossEncodeInput {\n");
    sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
    sb.append("    comparisons: ").append(toIndentedString(comparisons)).append("\n");
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

