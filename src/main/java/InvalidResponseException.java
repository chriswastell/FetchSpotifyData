package org.wastell.spotifydata;

public class InvalidResponseException extends Exception{


  public InvalidResponseException(String msg){
    super(msg);
  }

  public InvalidResponseException(String msg, Throwable cause){
    super(msg, cause);
  }

}
