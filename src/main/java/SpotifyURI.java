import java.net.URI;
import java.net.URISyntaxException;

/*
* Parses a Spotify URI to be a java URI
* Wrapper clas for java.net.URI
*
* spotify:{type}:id
* or,
* spotify:{user}:<user_id>:{playlist}:<playlist_id>
*/

public class SpotifyURI{

  private String input;
  private URI spotifyURI;

  public SpotifyURI(String input) throws URISyntaxException{
    this.input = input;
    spotifyURI = new URI(parseURI(input));
  }

  /*
  * Needed to modify spotify URI to a "proper" URI
  */
  private String parseURI(String uri) throws URISyntaxException{
    //Exlode the string and put it back together
    StringBuilder builder = new StringBuilder();
    String[] splitURI = uri.split(":");
    if(!splitURI[0].equals("spotify")){
      throw new URISyntaxException(uri, "Incorrect format");
    }
    builder.append("spotify://");
    if(!(splitURI[1].equals("artist") || splitURI[1].equals("album") || splitURI[1].equals("track"))){
      throw new URISyntaxException(uri, "Unrecognised URI type");
    }
    for(int i=1; i < splitURI.length; i++){
      builder.append(splitURI[i]);
      builder.append("/");
    }

    return builder.toString();
  }


  public SpotifyQuery.QueryType queryType(){
    String queryType = spotifyURI.getAuthority();
    switch(queryType){
      case "artist":
        return SpotifyQuery.QueryType.ARTIST;
      case "album":
        return SpotifyQuery.QueryType.ALBUM;
      case "track":
        return SpotifyQuery.QueryType.TRACK;
      default:
        return null;
    }
  }

  public String getID(){
    String path = spotifyURI.getPath();
    //TODO Validate the path

    path = path.replaceAll("/","");
    return path;
  }


}
