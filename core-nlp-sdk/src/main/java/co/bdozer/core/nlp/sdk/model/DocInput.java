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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * DocInput
 */
@JsonPropertyOrder({
  DocInput.JSON_PROPERTY_DOC
})
@JsonTypeName("DocInput")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-03-13T20:35:04.937-04:00[America/New_York]")
public class DocInput {
  public static final String JSON_PROPERTY_DOC = "doc";
  private String doc;

  public DocInput() { 
  }

  public DocInput doc(String doc) {
    
    this.doc = doc;
    return this;
  }

   /**
   * Get doc
   * @return doc
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_DOC)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getDoc() {
    return doc;
  }


  @JsonProperty(JSON_PROPERTY_DOC)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDoc(String doc) {
    this.doc = doc;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocInput docInput = (DocInput) o;
    return Objects.equals(this.doc, docInput.doc);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doc);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocInput {\n");
    sb.append("    doc: ").append(toIndentedString(doc)).append("\n");
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

