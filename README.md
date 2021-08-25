![Version](https://img.shields.io/github/v/tag/michaldrabik/showly-2.0?label=version)
![Build](https://img.shields.io/github/workflow/status/michaldrabik/showly-2.0/Master%20Flow)
![Twitter](https://img.shields.io/twitter/follow/AppShowly?style=social)

# Showly 2.0
<img src="https://i.ibb.co/ChBN7Lg/ic-launcher.png" align="left" width="180" hspace="10" vspace="10" />

Showly 2.0 is modern, slick, open source Android TV Shows Tracker.

Available on the Google Play Store and soon also on F-Droid.

<a href="https://play.google.com/store/apps/details?id=com.michaldrabik.showly2">
  <img
    alt="Get it on Google Play"
    height="80"
    src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"/>
</a>

## Screenshots

<div>
   <img src="assets/screenshots/github1.jpg" width="150" alt="Screenshot 1">
   <img src="assets/screenshots/github2.jpg" width="150" alt="Screenshot 1">
   <img src="assets/screenshots/github3.jpg" width="150" alt="Screenshot 1">
   <img src="assets/screenshots/github4.jpg" width="150" alt="Screenshot 1">
   <img src="assets/screenshots/github5.jpg" width="150" alt="Screenshot 1">
</div>

## Project Setup

1. Clone repository and open project in the latest version of Android Studio.
2. Create `keystore.properties` file and put it in the `/app` folder.
3. Add following properties into `keystore.properties` file (values are not important at this moment):
```
keyAlias=github
keyPassword=github
storePassword=github
```
4. Add your [Trakt.tv](https://trakt.tv/oauth/applications), [TMDB](https://developers.themoviedb.org/3/), [OMDB](http://www.omdbapi.com) and [Reddit](https://www.reddit.com/prefs/apps) API keys as following properties into your `local.properties` file located in the root directory of the project:
```
traktClientId="your trakt client id"
traktClientSecret="your trakt client secret"
tmdbApiKey="your tmdb api key"
omdbApiKey="your omdb api key"
redditClientId="your reddit client id"
```
5. Generate your own Firebase `google-services.json` file and put it in the `/app` directory.
6. Rebuild and start the app.

## Issues & Contributions

Feel free to post ideas and problems as Github Issues.

Pull requests are welcome. Remember about leaving a comment in the relevant issue if you are working on something.

### Language Translations

I'm always looking for help with translating app into more languages.<br>
If you are interested in helping now or in the future, please visit our CrowdIn project and join:<br>
https://crwd.in/showly-android-app

## Contact

Twitter: https://twitter.com/AppShowly

Landing Page: www.showlyapp.com

Email: showlyapp@gmail.com
