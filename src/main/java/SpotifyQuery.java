import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

public class SpotifyQuery{

  enum QueryType{
    ARTIST, ALBUM, TRACK
  }

  private SpotifyURI uri;
  private static final String BASE_URL = "https://api.spotify.com/v1/";


  public SpotifyQuery(SpotifyURI uri) throws SpotifyURIException{
    if(uri == null){
      throw new SpotifyURIException("URI Can not be null");
    }
    this.uri = uri;
  }

  /*
  * Parsing the URI into the query URL
  * https://api.spotify.com/v1/albums/{id}
  * https://api.spotify.com/v1/artists/{id}
  * https://api.spotify.com/v1/tracks/{id}
  * https://api.spotify.com/v1/users/{user_id}/playlists/{playlist_id}
  */
  public URL parseQueryURL() throws MalformedURLException {
    StringBuilder url = new StringBuilder(BASE_URL);

    //Obtain the Query type
    QueryType queryType = uri.queryType();
    switch(queryType){
      case ARTIST:
        url.append("artists");
        break;
      case ALBUM:
        url.append("albums");
        break;
      case TRACK:
        url.append("tracks");
        break;
      default:
        throw new MalformedURLException("Unrecognised query type");
    }

    String idTag = uri.getID();
    if(idTag.contains(",")){
      idTag = "?ids=" + idTag;
    } else {
      idTag = "/" + idTag;
    }
    url.append(idTag);
    URL queryURL = new URL(url.toString());
    return queryURL;

  }

  public SpotifyURI getURI(){
    return uri;
  }

  public QueryType getType(){
    return uri.queryType();
  }

  public String toString(){
    return uri.getID();
  }


}
