package com.farhanapps.HitChat.interfaces;

/**
 * Created by farhan on 24-04-2016.
 */
public interface UploadListener {
    void onUploadStart();
    void onUploadProgress(int progress);
    void onUploadFinish(String jsonArray);
    void onUploadError(String jsonArray);
}
