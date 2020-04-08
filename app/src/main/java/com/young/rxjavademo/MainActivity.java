package com.young.rxjavademo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    private int[] drawableRes;
    private static final String TAG = "young";
    private Disposable mDisposable;
    private List<Drawable> drawableList;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_view);
        drawableRes = new int[]{
                R.drawable.amap_bus, R.drawable.amap_car, R.drawable.amap_man,
                R.drawable.amap_ride, R.drawable.app_guide_broadcast_nor, R.drawable.app_guide_map_nor,
                R.drawable.app_guide_beauty_nor, R.drawable.app_guide_music_nor, R.drawable.app_guide_news_nor,
                R.drawable.app_guide_note_nor
        };
        drawableList = new ArrayList<>();
        myAdapter = new MyAdapter(this, drawableList);
        listView.setAdapter(myAdapter);

        rxJavaBaseUse();
        timeDoSomething();
        complicatedDoSomething();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 23);
            }
        }
    }

    private void rxJavaBaseUse() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("连载一");
                e.onNext("连载二");
                e.onNext("连载三");
                e.onComplete();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(String value) {
                Log.e(TAG, value);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        });
    }

    private void timeDoSomething() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(123);
                sleep(6000);
                e.onNext(456);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.e(TAG, integer + "");
                    }
                });
    }

    private void complicatedDoSomething() {
        Observable.create(new ObservableOnSubscribe<Drawable>() {
            @Override
            public void subscribe(ObservableEmitter<Drawable> e) throws Exception {
                for (int i = 0; i < drawableRes.length; i++) {
                    Drawable drawable = getResources().getDrawable(drawableRes[i]);
                    if (i == 5) {
                        sleep(6000);
                    }
                    if (i == 6) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        saveBitmap(bitmap, Bitmap.CompressFormat.PNG, "test.png");
                    }
                    e.onNext(drawable);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Drawable>() {
                    @Override
                    public void accept(Drawable drawable) throws Exception {
                        Log.e(TAG, drawable.toString());
                        drawableList.add(drawable);
                        myAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void saveBitmap(Bitmap bitmap, Bitmap.CompressFormat format, String name) {
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        } else {
            path = getFilesDir().getAbsolutePath();
        }
        File file = new File(path, name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(format, 100, fos);
            fos.close();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            sendBroadcast(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class MyAdapter extends BaseAdapter {

        private Context context;
        private List<Drawable> drawableList;

        public MyAdapter(Context context, List<Drawable> drawableList) {
            this.context = context;
            this.drawableList = drawableList;
        }

        @Override
        public int getCount() {
            return drawableList.size();
        }

        @Override
        public Object getItem(int position) {
            return drawableList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_image_view, parent, false);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.item_image_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imageView.setImageDrawable(drawableList.get(position));
            return convertView;
        }
    }

    private class ViewHolder {
        ImageView imageView;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 23) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已准许权限", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "没有权限无法使用该应用", Toast.LENGTH_LONG).show();
            }
        }
    }
}
