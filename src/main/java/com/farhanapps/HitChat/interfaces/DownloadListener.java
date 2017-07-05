package com.farhanapps.HitChat.interfaces;

/**
 * Created by farhan on 24-04-2016.
 */
public interface DownloadListener {
    void onDownloadStart();
    void onDownloadProgress(int progress);
    void onDownloadFinish();
    void onDownloadError(String error);
}
