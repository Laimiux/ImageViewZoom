package it.sephiroth.android.library.imagezoom.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchDoubleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.OnDrawableChangeListener;
import it.sephiroth.android.library.imagezoom.test.utils.DecodeUtils;

public class ImageViewTestActivity extends AppCompatActivity {

    private static final String LOG_TAG = "image-test";

    ImageViewTouch mImage;
    Button mButton1;
    Button mButton2;
    CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mImage = (ImageViewTouch) findViewById(R.id.image);

        // set the default image display type
        mImage.setDisplayType(DisplayType.FIT_IF_BIGGER);

        mButton1 = (Button) findViewById(R.id.button);
        mButton2 = (Button) findViewById(R.id.button2);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox1);

        mButton1.setOnClickListener(
            new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ActivityCompat.requestPermissions(ImageViewTestActivity.this,
								new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    } else {
                        // Return a successful result right away.
                        onRequestPermissionsResult(0, new String[]{}, new int[]{PackageManager.PERMISSION_GRANTED});
                    }
                }
            }
        );

        mButton2.setOnClickListener(
            new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int current = mImage.getDisplayType().ordinal() + 1;
                    if (current >= DisplayType.values().length) {
                        current = 0;
                    }

                    mImage.setDisplayType(DisplayType.values()[current]);
                }
            }
        );

        mImage.setSingleTapListener(
            new OnImageViewTouchSingleTapListener() {

                @Override
                public void onSingleTapConfirmed() {
                    Log.d(LOG_TAG, "onSingleTapConfirmed");
                }
            }
        );

        mImage.setDoubleTapListener(
            new OnImageViewTouchDoubleTapListener() {

                @Override
                public void onDoubleTap() {
                    Log.d(LOG_TAG, "onDoubleTap");
                }
            }
        );

        mImage.setOnDrawableChangedListener(
            new OnDrawableChangeListener() {

                @Override
                public void onDrawableChanged(Drawable drawable) {
                    Log.i(LOG_TAG, "onBitmapChanged: " + drawable);
                }
            }
        );
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        List<Integer> results = new ArrayList<>();
        for (Integer result : grantResults) {
            results.add(result);
        }

        if (results.contains(PackageManager.PERMISSION_GRANTED)) {
            selectRandomImage(mCheckBox.isChecked());
        }
    }

    public void selectRandomImage(boolean small) {
        Cursor c = getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (c != null) {
            int position = 0;
            if (c.moveToPosition(position)) {
                long id = c.getLong(c.getColumnIndex(Images.Media._ID));
                Uri imageUri = Uri.parse(Images.Media.EXTERNAL_CONTENT_URI + "/" + id);

                Log.d("image", imageUri.toString());

                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                int size = (int) (Math.min(metrics.widthPixels, metrics.heightPixels) / 0.55);

                if (small) {
                    size /= 3;
                }

                Bitmap bitmap = DecodeUtils.decode(this, imageUri, size, size);

                if (null != bitmap) {
                    Log.d(LOG_TAG, "screen size: " + metrics.widthPixels + "x" + metrics.heightPixels);
                    Log.d(LOG_TAG, "bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    mImage.setOnDrawableChangedListener(
                        new OnDrawableChangeListener() {
                            @Override
                            public void onDrawableChanged(final Drawable drawable) {
                                Log.v(LOG_TAG, "image scale: " + mImage.getScale() + "/" + mImage.getMinScale());
                                Log.v(LOG_TAG, "scale type: " + mImage.getDisplayType() + "/" + mImage.getScaleType());

                            }
                        }
                    );
                    mImage.setImageBitmap(bitmap, null, -1, -1);

                } else {
                    Toast.makeText(this, "Failed to load the image", Toast.LENGTH_LONG).show();
                }
            }
            c.close();
        }
    }
}
