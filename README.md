## Movies

**Note**: this is an unfinished project. This project was developed in my 
spare time. Now I don't have much time to devote to this project.

`TODO`

### Setup project

 - Download/pull/fork this repository
 - In [`keys.properties`](keys.properties) file create property named 
   `tmdbApiKey` and assign it to your API key 
   (you can get one [here](https://www.themoviedb.org/settings/api)).
 - Build, compile, and deploy on your target device.

### Originally planned

 - `TODO`

### Known issues

 - If a large number of activities have been opened, an `OutOfMemoryException` 
   might be thrown. Possible solution: implement custom SavedState manager on
   an application level which, based on system trim messages, would trim SavedState
   until only necessary information is retained in memory, sush as the ID of
   a movie. Another solution would be to serialize app's SavedState using custom
   SavedState manager mentioned above.
   
 - `TODO`