package FooDoNetServerClasses;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Map;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ImageDictionarySyncronized;
import UIUtil.RoundedImageView;
import upp.foodonet.R;

/**
 * Created by Asher on 06.02.2016.
 */
public class ImageDownloader {

    private static final String MY_TAG = "food_imageDownloader";

    private Context context;
    private Bitmap defaultBitmap;
    String imageFolderPath;
    String imageRepositoryBaseUrl;
    ImageDictionarySyncronized imageDictionary;

    public ImageDownloader(Context context, ImageDictionarySyncronized imageDictionary){
        this.context = context;
        defaultBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.foodonet_logo_200_200);
        imageFolderPath = context.getString(R.string.image_folder_path);
        imageRepositoryBaseUrl = context.getString(R.string.amazon_base_url_for_images);
        this.imageDictionary = imageDictionary;
    }

    public void Download(int publicationID, int publicationVersion, ImageView imageView){
        BitmapDrawable bitmapDrawable = CommonUtil.GetImageFromFileForPublication(context, publicationID, publicationVersion, null, 100);
        String fileName = CommonUtil.GetFileNameByIdAndVersion(publicationID, publicationVersion);
        if(bitmapDrawable == null){
            ForceDownload(publicationID, fileName, imageFolderPath, imageRepositoryBaseUrl, imageView);
        } else {
            imageDictionary.Put(publicationID, bitmapDrawable);
            CancelPotentialDownload(fileName, imageView);
            imageView.setImageDrawable(bitmapDrawable);
        }
    }

    private void ForceDownload(int id, String fileName, String imageFolderPath, String imageRepositoryBaseUrl, ImageView imageView) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (fileName == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (CancelPotentialDownload(fileName, imageView)) {

            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, id, fileName, imageRepositoryBaseUrl, imageFolderPath, imageDictionary);
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task, defaultBitmap, context.getResources());
                    imageView.setImageDrawable(downloadedDrawable);
                    imageView.setMinimumHeight(156);
                    task.execute();

        }
    }

    private static boolean CancelPotentialDownload(String fileName, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.fileName;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(fileName))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    class BitmapDownloaderTask extends AsyncTask<Void, Void, Bitmap> {
        public String fileName;
        int publicationID;
        private String baseUrl;
        private String imageFolderPath;
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ImageDictionarySyncronized> imageDictionary;

        private String getImageUrl(){
            return baseUrl + "/" + fileName;
        }

        public BitmapDownloaderTask(ImageView imageView, int id, String fileName, String baseUrl, String imageFolderPath, ImageDictionarySyncronized imageDictionary) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.fileName = fileName;
            this.publicationID = id;
            this.baseUrl = baseUrl;
            this.imageFolderPath = imageFolderPath;
            this.imageDictionary = new WeakReference<ImageDictionarySyncronized>(imageDictionary);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(Void... params) {
            return CommonUtil.LoadAndSavePicture(null, getImageUrl(), 100, imageFolderPath, fileName);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap == null){
                if(imageDictionary != null){
                    ImageDictionarySyncronized dictionary = imageDictionary.get();
                    dictionary.Put(publicationID, new BitmapDrawable(defaultBitmap));
                }
            }


            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
                if ((this == bitmapDownloaderTask)) {
                    imageView.setImageBitmap(bitmap);
                }
                if(imageDictionary != null){
                    ImageDictionarySyncronized dictionary = imageDictionary.get();
                    dictionary.Put(publicationID, new BitmapDrawable(bitmap));
                }
            }
        }
    }


    static class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, Bitmap defaultBitmap, Resources resources) {
            super(resources, defaultBitmap);
            this.bitmapDownloaderTaskReference =
                    new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }


}
