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
- Downgrade Playframework 2.4 -> 2.3: WS does not build queryString properly in Play 2.4...
- Bug fix: Twatcher cannot run registered scripts.

### v1.2.1
- Modify file constructure.  
  `data/` directory is DB and log files, `scripts` directory is user scripts
- Fix Linux shellscript.

### v1.2.2
- Wait until script/twitter ends
- Do not run script repeatedly on server mode.
