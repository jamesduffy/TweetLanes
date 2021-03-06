/*
 * Copyright (C) 2013 Chris Lacy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.tweetalib.android.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SecureCacheResponse;

import org.appdotnet4j.model.AdnPostCompose;
import twitter4j.StatusUpdate;

public class TwitterStatusUpdate {

    public TwitterStatusUpdate(String status, Long inReplyToStatusId) {
        mStatus = status;
        mInReplyToStatusId = inReplyToStatusId;
    }


    public TwitterStatusUpdate(String status) {
        this(status, null);
    }

    public StatusUpdate getT4JStatusUpdate() {
        StatusUpdate statusUpdate = new StatusUpdate(mStatus);

        if (mInReplyToStatusId != null) {
            statusUpdate.setInReplyToStatusId(mInReplyToStatusId);
        }

        if (mMediaFilePath != null) {
            try{
                statusUpdate.setMedia(getMediaFile(mMediaFilePath));
            }
            catch (IOException error){
                error.printStackTrace();
            }
            catch (OutOfMemoryError error){
                error.printStackTrace();
            }
        }

        return statusUpdate;
    }

    public AdnPostCompose getAdnComposePost() {
        File mediaFile = null;
        try{
            mediaFile = getMediaFile(mMediaFilePath);
        }
        catch (IOException error){
            error.printStackTrace();
        }
        catch (OutOfMemoryError error){
            error.printStackTrace();
        }

        AdnPostCompose statusUpdate = new AdnPostCompose(mStatus, mInReplyToStatusId, mediaFile);
        return statusUpdate;
    }

    public void setMediaFilePath(String mediaFilePath) {
        mMediaFilePath = mediaFilePath;
    }

    String mStatus;
    Long mInReplyToStatusId;
    String mMediaFilePath;


    private File getMediaFile(String mediaFilePath) throws IOException {

        if(mediaFilePath==null){
            return null;
        }

        File originalFile = new File(mediaFilePath);
        Bitmap resizeImage = TryResizeImage(originalFile);

        if (resizeImage == null){
            return originalFile;
        }

        File resizedFile = SaveImage(resizeImage);

        if (resizedFile==null)
        {
            return originalFile;
        }

        return resizedFile;
    }

    private Bitmap TryResizeImage(File originalFile) throws FileNotFoundException
    {
        Bitmap resizeImage = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeStream(new FileInputStream(originalFile), null, options);
        int width_tmp = options.outWidth;
        int height_tmp = options.outHeight;
        int scale = 1;

        int requiredWidth = 1500;
        int requiredHeight = 1500;

        while (width_tmp / 2 >= requiredWidth || height_tmp / 2 >= requiredHeight)
        {
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        if (scale > 1)
        {
            // decode with inSampleSize
            options = new BitmapFactory.Options();
            options.inSampleSize = scale;

            resizeImage = BitmapFactory.decodeStream(new FileInputStream(originalFile), null, options);
        }

        return resizeImage;
    }

    private File SaveImage(Bitmap resizeImage) throws IOException
    {
        File path = new File(Environment.getExternalStorageDirectory(),"temp/images/Tweet Lanes");

        File tempFile = null;

        OutputStream outStream = null;

        try
        {
            path.mkdirs();
            tempFile = File.createTempFile("img", ".jpeg", path);
            outStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            resizeImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        }
        catch (Exception ex)
        {
            tempFile = null;
        }
        finally
        {
            if (outStream != null) {
                try {
                    outStream.close();
                    resizeImage.recycle();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempFile;
    }
}
