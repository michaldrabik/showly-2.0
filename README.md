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
   <img src="assets/screenshots/screenshot1.png" width="150" alt="screenshot 1">
   <img src="assets/screenshots/screenshot2.png" width="150" alt="screenshot 1">
   <img src="assets/screenshots/screenshot5.png" width="150" alt="screenshot 1">
   <img src="assets/screenshots/screenshot3.png" width="150" alt="screenshot 1">
   <img src="assets/screenshots/screenshot4.png" width="150" alt="screenshot 1">
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
4. Add your [Trakt.tv](https://trakt.tv/oauth/applications) and [TVDB](https://thetvdb.com/dashboard/account/apikey) api keys as following properties into your `local.properties` file located in the root directory of the project:
```
traktClientId="your trakt client id"
traktClientSecret="your trakt client secret"
tvdbApiKey="your tvdb api key"
```
5. Generate your own Firebase `google-services.json` file and put it in the `/app` directory.
6. Rebuild and start the app.

## Issues & Contributions

Feel free to post ideas and problems as Github Issues.

Pull requests are welcome. Remember about leaving a comment in the relevant issue if you are working on something.

## Contact

Landing Page: www.showlyapp.com
Feel free to write an email: showlyapp@gmail.com
