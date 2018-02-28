/**
 * Reseplaneraren
 * Provides access to Västtrafik journey planner
 *
 * OpenAPI spec version: 1.10.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.api;

import io.swagger.client.ApiInvoker;
import io.swagger.client.ApiException;
import io.swagger.client.Pair;

import io.swagger.client.model.*;

import java.util.*;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import io.swagger.client.model.LocationList;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class LocationApi {
  String basePath = "https://api.vasttrafik.se/bin/rest.exe/v2";
  ApiInvoker apiInvoker = ApiInvoker.getInstance();

  public void addHeader(String key, String value) {
    getInvoker().addDefaultHeader(key, value);
  }

  public ApiInvoker getInvoker() {
    return apiInvoker;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getBasePath() {
    return basePath;
  }

  /**
  * Returns a list of all stops available in the journey planner.
  * Returns a list of all stops available in the journey planner. Be aware that a call of this service is very time consuming and should be only requested when it is really needed.
   * @param format the required response format
   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
   * @return LocationList
  */
  public LocationList getAllStops (String format, String jsonpCallback) throws TimeoutException, ExecutionException, InterruptedException, ApiException {
    Object postBody = null;

    // create path and map variables
    String path = "/location.allstops";

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));
    String[] contentTypes = {
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
    }

    String[] authNames = new String[] {  };

    try {
      String localVarResponse = apiInvoker.invokeAPI (basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames);
      if (localVarResponse != null) {
         return (LocationList) ApiInvoker.deserialize(localVarResponse, "", LocationList.class);
      } else {
         return null;
      }
    } catch (ApiException ex) {
       throw ex;
    } catch (InterruptedException ex) {
       throw ex;
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof VolleyError) {
        VolleyError volleyError = (VolleyError)ex.getCause();
        if (volleyError.networkResponse != null) {
          throw new ApiException(volleyError.networkResponse.statusCode, volleyError.getMessage());
        }
      }
      throw ex;
    } catch (TimeoutException ex) {
      throw ex;
    }
  }

      /**
   * Returns a list of all stops available in the journey planner.
   * Returns a list of all stops available in the journey planner. Be aware that a call of this service is very time consuming and should be only requested when it is really needed.
   * @param format the required response format   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
  */
  public void getAllStops (String format, String jsonpCallback, final Response.Listener<LocationList> responseListener, final Response.ErrorListener errorListener) {
    Object postBody = null;


    // create path and map variables
    String path = "/location.allstops".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();

    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));


    String[] contentTypes = {
      
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      

      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
          }

    String[] authNames = new String[] {  };

    try {
      apiInvoker.invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String localVarResponse) {
            try {
              responseListener.onResponse((LocationList) ApiInvoker.deserialize(localVarResponse,  "", LocationList.class));
            } catch (ApiException exception) {
               errorListener.onErrorResponse(new VolleyError(exception));
            }
          }
      }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            errorListener.onErrorResponse(error);
          }
      });
    } catch (ApiException ex) {
      errorListener.onErrorResponse(new VolleyError(ex));
    }
  }
  /**
  * Returns a list of possible matches in the journey planner database
  * Performs a pattern matching of a user input to retrieve a list of possible matches in the journey planner database. Possible matches might be stops/stations, points of interest and addresses.
   * @param input a string with the user input
   * @param format the required response format
   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
   * @return LocationList
  */
  public LocationList getLocationByName (String input, String format, String jsonpCallback) throws TimeoutException, ExecutionException, InterruptedException, ApiException {
    Object postBody = null;

    // create path and map variables
    String path = "/location.name";

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();
    queryParams.addAll(ApiInvoker.parameterToPairs("", "input", input));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));
    String[] contentTypes = {
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
    }

    String[] authNames = new String[] {  };

    try {
      String localVarResponse = apiInvoker.invokeAPI (basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames);
      if (localVarResponse != null) {
         return (LocationList) ApiInvoker.deserialize(localVarResponse, "", LocationList.class);
      } else {
         return null;
      }
    } catch (ApiException ex) {
       throw ex;
    } catch (InterruptedException ex) {
       throw ex;
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof VolleyError) {
        VolleyError volleyError = (VolleyError)ex.getCause();
        if (volleyError.networkResponse != null) {
          throw new ApiException(volleyError.networkResponse.statusCode, volleyError.getMessage());
        }
      }
      throw ex;
    } catch (TimeoutException ex) {
      throw ex;
    }
  }

      /**
   * Returns a list of possible matches in the journey planner database
   * Performs a pattern matching of a user input to retrieve a list of possible matches in the journey planner database. Possible matches might be stops/stations, points of interest and addresses.
   * @param input a string with the user input   * @param format the required response format   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
  */
  public void getLocationByName (String input, String format, String jsonpCallback, final Response.Listener<LocationList> responseListener, final Response.ErrorListener errorListener) {
    Object postBody = null;


    // create path and map variables
    String path = "/location.name".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();

    queryParams.addAll(ApiInvoker.parameterToPairs("", "input", input));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));


    String[] contentTypes = {
      
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      

      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
          }

    String[] authNames = new String[] {  };

    try {
      apiInvoker.invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String localVarResponse) {
            try {
              responseListener.onResponse((LocationList) ApiInvoker.deserialize(localVarResponse,  "", LocationList.class));
            } catch (ApiException exception) {
               errorListener.onErrorResponse(new VolleyError(exception));
            }
          }
      }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            errorListener.onErrorResponse(error);
          }
      });
    } catch (ApiException ex) {
      errorListener.onErrorResponse(new VolleyError(ex));
    }
  }
  /**
  * Returns the address nearest a given coordinate.
  * 
   * @param originCoordLat latitude of coordinate in the WGS84 system
   * @param originCoordLong longitude of coordinate in the WGS84 system
   * @param format the required response format
   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
   * @return LocationList
  */
  public LocationList getNearbyAddress (Double originCoordLat, Double originCoordLong, String format, String jsonpCallback) throws TimeoutException, ExecutionException, InterruptedException, ApiException {
    Object postBody = null;
    // verify the required parameter 'originCoordLat' is set
    if (originCoordLat == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLat' when calling getNearbyAddress",
        new ApiException(400, "Missing the required parameter 'originCoordLat' when calling getNearbyAddress"));
    }
    // verify the required parameter 'originCoordLong' is set
    if (originCoordLong == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLong' when calling getNearbyAddress",
        new ApiException(400, "Missing the required parameter 'originCoordLong' when calling getNearbyAddress"));
    }

    // create path and map variables
    String path = "/location.nearbyaddress";

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();
    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLat", originCoordLat));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLong", originCoordLong));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));
    String[] contentTypes = {
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
    }

    String[] authNames = new String[] {  };

    try {
      String localVarResponse = apiInvoker.invokeAPI (basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames);
      if (localVarResponse != null) {
         return (LocationList) ApiInvoker.deserialize(localVarResponse, "", LocationList.class);
      } else {
         return null;
      }
    } catch (ApiException ex) {
       throw ex;
    } catch (InterruptedException ex) {
       throw ex;
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof VolleyError) {
        VolleyError volleyError = (VolleyError)ex.getCause();
        if (volleyError.networkResponse != null) {
          throw new ApiException(volleyError.networkResponse.statusCode, volleyError.getMessage());
        }
      }
      throw ex;
    } catch (TimeoutException ex) {
      throw ex;
    }
  }

      /**
   * Returns the address nearest a given coordinate.
   * 
   * @param originCoordLat latitude of coordinate in the WGS84 system   * @param originCoordLong longitude of coordinate in the WGS84 system   * @param format the required response format   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
  */
  public void getNearbyAddress (Double originCoordLat, Double originCoordLong, String format, String jsonpCallback, final Response.Listener<LocationList> responseListener, final Response.ErrorListener errorListener) {
    Object postBody = null;

    // verify the required parameter 'originCoordLat' is set
    if (originCoordLat == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLat' when calling getNearbyAddress",
        new ApiException(400, "Missing the required parameter 'originCoordLat' when calling getNearbyAddress"));
    }
    // verify the required parameter 'originCoordLong' is set
    if (originCoordLong == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLong' when calling getNearbyAddress",
        new ApiException(400, "Missing the required parameter 'originCoordLong' when calling getNearbyAddress"));
    }

    // create path and map variables
    String path = "/location.nearbyaddress".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();

    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLat", originCoordLat));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLong", originCoordLong));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));


    String[] contentTypes = {
      
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      

      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
          }

    String[] authNames = new String[] {  };

    try {
      apiInvoker.invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String localVarResponse) {
            try {
              responseListener.onResponse((LocationList) ApiInvoker.deserialize(localVarResponse,  "", LocationList.class));
            } catch (ApiException exception) {
               errorListener.onErrorResponse(new VolleyError(exception));
            }
          }
      }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            errorListener.onErrorResponse(error);
          }
      });
    } catch (ApiException ex) {
      errorListener.onErrorResponse(new VolleyError(ex));
    }
  }
  /**
  * Returns a list of stops around a given center coordinate.
  * Returns a list of stops around a given center coordinate. The returned results are ordered by their distance to the center coordinate.
   * @param originCoordLat latitude of center coordinate in the WGS84 system
   * @param originCoordLong longitude of center coordinate in the WGS84 system
   * @param maxNo maximum number of returned stops
   * @param maxDist maximum distance from the center coordinate
   * @param format the required response format
   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
   * @return LocationList
  */
  public LocationList getNearbyStops (Double originCoordLat, Double originCoordLong, Integer maxNo, Integer maxDist, String format, String jsonpCallback) throws TimeoutException, ExecutionException, InterruptedException, ApiException {
    Object postBody = null;
    // verify the required parameter 'originCoordLat' is set
    if (originCoordLat == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLat' when calling getNearbyStops",
        new ApiException(400, "Missing the required parameter 'originCoordLat' when calling getNearbyStops"));
    }
    // verify the required parameter 'originCoordLong' is set
    if (originCoordLong == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLong' when calling getNearbyStops",
        new ApiException(400, "Missing the required parameter 'originCoordLong' when calling getNearbyStops"));
    }

    // create path and map variables
    String path = "/location.nearbystops";

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();
    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLat", originCoordLat));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLong", originCoordLong));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "maxNo", maxNo));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "maxDist", maxDist));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));
    String[] contentTypes = {
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
    }

    String[] authNames = new String[] {  };

    try {
      String localVarResponse = apiInvoker.invokeAPI (basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames);
      if (localVarResponse != null) {
         return (LocationList) ApiInvoker.deserialize(localVarResponse, "", LocationList.class);
      } else {
         return null;
      }
    } catch (ApiException ex) {
       throw ex;
    } catch (InterruptedException ex) {
       throw ex;
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof VolleyError) {
        VolleyError volleyError = (VolleyError)ex.getCause();
        if (volleyError.networkResponse != null) {
          throw new ApiException(volleyError.networkResponse.statusCode, volleyError.getMessage());
        }
      }
      throw ex;
    } catch (TimeoutException ex) {
      throw ex;
    }
  }

      /**
   * Returns a list of stops around a given center coordinate.
   * Returns a list of stops around a given center coordinate. The returned results are ordered by their distance to the center coordinate.
   * @param originCoordLat latitude of center coordinate in the WGS84 system   * @param originCoordLong longitude of center coordinate in the WGS84 system   * @param maxNo maximum number of returned stops   * @param maxDist maximum distance from the center coordinate   * @param format the required response format   * @param jsonpCallback If JSONP response format is needed, you can append an additional parameter to specify the name of a callback function, and the JSON object will be wrapped by a function call with this name.
  */
  public void getNearbyStops (Double originCoordLat, Double originCoordLong, Integer maxNo, Integer maxDist, String format, String jsonpCallback, final Response.Listener<LocationList> responseListener, final Response.ErrorListener errorListener) {
    Object postBody = null;

    // verify the required parameter 'originCoordLat' is set
    if (originCoordLat == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLat' when calling getNearbyStops",
        new ApiException(400, "Missing the required parameter 'originCoordLat' when calling getNearbyStops"));
    }
    // verify the required parameter 'originCoordLong' is set
    if (originCoordLong == null) {
      VolleyError error = new VolleyError("Missing the required parameter 'originCoordLong' when calling getNearbyStops",
        new ApiException(400, "Missing the required parameter 'originCoordLong' when calling getNearbyStops"));
    }

    // create path and map variables
    String path = "/location.nearbystops".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    // header params
    Map<String, String> headerParams = new HashMap<String, String>();
    // form params
    Map<String, String> formParams = new HashMap<String, String>();

    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLat", originCoordLat));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "originCoordLong", originCoordLong));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "maxNo", maxNo));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "maxDist", maxDist));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "format", format));
    queryParams.addAll(ApiInvoker.parameterToPairs("", "jsonpCallback", jsonpCallback));


    String[] contentTypes = {
      
    };
    String contentType = contentTypes.length > 0 ? contentTypes[0] : "application/json";

    if (contentType.startsWith("multipart/form-data")) {
      // file uploading
      MultipartEntityBuilder localVarBuilder = MultipartEntityBuilder.create();
      

      HttpEntity httpEntity = localVarBuilder.build();
      postBody = httpEntity;
    } else {
      // normal form params
          }

    String[] authNames = new String[] {  };

    try {
      apiInvoker.invokeAPI(basePath, path, "GET", queryParams, postBody, headerParams, formParams, contentType, authNames,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String localVarResponse) {
            try {
              responseListener.onResponse((LocationList) ApiInvoker.deserialize(localVarResponse,  "", LocationList.class));
            } catch (ApiException exception) {
               errorListener.onErrorResponse(new VolleyError(exception));
            }
          }
      }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            errorListener.onErrorResponse(error);
          }
      });
    } catch (ApiException ex) {
      errorListener.onErrorResponse(new VolleyError(ex));
    }
  }
}
