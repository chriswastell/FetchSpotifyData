import java.util.concurrent.Callable;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.ConnectException;

import java.util.List;
import java.util.Map;

class SpotifyFetchTask implements Callable<SpotifyResult>{

  private SpotifyQuery query;
  private SpotifyExecutor exec;

  private static final int RETRY_LIMIT = 5;

  public SpotifyFetchTask(SpotifyQuery query, SpotifyExecutor exec){
    this.query = query;
    this.exec = exec; //The guy respsonsible for execution

  }

  /*
  * The IOException will cause an ExecutionException
  */
  public SpotifyResult call() throws InvalidResponseException{
    int attempt = 0;
    while(attempt < RETRY_LIMIT){
      //Try to make the query
      //Setup the result

      SpotifyResult result = SpotifyUtils.makeQuery(query);
      if(!result.isError()){
        return result;
      }

      //This is where we need to check for an error
      int responseCode = result.getResponseCode();
      if( responseCode != 429){
        //All other errors are just breaking for us now
        return null;
      }

      //Obtain retry after and we're good to roll
      Map<String, List<String>> headerFields = result.getHeaderFields();
      if(headerFields.containsKey("Retry-After")){
        List<String> headerLine = headerFields.get("Retry-After");
        String value = headerLine.get(0);
        //All threads that get here will wait - might take longer but avoids having to resubmit tasks
        exec.backOff(Integer.parseInt(value));
      }

      //Increment our attempt
      attempt++;
    }

    //Doesn't mean we have a result that isn't all null mind
    return null;
  }

}
