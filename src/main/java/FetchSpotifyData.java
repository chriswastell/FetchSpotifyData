package org.wastell.spotifydata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/*
* Class is the entry point for our library.
* Requires knowledge of CLIENT_ID and
*/
public class FetchSpotifyData{

  private BlockingQueue<Future<SpotifyResult>> results;
  private ExecutorCompletionService<SpotifyResult> spotifyQueryService;
  private SpotifyExecutor exec;
  private static FetchSpotifyData instance = null; //We only want one of these guys

  private static final int JOB_LIMIT = 15; //Limits number of requests in each query

  private FetchSpotifyData(){
    //Setup the executor and service
    exec = new SpotifyExecutor();;

    results = new LinkedBlockingQueue<Future<SpotifyResult>>();

    spotifyQueryService = new ExecutorCompletionService<SpotifyResult>(exec, results);

  }

  /*
  * Returns a list of workable data types.
  * Will always be  list since a single query may contain multiple things
  */
  public List<SpotifyEntity> addWork(SpotifyQuery query) throws InterruptedException, InvalidResponseException{
    Callable<SpotifyResult> runQuery = new SpotifyFetchTask(query, exec);


    spotifyQueryService.submit(runQuery);

    Future<SpotifyResult> answer = results.take();
    List<SpotifyEntity> entities = new LinkedList<SpotifyEntity>();

    try{
      SpotifyResult result = answer.get();
      SpotifyQuery.QueryType type = result.getType();

      switch(type){
        case ARTIST:
          entities = SpotifyArtist.parseResponse(result);
          break;
        case ALBUM:
          entities = SpotifyAlbum.parseResponse(result);
          break;
        case TRACK:
          entities = SpotifyTrack.parseResponse(result);
          break;
        default:
          return null;
      }



    } catch(ExecutionException ex){
      System.out.println("Unable to retrieve data/connect");
      ex.printStackTrace();
    } catch(NullPointerException ex){
      System.out.println("Result failed Unrecognised error");
      ex.printStackTrace();
      //Add the work back in
    }



    return entities;
  }

  /*
  * Build minumum number of queries for a given type
  */
  private List<SpotifyQuery> condenseQueries(SpotifyQuery.QueryType type, List<SpotifyQuery> input) throws SpotifyURIException, URISyntaxException{
    List<SpotifyQuery> queries = new LinkedList<SpotifyQuery>();
    StringBuilder baseUri = new StringBuilder("spotify:");

    switch(type){
      case ARTIST:
        baseUri.append("artist:");
        break;
      case ALBUM:
        baseUri.append("album:");
        break;
      case TRACK:
        baseUri.append("track:");
        break;
      default:
        throw new SpotifyURIException("Unrecognised Type");
    }

    StringBuilder uri = new StringBuilder(baseUri);
    //Condense the ID's
    int querySize = 0;
    for(SpotifyQuery query: input){
      if(query.getType() != type){
        throw new SpotifyURIException("Queries must be all the same type to condense");
      }

      String id = query.getURI().getID();
      uri.append(id + ",");

      querySize++;

      if(querySize == JOB_LIMIT){
          SpotifyQuery condensedQuery = new SpotifyQuery(new SpotifyURI(uri.substring(0,uri.length()-1)));

          queries.add(condensedQuery);
          uri = new StringBuilder(baseUri);
          querySize = 0; //Reset
      }

    }


    SpotifyQuery finalQuery = new SpotifyQuery(new SpotifyURI(uri.substring(0,uri.length()-1).trim()));


    String ids = finalQuery.getURI().getID();

    if(!ids.isEmpty()){
      queries.add(finalQuery);
    }



    return queries;

  }

  public List<SpotifyEntity> addWork(List<SpotifyQuery> query) throws InterruptedException, InvalidResponseException, SpotifyURIException, URISyntaxException{

      List<List<SpotifyQuery>> jobs = new LinkedList<List<SpotifyQuery>>();

      //Given a list of queries we need to separate them out into different jobs before submission
      Stream<SpotifyQuery> artistStream = query.stream().filter(q -> q.getType() == SpotifyQuery.QueryType.ARTIST);

      List<SpotifyQuery> artists = artistStream.collect(Collectors.toList());

      //condense jobs
      jobs.add(condenseQueries(SpotifyQuery.QueryType.ARTIST, artists));


      Stream<SpotifyQuery> albumStream = query.stream().filter(q-> q.getType() == SpotifyQuery.QueryType.ALBUM);
      List<SpotifyQuery> albums = albumStream.collect(Collectors.toList());

      //condense jobs
      jobs.add(condenseQueries(SpotifyQuery.QueryType.ALBUM, albums));

      Stream<SpotifyQuery> trackStream = query.stream().filter(q-> q.getType() == SpotifyQuery.QueryType.TRACK);
      List<SpotifyQuery> tracks = trackStream.collect(Collectors.toList());

      //Create a single job for the albums
      //condense jobs
      jobs.add(condenseQueries(SpotifyQuery.QueryType.TRACK, tracks));

      //TODO This is awful...
      List<SpotifyEntity> fullResult = new LinkedList<SpotifyEntity>();


      for(List<SpotifyQuery> job : jobs){
        for(SpotifyQuery inputQuery : job){
          fullResult.addAll(addWork(inputQuery));
        }
      }

      return fullResult;
  }


  public static FetchSpotifyData getInstance(String clientID, String clientSecret){
    //Update the utilities
    SpotifyUtils.setClientID(clientID);
    SpotifyUtils.setClientSecret(clientSecret);

    if( instance == null){
      instance = new FetchSpotifyData();
    }
    return instance;

  }

}
