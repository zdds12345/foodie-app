package com.zhengdesheng.z202304100318.ademo.ui.map;

import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;

public class PoiSearchListener implements OnGetPoiSearchResultListener {
    private final OnPoiResultCallback callback;

    public interface OnPoiResultCallback {
        void onPoiResult(PoiResult result);
    }

    public PoiSearchListener(OnPoiResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (callback != null) {
            callback.onPoiResult(result);
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult result) {
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult result) {
    }
}
