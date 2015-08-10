### v1.0.0
- First release.

### v1.1.0
- Save configs to H2 database instead of JSON file.
- Enable setting configs (including Twitter login) via browser.
- Twitter client the app uses fixed to my twatcher app.  
  You can use own client app by changing conf/application.conf.

### v1.1.1
- Exclude API doc html files from zip.

### v1.1.2
- Bug fix: Twatcher regards as death if no Twitter account registered.

### v1.2.0
- Add server mode: Check whether Twitter is active or not each 15 minutes.
- Bug fix: Twatcher cannot run registered scripts.