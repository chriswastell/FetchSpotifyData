import java.net.URL;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64; //Will only work in Java 8+..
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.*; //TODO Figure this out later..

/*
* A set of utilities to help make spotify queries
*/

class SpotifyUtils{

  private static final String AUTH_URL = "https://accounts.spotify.com/api/token";

  //TODO Where is best for these to be fed into the library
  private static String CLIENT_ID = null;

  //TODO This can't be here
  private static String CLIENT_SECRET = null;

  //Cache the auth string
  private static String AUTH_STRING = null;

  private static Lock authLock = new ReentrantLock();


  public static void setClientID(String clientID){
    CLIENT_ID = clientID;
  }

  public static void setClientSecret(String clientSecret){
    CLIENT_SECRET = clientSecret;
  }

  /*
  * IOException - Represents an error in communication either the connection, or internal processing. Either way not good and we need to try again.
  */
  private static String getAuthString() throws InvalidResponseException{
    //Create a URL
    URL authURL = null;
    try{
      authURL = new URL(AUTH_URL);
    } catch(MalformedURLException ex){
      //TODO Should never get here without coder error
      ex.printStackTrace();
      return null;
    }

    //Open connection and set headers
    HttpsURLConnection connection = null;
    try{
      connection = (HttpsURLConnection) authURL.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Basic "+ authString(CLIENT_ID,CLIENT_SECRET));

    } catch(ProtocolException ex){
      //TODO IF we're here spotify have changed their protocols
      ex.printStackTrace();
      return null;
    } catch (IOException ex){
      //TODO Unable to open connection
    }



    //Fixing up the data/body to make the request
    connection.setDoOutput(true);
    try{
      DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
      try{
        writer.write("grant_type=client_credentials".getBytes(StandardCharsets.UTF_8));
        writer.flush();
      } finally {
        writer.close();
      }

    } catch ( IOException ex){
      //TODO Unable to write
      ex.printStackTrace();
    }


    //Attempt to parse the response
    JsonElement parseTree = parseResponse(connection);
    JsonObject obj = parseTree.getAsJsonObject();

    if(!obj.has("access_token")){
      //Then it wasn't what we expected
      throw new InvalidResponseException("Unable to authorize");
    }

    JsonElement authPair = obj.get("access_token");
    String auth = authPair.getAsJsonPrimitive().getAsString();

    return auth; //All good if we get this far
  }


  /*
  * Helper method to encode the CLIENT_ID for request
  */
  private static String authString(String clientID, String clientSecret){

    if(clientID == null || clientSecret == null){
      throw new IllegalArgumentException("Invalid authorization parameters");
    }

    Base64.Encoder encoder = Base64.getEncoder();
    String toEncode = clientID + ":" + clientSecret;
    byte[] toEncodeBytes = toEncode.getBytes(StandardCharsets.UTF_8);
    String encoded = encoder.encodeToString(toEncodeBytes);
    return encoded;
  }



  public static SpotifyResult makeQuery(SpotifyQuery query) throws InvalidResponseException{
    SpotifyResult result = new SpotifyResult(query);

    //Mutex to make thread safe operation
    authLock.lock();
    if(AUTH_STRING == null){
      AUTH_STRING = getAuthString();
    }
    authLock.unlock();

    //We think we have an auth String - make the query
    URL url = null;
    try{
      url = query.parseQueryURL();
    } catch (MalformedURLException ex){
      //Should never get here without programmer errors
      throw new InvalidResponseException("Invalid Spotify URL - Has the protocol changed?", ex);
    }

    HttpsURLConnection connection = null;

    try{
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", "Bearer "+ AUTH_STRING);

      //Set the response code
      //Check that our authorization hasn't timed out etc...
      if(connection.getResponseCode() == 401){
        //TODO Check the responseCode
        AUTH_STRING = null;

        // Try again
        makeQuery(query);
      }

      result.setResponseCode(connection.getResponseCode());
      result.setHeaderFields(connection.getHeaderFields());
    } catch(ProtocolException ex){
      throw new InvalidResponseException("Spotify protocol has changed", ex);
    } catch(IOException ex){
      throw new InvalidResponseException("Connection error", ex);
    }


    //Attempt to build a JsonElement
    JsonElement parseTree = parseResponse(connection);

    result.setResponse(parseTree);



    return result;

  }

  private static JsonElement parseResponse(HttpsURLConnection connection) throws InvalidResponseException{

    StringBuilder response = new StringBuilder();

    try{
      int statusCode = connection.getResponseCode();
      InputStream input;
      if(statusCode >= 400){
          //This is an error, use the error stream
          input = connection.getErrorStream();
      } else {
          //We're hoping for the best here
          input = connection.getInputStream();
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      try{
        String line;
        while((line = reader.readLine()) != null){
          response.append(line);
        }

      } finally {
        reader.close();
      }
    } catch (IOException ex){
      throw new InvalidResponseException("Unable to connect", ex);
    }

    //Attempt to build a JsonElement
    JsonElement parseTree = null;
    try{
     parseTree = new JsonParser().parse(response.toString());
    } catch(JsonParseException ex){
      throw new InvalidResponseException("Unable to parse response", ex);
    }
    return parseTree;

  }

}
