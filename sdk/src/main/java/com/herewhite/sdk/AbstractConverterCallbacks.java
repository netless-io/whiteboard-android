package com.herewhite.sdk;

import com.herewhite.sdk.domain.ConvertedFiles;
import com.herewhite.sdk.domain.ConversionInfo;

public class AbstractConverterCallbacks implements ConverterCallbacks {
    @Override
    public void onProgress(Double progress, ConversionInfo convertInfo) {

    }

    @Override
    public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {

    }

    @Override
    public void onFailure(Exception e) {

    }
}
