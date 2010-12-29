//Runnable getImage = new Runnable() {
//        public void run() {
//          // get the image from http://developerlife.com/theblog/wp-content/uploads/2007/11/news-thumb.png
//          // save it here (user.dir/FILENAME)
//          // file is saved here on emulator - /data/data/com.developerlife/files/file.png
//          try {
//            Log.i(Global.TAG, "MainDriver: trying to download and save PNG file to user.dir");
//            HttpClient client = new HttpClient();
//            GetMethod get = new GetMethod("http://developerlife.com/theblog/wp-content/uploads/2007/11/news-thumb.png");
//            client.executeMethod(get);
//            byte[] bRay = get.getResponseBody();
//
//            FileOutputStream fos = openFileOutput(Global.FILENAME, Activity.MODE_WORLD_WRITEABLE);
//            fos.write(bRay);
//            fos.flush();
//            fos.close();
//            Log.i(Global.TAG, "MainDriver: successfully downloaded PNG file to user.dir");
//          }
//          catch (Exception e) {
//            Log.e(Global.TAG, "MainDriver: could not download and save PNG file", e);
//          }
//
//        }
//      };
//      new Thread(getImage).start();
