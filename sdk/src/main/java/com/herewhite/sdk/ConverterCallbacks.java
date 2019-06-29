package com.herewhite.sdk;

import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConversionInfo;

public interface ConverterCallbacks {
    void onProgress(Double progress, ConversionInfo convertInfo);
    void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo);
    void onFailure(ConvertException e);
}
