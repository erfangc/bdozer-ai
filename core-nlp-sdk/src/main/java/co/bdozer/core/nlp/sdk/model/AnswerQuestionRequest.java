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
 * AnswerQuestionRequest
 */
@JsonPropertyOrder({
  AnswerQuestionRequest.JSON_PROPERTY_QUESTIONS,
  AnswerQuestionRequest.JSON_PROPERTY_CONTEXT
})
@JsonTypeName("AnswerQuestionRequest")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-04-04T19:25:20.493-04:00[America/New_York]")
public class AnswerQuestionRequest {
  public static final String JSON_PROPERTY_QUESTIONS = "questions";
  private List<String> questions = new ArrayList<String>();

  public static final String JSON_PROPERTY_CONTEXT = "context";
  private String context;

  public AnswerQuestionRequest() { 
  }

  public AnswerQuestionRequest questions(List<String> questions) {
    
    this.questions = questions;
    return this;
  }

  public AnswerQuestionRequest addQuestionsItem(String questionsItem) {
    this.questions.add(questionsItem);
    return this;
  }

   /**
   * Get questions
   * @return questions
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_QUESTIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<String> getQuestions() {
    return questions;
  }


  @JsonProperty(JSON_PROPERTY_QUESTIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setQuestions(List<String> questions) {
    this.questions = questions;
  }


  public AnswerQuestionRequest context(String context) {
    
    this.context = context;
    return this;
  }

   /**
   * Get context
   * @return context
  **/
  @javax.annotation.Nonnull
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getContext() {
    return context;
  }


  @JsonProperty(JSON_PROPERTY_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setContext(String context) {
    this.context = context;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnswerQuestionRequest answerQuestionRequest = (AnswerQuestionRequest) o;
    return Objects.equals(this.questions, answerQuestionRequest.questions) &&
        Objects.equals(this.context, answerQuestionRequest.context);
  }

  @Override
  public int hashCode() {
    return Objects.hash(questions, context);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnswerQuestionRequest {\n");
    sb.append("    questions: ").append(toIndentedString(questions)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
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

