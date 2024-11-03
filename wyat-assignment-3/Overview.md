---
aliases:
class: Note
from:
rating:
tags:
---
# Notes

The application uses the GPS sensor to get the user's current location, which is used to create location based exercises. I chose gps because 1, it is used with the google map SDK, 2 it is most useful outdoor, which most exercises are expected to be outdoors. 

GPS has limitations like poor performance indoors and higher power consumption. Google does give guides on how to reduce the power consumption, though. 

If the user does not manually select a location on the map, the app defaults to their current GPS position when the "+" button is pressed. In cases where GPS is unavailable or permissions are denied, the app handles errors by allowing users to manually select a location on the map.

The GPS sensor integrates directly with the app’s GUI, providing real-time updates on the map and allowing users to interact with their location. The app uses error-handling by giving permission checks, manual location selection, and prompts to the user if GPS data is unavailable. 

Note: Google did a lot of the heavy lifting with the markers:
https://developers.google.com/maps/documentation/android-sdk/marker


