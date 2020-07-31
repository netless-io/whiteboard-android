package com.herewhite.sdk;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;

import com.google.gson.Gson;
import com.herewhite.sdk.domain.WhiteObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;

/**
 * white on 2018/8/10.
 */

public class WhiteboardView extends DWebView {


    public WhiteboardView(Context context) {
        super(getFixedContext(context));
        init();
    }

    public WhiteboardView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
        init();
    }

    public static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.createConfigurationContext(new Configuration());
        } else {
            return context;
        }
    }

    private void init() {
        getSettings().setMediaPlaybackRequiresUserGesture(false);
        loadUrl("file:///android_asset/whiteboard/index.html");
        setWebChromeClient(new FixWebChromeClient());
        int version = getWebViewVersion();
        if (version >= 55 && version < 68) {
            // https://github.com/chartjs/Chart.js/issues/5676
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private int getWebViewVersion() {
        String userAgent = getSettings().getUserAgentString();
        Pattern pattern = Pattern.compile("Chrome/([\\d]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            String group = matcher.group(1);
            return Integer.valueOf(group);
        }
        return 0;
    }

    private final static Gson gson = new Gson();


    public <T> void callHandler(String method, Object[] args, OnReturnValue<T> handler) {
        super.callHandler(method, toMaps(args), handler);
    }

    public void callHandler(String method, Object[] args) {
        this.callHandler(method, args, null);
    }

    public <T> void callHandler(String method, OnReturnValue<T> handler) {
        this.callHandler(method, null, handler);
    }

    private Object[] toMaps(Object[] objects) {
        if (objects != null) {
            Object[] maps = new Object[objects.length];
            for (int i = 0; i < objects.length; i++) {
                maps[i] = toJSON(objects[i]);
            }
            return maps;
        } else {
            return new Object[0];
        }
    }

    private static final Class[] PRIMITIVE_TYPES = { int.class, long.class, short.class,
            float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
            Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };
    private static List<Class> PRIMITIVE_LIST = new ArrayList<>(Arrays.asList(PRIMITIVE_TYPES));

    private static boolean isPrimitiveOrStringOrNull(Object target) {
        if (target instanceof String) {
            return true;
        } else if (target == null) {
            return true;
        }

        return PRIMITIVE_LIST.contains(target.getClass());
    }

    private Object toJSON(Object object) {

        if (isPrimitiveOrStringOrNull(object)) {
            return object;
        } else if (object instanceof WhiteObject) {
            return ((WhiteObject) object).toJSON();
        } else if (object instanceof WhiteObject[]) {
            List<JSONObject> list = new ArrayList<>();
            for (int i=0; i<((WhiteObject[]) object).length; i++) {
                list.add(((WhiteObject[])object)[i].toJSON());
            }
            return list;
        } else {
            return gson.toJson(object);
        }
    }

    class FixWebChromeClient extends WebChromeClient {
        @Override
        public Bitmap getDefaultVideoPoster() {
            try{
                int width = 100;
                int height = 50;
                // fix https://bugs.chromium.org/p/chromium/issues/detail?id=521753#c8
                return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }catch(Exception e){
                return super.getDefaultVideoPoster();
            }
        }
    }
}
