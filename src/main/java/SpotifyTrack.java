package org.wastell.spotifydata;

import java.util.List;
import java.util.LinkedList;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

public class SpotifyTrack implements SpotifyEntity{

  private JsonElement trackObj;

  private SpotifyTrack(JsonElement response){
    this.trackObj = response;
    System.out.println(this);
  }


  public List<SpotifyArtist> getArtists(){
    JsonObject obj = trackObj.getAsJsonObject();
    JsonArray artistArr = obj.getAsJsonArray("artists");
    List<SpotifyArtist> artists = new LinkedList<SpotifyArtist>();
    for(JsonElement entry: artistArr){
      try{
        artists.add(SpotifyArtist.parseResult(entry));
      } catch (InvalidResponseException ex){
        ex.printStackTrace();
      }
    }
    return artists;
  }


  public String getName(){
    JsonObject obj = trackObj.getAsJsonObject();

    return obj.getAsJsonPrimitive("name").getAsString();
  }

  public String toString(){
      return "Track: " + getName() + " Artist: " + getArtists();
  }

  public static SpotifyTrack parseResult(JsonElement response) throws InvalidResponseException{

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
      if(!type.equalsIgnoreCase("track")){
        throw new InvalidResponseException("Not a Spotify Track response.");
      }

    }

    return new SpotifyTrack(response);

  }

  public static List<SpotifyEntity> parseResponse(SpotifyResult result) throws InvalidResponseException{
    List<SpotifyEntity> tracks = new LinkedList<SpotifyEntity>();
    JsonArray entries = new JsonArray();
    if(result.isMultiple()){
      JsonObject obj = result.getResponse().getAsJsonObject();
      if(!obj.has("tracks")){
        System.out.println("Invalid response");
        return null;
      }
       entries = obj.getAsJsonArray("tracks");
    } else {
      //If we only have one just add it into the array for now
      entries.add(result.getResponse());
    }

    for(JsonElement entry : entries){
      //Now we can parse the entry as before and create our list
      SpotifyTrack track = parseResult(entry);
      tracks.add(track);
    }

    return tracks;
  }

}
