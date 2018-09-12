package com.ngoe.myalbum;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class Main2Activity extends AppCompatActivity implements AdapterView.OnItemClickListener {
        ArrayList<String> imageIDs = new ArrayList<>();
        ImageView imageView;
        int currentImageCount=0;
        ProgressDialog progressDialog;
        AdRequest adRequest;
        AdView banner;
        InterstitialAd interstitialAd;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);
            setTitle(getWhat("name"));

            adRequest = new AdRequest.Builder().build();
            banner = findViewById(R.id.adView);
            banner.loadAd(adRequest);
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId("ca-app-pub-2780984156359274/6000153603");
            interstitialAd.loadAd(adRequest);
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    loadAD();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    loadAD();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    loadAD();
                }
            });
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Export!");

            List<String> strings;
            try {
                strings = getImage(this);
                for (int i =1;i<strings.size();i++){
                    imageIDs.add(strings.get(i));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Gallery gallery = findViewById(R.id.mygallery);
            gallery.setAdapter(new ImageAdapter(this));
            gallery.setOnItemClickListener(this);

            imageView=findViewById(R.id.myimage);
            imageView.setLongClickable(true);
            try {
                InputStream ims = getAssets().open("image/"+imageIDs.get(0));
                Drawable d = Drawable.createFromStream(ims, null);
                imageView.setImageDrawable(d);
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    PhotoViewAttacher pAttacher;
                    pAttacher = new PhotoViewAttacher(imageView);
                    pAttacher.update();
                    return true;
                }
            });
        }

    private void loadAD() {
            if (!interstitialAd.isLoaded()){
                interstitialAd.loadAd(adRequest);
            }
    }

    private void showAD(){
            if (interstitialAd.isLoaded()){
                interstitialAd.show();
            }else{
                interstitialAd.loadAd(adRequest);
            }
    }

    public void onItemClick(AdapterView adapterView,View view,int position,long id) {
            currentImageCount = position;
            try {
                InputStream ims = getAssets().open("image/"+imageIDs.get(position));
                Drawable d = Drawable.createFromStream(ims, null);
                imageView.setImageDrawable(d);
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }

        public class ImageAdapter extends BaseAdapter {
            Context ctx;
            int itemBackground;
            public ImageAdapter(Context ctx) {
                this.ctx = ctx;
                TypedArray array = obtainStyledAttributes(R.styleable.MyGallery);
                itemBackground = array.getResourceId(R.styleable.MyGallery_android_galleryItemBackground, 0);
                array.recycle();
            }

            public int getCount() {
                return imageIDs.size();
            }

            public Object getItem(int position) {
                return position;
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView=new ImageView(ctx);
                try {
                    InputStream ims = getAssets().open("image/"+imageIDs.get(position));
                    Drawable d = Drawable.createFromStream(ims, null);
                    imageView.setImageDrawable(d);
                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new Gallery.LayoutParams(250,220));
                imageView.setBackgroundResource(itemBackground);
                return imageView;
            }
        }

    private List<String> getImage(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list("image");
        List<String> it= Arrays.asList(files);
        return it;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()){
                case R.id.saveOne:
                    if (checkPermissions()==true) {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(imageIDs.get(currentImageCount));
                        new saveALL().execute(arrayList);
                    }
                    break;
                case R.id.saveAll:
                    if (checkPermissions()==true) {
                        new saveALL().execute(imageIDs);
                    }
                    break;
                case R.id.about:
                    showABOUT();
                    break;
            }
        return super.onOptionsItemSelected(item);
    }

    class saveALL extends AsyncTask<ArrayList<String>,Integer,Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Saving...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>[] arrayLists) {
            boolean check = false;
            for (int i=0;i<arrayLists[0].size();i++) {
                check = exportImage(arrayLists[0].get(i));
            }
            return check;
        }

        @Override
        protected void onPostExecute(final Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAD();
                    AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                    if (aBoolean==true){
                        builder.setTitle("Completed!");
                        builder.setMessage("Photos saved in :\n"+Environment.getExternalStorageDirectory()+"/Image2APK/");
                    }else{
                        builder.setTitle("ERROR!");
                        builder.setMessage("Please try again :(");
                    }
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showAD();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    public boolean exportImage(String image){
            NgarLoe ngarLoe = new NgarLoe();
            File output = new File(Environment.getExternalStorageDirectory()+"/Image2APK/");
            if (!output.exists()){
                output.mkdirs();
            }
            boolean check = ngarLoe.assets2SD(this,"image/"+image,output.toString()+"/",image);
            return check;
    }

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(Main2Activity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 5217);
            return false;
        }
        Log.d("Permission","Permission"+"\n"+String.valueOf(true));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5217: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please try again :)", Toast.LENGTH_SHORT).show();
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow Write Storage Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public String getWhat(String what) {
        String json = null;
        try {
            InputStream is = getAssets().open("name");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        JSONObject obj;
        try {
            obj = new JSONObject(json);
            json = obj.getString(what);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getAPKVersion(){
        String version = "1.0";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version  = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public void showABOUT(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("APK Name : "+getWhat("name")+"\nVersion : "+getAPKVersion()+"\nPackage : "+getPackageName()+"\n\nCreated with Photo APK Creator!")
                .setTitle("❤️ ABOUT APK ❤️")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAD();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAD();
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.myanmarapk.com/p/photo-apk-creator.html")),""));
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Notice!")
                .setIcon(R.drawable.icon)
                .setCancelable(false)
                .setMessage("Do you want to exit ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAD();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAD();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
