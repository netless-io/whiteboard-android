package com.herewhite.sdk;

import com.herewhite.sdk.domain.ConvertedPpt;
import com.herewhite.sdk.domain.PptConvertInfo;

public class AbstractPptConverterCallbacks implements PptConverterCallbacks {
    @Override
    public void onProgress(Double progress, PptConvertInfo convertInfo) {

    }

    @Override
    public void onFinish(ConvertedPpt ppt, PptConvertInfo convertInfo) {

    }

    @Override
    public void onFailure(Exception e) {

    }
}
