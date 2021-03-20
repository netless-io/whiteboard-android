package com.herewhite.sdk;

import com.herewhite.sdk.domain.ConversionInfo;
import com.herewhite.sdk.domain.ConvertException;
import com.herewhite.sdk.domain.ConvertedFiles;

/**
 * @deprecated 空实现类由用户应用处理
 */
@Deprecated
public class AbstractConverterCallbacks implements ConverterCallbacks {
    @Override
    public void onProgress(Double progress, ConversionInfo convertInfo) {

    }

    @Override
    public void onFinish(ConvertedFiles ppt, ConversionInfo convertInfo) {

    }

    @Override
    public void onFailure(ConvertException e) {

    }
}
