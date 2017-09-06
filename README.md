# FetchSpotifyData


Simple, lightweight library for obtaining data from the Spotify WebAPI using Spotify URI's.

This library multithreads requests where possible and handles rate-limiting where applicable (including retry logic).
The idea is that given a Spotify URI, requests are made and the Json objects received in response are parsed and  basic objects representing 
Artists, Albums, and Tracks are returned.
This hides any unnecessary complexity from the application.
