package co.bdozer.core.nlp.sdk;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.threeten.bp.*;
import feign.okhttp.OkHttpClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openapitools.jackson.nullable.JsonNullableModule;
import com.fasterxml.jackson.datatype.threetenbp.ThreeTenModule;

import feign.Feign;
import feign.RequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import co.bdozer.core.nlp.sdk.auth.HttpBasicAuth;
import co.bdozer.core.nlp.sdk.auth.HttpBearerAuth;
import co.bdozer.core.nlp.sdk.auth.ApiKeyAuth;
import co.bdozer.core.nlp.sdk.ApiResponseDecoder;


@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-03-16T21:02:09.915-04:00[America/New_York]")
public class ApiClient {
  private static final Logger log = Logger.getLogger(ApiClient.class.getName());

  public interface Api {}

  protected ObjectMapper objectMapper;
  private String basePath = "http://localhost";
  private Map<String, RequestInterceptor> apiAuthorizations;
  private Feign.Builder feignBuilder;

  public ApiClient() {
    objectMapper = createObjectMapper();
    apiAuthorizations = new LinkedHashMap<String, RequestInterceptor>();
    feignBuilder = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
                .decoder(new ApiResponseDecoder(objectMapper))
                .logger(new Slf4jLogger());
  }

  public ApiClient(String[] authNames) {
    this();
    for(String authName : authNames) {
      log.log(Level.FINE, "Creating authentication {0}", authName);
      throw new RuntimeException("auth name \"" + authName + "\" not found in available auth names");
    }
  }

  /**
   * Basic constructor for single auth name
   * @param authName
   */
  public ApiClient(String authName) {
    this(new String[]{authName});
  }

  /**
   * Helper constructor for single api key
   * @param authName
   * @param apiKey
   */
  public ApiClient(String authName, String apiKey) {
    this(authName);
    this.setApiKey(apiKey);
  }

  public String getBasePath() {
    return basePath;
  }

  public ApiClient setBasePath(String basePath) {
    this.basePath = basePath;
    return this;
  }

  public Map<String, RequestInterceptor> getApiAuthorizations() {
    return apiAuthorizations;
  }

  public void setApiAuthorizations(Map<String, RequestInterceptor> apiAuthorizations) {
    this.apiAuthorizations = apiAuthorizations;
  }

  public Feign.Builder getFeignBuilder() {
    return feignBuilder;
  }

  public ApiClient setFeignBuilder(Feign.Builder feignBuilder) {
    this.feignBuilder = feignBuilder;
    return this;
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.setDateFormat(new RFC3339DateFormat());
    ThreeTenModule module = new ThreeTenModule();
    module.addDeserializer(Instant.class, CustomInstantDeserializer.INSTANT);
    module.addDeserializer(OffsetDateTime.class, CustomInstantDeserializer.OFFSET_DATE_TIME);
    module.addDeserializer(ZonedDateTime.class, CustomInstantDeserializer.ZONED_DATE_TIME);
    objectMapper.registerModule(module);
    JsonNullableModule jnm = new JsonNullableModule();
    objectMapper.registerModule(jnm);
    return objectMapper;
  }

  public ObjectMapper getObjectMapper(){
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
  }

  /**
   * Creates a feign client for given API interface.
   *
   * Usage:
   *    ApiClient apiClient = new ApiClient();
   *    apiClient.setBasePath("http://localhost:8080");
   *    XYZApi api = apiClient.buildClient(XYZApi.class);
   *    XYZResponse response = api.someMethod(...);
   * @param <T> Type
   * @param clientClass Client class
   * @return The Client
   */
  public <T extends Api> T buildClient(Class<T> clientClass) {
    return feignBuilder.target(clientClass, basePath);
  }

  /**
   * Select the Accept header's value from the given accepts array:
   *   if JSON exists in the given array, use it;
   *   otherwise use all of them (joining into a string)
   *
   * @param accepts The accepts array to select from
   * @return The Accept header to use. If the given array is empty,
   *   null will be returned (not to set the Accept header explicitly).
   */
  public String selectHeaderAccept(String[] accepts) {
    if (accepts.length == 0) return null;
    if (StringUtil.containsIgnoreCase(accepts, "application/json")) return "application/json";
    return StringUtil.join(accepts, ",");
  }

  /**
   * Select the Content-Type header's value from the given array:
   *   if JSON exists in the given array, use it;
   *   otherwise use the first one of the array.
   *
   * @param contentTypes The Content-Type array to select from
   * @return The Content-Type header to use. If the given array is empty,
   *   JSON will be used.
   */
  public String selectHeaderContentType(String[] contentTypes) {
    if (contentTypes.length == 0) return "application/json";
    if (StringUtil.containsIgnoreCase(contentTypes, "application/json")) return "application/json";
    return contentTypes[0];
  }

  /**
   * Helper method to configure the bearer token.
   * @param bearerToken the bearer token.
   */
  public void setBearerToken(String bearerToken) {
    HttpBearerAuth apiAuthorization =  getAuthorization(HttpBearerAuth.class);
    apiAuthorization.setBearerToken(bearerToken);
  }

  /**
   * Helper method to configure the first api key found
   * @param apiKey API key
   */
  public void setApiKey(String apiKey) {
    ApiKeyAuth apiAuthorization =  getAuthorization(ApiKeyAuth.class);
    apiAuthorization.setApiKey(apiKey);
  }

  /**
   * Helper method to configure the username/password for basic auth
   * @param username Username
   * @param password Password
   */
  public void setCredentials(String username, String password) {
    HttpBasicAuth apiAuthorization = getAuthorization(HttpBasicAuth.class);
    apiAuthorization.setCredentials(username, password);
  }

  /**
   * Gets request interceptor based on authentication name
   * @param authName Authentication name
   * @return Request Interceptor
   */
  public RequestInterceptor getAuthorization(String authName) {
    return apiAuthorizations.get(authName);
  }

  /**
   * Adds an authorization to be used by the client
   * @param authName Authentication name
   * @param authorization Request interceptor
   */
  public void addAuthorization(String authName, RequestInterceptor authorization) {
    if (apiAuthorizations.containsKey(authName)) {
      throw new RuntimeException("auth name \"" + authName + "\" already in api authorizations");
    }
    apiAuthorizations.put(authName, authorization);
    feignBuilder.requestInterceptor(authorization);
  }

  private <T extends RequestInterceptor> T getAuthorization(Class<T> type) {
    return (T) apiAuthorizations.values()
                                .stream()
                                .filter(requestInterceptor -> type.isAssignableFrom(requestInterceptor.getClass()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No Oauth authentication or OAuth configured!"));
    }
}
