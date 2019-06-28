package com.herewhite.sdk;

import com.herewhite.sdk.domain.ConvertedPpt;
import com.herewhite.sdk.domain.PptConvertInfo;

public interface PptConverterCallbacks {
    public void onProgress(Double progress, PptConvertInfo convertInfo);
    public void onFinish(ConvertedPpt ppt, PptConvertInfo convertInfo);
    public void onFailure(Exception e);
}
