package top.leixiao.mindnote.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtil {
    public static final float MAX_IMAGE_SIZE = 1800.0f;
    public static final int RESULT_DATA_DIR_FAIL = -4;
    public static final int RESULT_FILE_NOT_AVAILABLE = -2;
    public static final int RESULT_OTHER_FAILURE = -5;
    public static final int RESULT_SAVE_FAILURE = -3;
    public static final int RESULT_SPACE_NOT_ENOUGH = -1;
    public static final int RESULT_SUCCESS = 0;
    static final String TAG = "ImageUtil";
    static Config config = Config.ARGB_8888;

    private static Bitmap decodeUriToBitmap(ContentResolver cr, Uri uri, Options options) {
        Bitmap bm = null;
        InputStream is = null;
        try {
            is = cr.openInputStream(uri);
            bm = BitmapFactory.decodeStream(is, null, options);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
            Log.e("decodeImageToBitmap", e2.toString());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
        }
        return bm;
    }

    public static Bitmap decodeImageToBitmap(Context context, Uri uri) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = config;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        Bitmap bm = decodeUriToBitmap(context.getContentResolver(), uri, opts);
        opts.inSampleSize = (int) Math.floor((double) Math.max(((float) opts.outWidth) / MAX_IMAGE_SIZE, ((float) opts.outHeight) / MAX_IMAGE_SIZE));
        opts.inJustDecodeBounds = false;
        return rotateBitmapwithRotater(decodeUriToBitmap(context.getContentResolver(), uri, opts), ReflectUtils.getOrientFromInputStream(context, uri));
    }

    public static Bitmap rotateBitmapwithRotater(Bitmap b, int rotater) {
        if (b == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        switch (rotater) {
            case 3 /*3*/:
                matrix.setRotate(180.0f);
                break;
            case 6 /*6*/:
                matrix.setRotate(90.0f);
                break;
            case 8 /*8*/:
                matrix.setRotate(270.0f);
                break;
            default:
                return b;
        }
        Bitmap newbit = Bitmap.createBitmap(b, RESULT_SUCCESS, RESULT_SUCCESS, b.getWidth(), b.getHeight(), matrix, true);
        if (newbit != b) {
            b.recycle();
        }
        return newbit;
    }

    //严重问题注释了
    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        boolean z = false;
        if (filename != null) {
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
            if (filename.endsWith(".png")) {
                format = Bitmap.CompressFormat.PNG;
            }
            OutputStream stream = null;

            try {
                stream = new FileOutputStream(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            z = bmp.compress(format, 100, stream);

            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    //始终返回true，空间始终够用
    public static boolean checkSdcardAvailableSpace(long size) {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return false;
        }

/*        long availableSize = new StatFs(Environment.getExternalStorageDirectory().getPath()).getAvailableBytes();
        if (availableSize < size || availableSize == 0) {
            return false;
        }*/
        return true;
    }

    //我修改过的方法
    public static File getImageFile(Context context, Uri uri, String uuid) {
        if (uri == null) {
            return null;
        }
        File file = null;
        //准备前缀
        String prefix = "img_" + NoteUtil.getTimeString();
        //永远不会重复，应为时间作为参数
        file = NoteUtil.getFile(uuid, prefix + ".jpg");

        if (file.getParentFile().exists()) {//为什么返回,因为文件夹已建好，文件还没有建
            Log.d(TAG, "getImageFile: else if");
            return file;
        } else {//新建笔记时，第一次加图片会进入else，来建立文件夹
            Log.d(TAG, "getImageFile: else");
            File pDataDir = new File(NoteUtil.FILES_ANDROID_DATA);
            if (pDataDir == null || !pDataDir.exists()) {
                Log.d(TAG, "Android data dir not exist.");
                return null;
            } else if (file.getParentFile().mkdirs()) {
                return file;
            } else {
                Log.d(TAG, "mkdirs fail: " + file.getParentFile().getPath());
                return null;
            }
        }
    }


    //我修改的方法
    public static int saveIntoFile(Context context, Uri uri, File file) throws IOException {

        if (file == null || uri == null) {
            return RESULT_SUCCESS;
        }
        Bitmap bmp = decodeImageToBitmap(context, uri);
        if (bmp == null) {
            return RESULT_FILE_NOT_AVAILABLE;
        }
        MemoByteArrayOutputStream baos = new MemoByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        FileOutputStream os = new FileOutputStream(file);
        baos.writeTo(os);
        if (os != null) {
            os.close();
        }
        if (baos != null) {
            baos.close();
        }
        if (bmp == null) {
            return 0;
        }
        bmp.recycle();
        return 0;
    }

    public static void getImageSizeRect(String fileName, Rect rect) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(fileName, opts);
        rect.set(RESULT_SUCCESS, RESULT_SUCCESS, opts.outWidth, opts.outHeight);
        if (bm != null) {
            bm.recycle();
        }
    }

    public static class NoteImageGetter {
        Context mContext;
        int mWidth;

        public NoteImageGetter(Context context, int width) {
            this.mWidth = width;
            this.mContext = context;
        }

        public BitmapDrawable getDrawable(String source) {
            BitmapDrawable drawable = createFromPath(source);
            if (drawable != null) {
                drawable.setBounds(ImageUtil.RESULT_SUCCESS, ImageUtil.RESULT_SUCCESS, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            return drawable;
        }

        public BitmapDrawable createFromPath(String pathName) {
            Options options;
            if (pathName == null) {
                return null;
            }
            File image = new File(pathName);
            if (!image.exists()) {
                return null;
            }
            Bitmap bm = null;
            try {
                Options opts;
                Options opts1 = new Options();
                try {
                    opts1.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(image.toString(), opts1);
                    opts = new Options();
                } catch (Exception e) {
                    options = opts1;
                    if (bm != null) {
                        return null;
                    }
                    bm.recycle();
                    return null;
                }
                try {
                    opts.inSampleSize = opts1.outWidth / this.mWidth;
                    opts.inPreferredConfig = ImageUtil.config;
                    opts.inJustDecodeBounds = false;
                    opts.inInputShareable = true;
                    opts.inPurgeable = true;
                    bm = BitmapFactory.decodeFile(image.toString(), opts);
                    Bitmap pic = bm;
                    if (!(bm == null || pic == bm)) {
                        bm.recycle();
                    }
                    if (pic != null) {
                        return new BitmapDrawable(this.mContext.getResources(), pic);
                    }
                    return null;
                } catch (Exception e2) {
                    options = opts1;
                    Options options2 = opts;
                    if (bm != null) {
                        return null;
                    }
                    bm.recycle();
                    return null;
                }
            } catch (Exception e3) {
                if (bm != null) {
                    return null;
                }
                bm.recycle();
                return null;
            }
        }
    }

    public static class WidgetImageGetter {
        public Bitmap getBitmap(Context context, Uri uri, int maxWidth, int maxHeight) {
            Bitmap bmp = ImageUtil.decodeImageToBitmap(context, uri);
            if (bmp == null) {
                return null;
            }
            int originWidth = bmp.getWidth();
            int width = originWidth;
            int height = bmp.getHeight();
            Bitmap ret = bmp;
            if (width != maxWidth) {
                width = maxWidth;
                height = (int) Math.floor(((double) height) / ((double) ((((float) originWidth) * 1.0f) / ((float) width))));
                Bitmap pic = Bitmap.createScaledBitmap(bmp, width, height, true);
                if (pic != null) {
                    bmp.recycle();
                    ret = pic;
                }
            }
            if (height > maxHeight) {
                Bitmap bm = Bitmap.createBitmap(ret, ImageUtil.RESULT_SUCCESS, (height - maxHeight) / 2, width, maxHeight);
                if (bm != null) {
                    ret.recycle();
                    return bm;
                }
            }
            return ret;
        }
    }
}
