package org.wastell.spotifydata;


import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

import java.util.List;
import java.util.LinkedList;

public class SpotifyAlbum implements SpotifyEntity{

  private JsonElement albumObj;

  private SpotifyAlbum(JsonElement response){
    this.albumObj = response;
    System.out.println(this);

  }

  public String getName(){
    JsonObject obj = albumObj.getAsJsonObject();
    return obj.getAsJsonPrimitive("name").getAsString();
  }

  // public List<String> getTracks(){
  //   JsonObject obj = albumObj.getAsJsonObject();
  //   JsonArray trackArr = obj.getAsJsonObject("tracks").getAsJsonArray("items");
  //   List<String> tracks = new LinkedList<String>();
  //   for(JsonElement ell : trackArr){
  //     tracks.add( ell.getAsString() );
  //   }
  //
  //   return tracks;
  // }

  public List<SpotifyArtist> getArtists(){
    JsonObject obj = albumObj.getAsJsonObject();
    JsonArray artistArr = obj.getAsJsonArray("artists");
    List<SpotifyArtist> artists = new LinkedList<SpotifyArtist>();
    for(JsonElement ell : artistArr){
      try{
        artists.add( SpotifyArtist.parseResult(ell));
      } catch (InvalidResponseException ex){
        ex.printStackTrace();
      }
    }

    return artists;
  }

  public List<String> getGenres(){
    JsonObject obj = albumObj.getAsJsonObject();
    JsonArray genArr = obj.getAsJsonArray("genres");
    List<String> genres = new LinkedList<String>();
    for(JsonElement ell : genArr){
      genres.add( ell.getAsString() );
    }

    return genres;
  }

  public String toString(){
    return "Album: " + getName() + " Genres: " + getGenres() + "Artists: " + getArtists() ;
  }

  private static SpotifyAlbum parseResult(JsonElement response) throws InvalidResponseException{
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
      if(!type.equalsIgnoreCase("album")){
        throw new InvalidResponseException("Not a Spotify Album response.");
      }

    }
    return new SpotifyAlbum(response);

    // String albumName = obj.getAsJsonPrimitive("name").getAsString();

    // int tracks = obj.getAsJsonObject("tracks").getAsJsonArray("items").size();


  }

  public static List<SpotifyEntity> parseResponse(SpotifyResult result) throws InvalidResponseException{
    List<SpotifyEntity> albums = new LinkedList<SpotifyEntity>();
    JsonArray entries = new JsonArray();
    if(result.isMultiple()){
      JsonObject obj = result.getResponse().getAsJsonObject();
      if(!obj.has("albums")){
        System.out.println("Invalid response");
        return null;
      }
       entries = obj.getAsJsonArray("albums");
    } else {
      //If we only have one just add it into the array for now
      entries.add(result.getResponse());
    }

    for(JsonElement entry : entries){
      //Now we can parse the entry as before and create our list
      SpotifyAlbum album = parseResult(entry);
      albums.add(album);
    }

    return albums;
  }
}
