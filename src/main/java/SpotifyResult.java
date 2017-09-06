package org.wastell.spotifydata;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

/*
* A data type to wrap the results of a query.
* TODO No guarantee that the response and response code match up
*/
class SpotifyResult{

  private SpotifyQuery query;
  private Map<String, List<String>> headerFields;
  private JsonElement jsonResponse;
  private int responseCode;
  // private List<SpotifyEntity> results;

  public SpotifyResult(SpotifyQuery query){
    this.query = query;
    jsonResponse = null;
    responseCode = -1;
  }

  public boolean completed(){
    return jsonResponse != null;
  }

  public int getResponseCode(){
    return responseCode;
  }

  public JsonElement getResponse(){
    return jsonResponse;
  }

  public Map<String, List<String>> getHeaderFields(){
    return headerFields;
  }

  public void setResponseCode(int code){
    responseCode = code;
  }

  public void setResponse(JsonElement response){
    jsonResponse = response;
  }

  public void setHeaderFields(Map<String, List<String>> headerFields){
    this.headerFields = headerFields;
  }

  public SpotifyQuery.QueryType getType(){
    return query.getType();
  }

  /*
  * Returns true if this is the result of a multiple query
  */
  //TODO Needs to actually work!
  public boolean isMultiple(){
    if(jsonResponse.isJsonObject()){
      JsonObject obj = jsonResponse.getAsJsonObject();

      switch(query.getType()){
        case ARTIST:
          return obj.has("artists");
        case ALBUM:
          return obj.has("albums");
        case TRACK:
          return obj.has("tracks");
      }

    }
    return false;
  }

  public boolean isError(){
    return responseCode >= 400;
  }

}
