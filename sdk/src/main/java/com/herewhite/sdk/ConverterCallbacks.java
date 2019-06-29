package com.herewhite.sdk;

import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConversionInfo;

public interface ConverterCallbacks {
    public void onProgress(Double progress, ConversionInfo convertInfo);
    public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo);
    public void onFailure(Exception e);
}
