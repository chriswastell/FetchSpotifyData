# FetchSpotifyData


Simple, lightweight library for obtaining data from the Spotify WebAPI using Spotify URI's.

This library multithreads requests where possible and handles rate-limiting where applicable (including retry logic).
The idea is that given a Spotify URI, requests are made and the JSON objects received in response are parsed and  basic objects representing 
Artists, Albums, and Tracks are returned.
This hides any unnecessary complexity from the application.


# Dependencies
JSON parsing is handled using gson.

# Disclaimer
Why is this here? Well I wanted to experiment with the Spotify WebAPI amongst other things. Spotify have created some very good SDK's however I wanted to explore some of the other challenges and to design a no-fat type solution. 

Additionally it is my hope this will be used in a subsequent Android project.
There are plenty of things I'd like to add to this project - we'll see! 
