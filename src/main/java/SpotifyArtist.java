package org.wastell.spotifydata;


import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

import java.util.List;
import java.util.LinkedList;

/*
* The class represnts an artist.
* Needs to be able to construct from suitable JSON response
*/

public class SpotifyArtist implements SpotifyEntity{

  private JsonElement artistObj;

  private SpotifyArtist(JsonElement artistObj){
    this.artistObj = artistObj;
    System.out.println(this);
  }


  public String getName(){
    JsonObject obj = artistObj.getAsJsonObject();
    return obj.getAsJsonPrimitive("name").getAsString();
  }

  //TODO Doesn't exist for simplified object
  public List<String> getGenres(){
    JsonObject obj = artistObj.getAsJsonObject();
    JsonArray genreArray = obj.getAsJsonArray("genres");
    List<String> genres = new LinkedList<String>();
    for(JsonElement genre : genreArray){
      String genreString = genre.getAsString();
      genres.add(genreString);
    }
    return genres;
  }

  @Override
  public String toString(){
    return "Artist: " + getName();
  }

  public static SpotifyArtist parseResult(JsonElement response) throws InvalidResponseException{

    if(response == null || response instanceof JsonNull){
      //TODO Indicates a bad result
      return null;
    }

    JsonObject obj = response.getAsJsonObject();
    if(!obj.has("type")){
      throw new InvalidResponseException("Invalid Spotify response.");
    }
    if(obj.has("type")){
      String type = obj.getAsJsonPrimitive("type").getAsString();
      if(!type.equalsIgnoreCase("artist")){
        throw new InvalidResponseException("Not a Spotify Artist response.");
      }

    }

    return new SpotifyArtist(response);
  }

  public static List<SpotifyEntity> parseResponse(SpotifyResult result) throws InvalidResponseException{

    List<SpotifyEntity> artists = new LinkedList<SpotifyEntity>();
    JsonArray entries = new JsonArray();
    if(result.isMultiple()){
      JsonObject obj = result.getResponse().getAsJsonObject();
      if(!obj.has("artists")){
        throw new InvalidResponseException("Invalid response. Not an artist");
      }
       entries = obj.getAsJsonArray("artists");
    } else {
      //If we only have one just add it into the array for now
      entries.add(result.getResponse());
    }

    for(JsonElement entry : entries){
      //Now we can parse the entry as before and create our list
      SpotifyArtist artist = parseResult(entry);
      artists.add(artist);
    }

    return artists;

  }

}
