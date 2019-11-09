## Movies

**Note**: this is an unfinished project. This project was developed in my 
spare time. Now I don't have much time to devote to this project.

`TODO`

### Setup project

 - Download/pull/fork this repository.
 
 - In `local.properties` file create property named 
   `tmdbApiKey` and assign it to your API key 
   (you can get one [here](https://www.themoviedb.org/settings/api)).
   
 - Build, compile, and deploy on your target device.

### In plans

 - **Reformat code to follow best coding practices**.

 - Implement **global search**, which allows to search not only movies, but also
   TV shows and people. See [this](https://developers.themoviedb.org/3/search/multi-search)
   for more details.
   
 - [Update UI](https://www.behance.net/gallery/61856793/Kino-the-Movie-iOS-UI-Kit).

 - Add **home screen** where users can see short list of now-trending
   movies, TV shows and potentially much more...
   
 - Implement **Watchlist** functionality that would allow users to add movies & TV shows
   to "Watch later" lists. Try reusing Favourites functionality here.
   
 - Add **account integration** with either TMDB or my own (why not) server.
   Allow users to [create](https://developers.themoviedb.org/3/authentication/how-do-i-generate-a-session-id)
   their accounts, sync their settings (requires custom server), 
   [favourites](https://developers.themoviedb.org/3/account/mark-as-favorite)
   & [watchlist](https://developers.themoviedb.org/3/account/add-to-watchlist)
   with the server & other devices, 
   [personalize](https://developers.themoviedb.org/4/account/get-account-movie-recommendations) 
   content recommendations, etc.

### Known issues

 - If a large number of activities have been opened, an `OutOfMemoryException` 
   might be thrown. Possible solution: implement custom SavedState manager on
   an application level which, based on system trim messages, would trim SavedState
   until only necessary information is retained in memory, sush as the ID of
   a movie. Another solution would be to serialize app's SavedState using custom
   SavedState manager mentioned above.
   
 - Backdrop in Details doesn't scroll correctly. Probably requires implementing
   custom CoordinatorLayout Behavior.
   
 - `TODO`
