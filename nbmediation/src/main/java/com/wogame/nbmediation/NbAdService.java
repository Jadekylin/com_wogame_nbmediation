package com.wogame.nbmediation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.banner.AdSize;
import com.nbmediation.sdk.banner.BannerAd;
import com.nbmediation.sdk.banner.BannerAdListener;
import com.nbmediation.sdk.interstitial.InterstitialAd;
import com.nbmediation.sdk.interstitial.InterstitialAdListener;
import com.nbmediation.sdk.mobileads.TTAdManagerHolder;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAd;
import com.nbmediation.sdk.nativead.NativeAdListener;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.model.Scene;
import com.nbmediation.sdk.video.RewardedVideoAd;
import com.nbmediation.sdk.video.RewardedVideoListener;
import com.wogame.cinterface.AdInterface;
import com.wogame.cinterface.CallBackActivity;
import com.wogame.common.AppMacros;
import com.wogame.nbmediation.activity.SplashActivity;
import com.wogame.nbmediation.utils.NewApiUtils;

/**
 * 需要在主app 的 assets里加入hippo-ads-config.json文件
 * 注释lib model 也可以放，但考虑公用库就放入主app model 也是一样）
 */
public class NbAdService extends AdInterface {
    private static final String TAG = "hippo_sdk";

    private static NbAdService hippoAdService = null;
    private Activity mActivity;
    private static String sceneId;;
    private String mAppKey;
    private String mBannerKey;
    private String mNativeKey;

    private int mNativeExpressViewY = 0;
    private int mNativeExpressViewX = 0;
    private final float Screen_W = 1080;
    private final float Screen_H = 1920;
    private float mRScreenW = 0;
    private float gameAdW = 988; // 1080 设计屏幕宽度
    private float gameAdH = 705;
    private float mPasX;
    private float mPasY;
    private boolean mIsVideoComplete = false;

    private Resources mResources;

    private View mViewRoot;

    private LinearLayout adBannerContainer;
    private LinearLayout adNativeContainer;
    private View adView;
    private NativeAdView nativeAdView;

    private BannerAd bannerAd;
    private View mBannerView;

    private NativeAd nativeAd;
    private AdInfo mNativeInfo;

    private CallBackActivity mCallBackActivity;
    public static final String APPKEY = "kXDlKvOwFYf0inXBd65Pzo0vpF2utBim";

    public static final String P_BANNER = "260";
    public static final String P_NATIVE = "258";

    public static NbAdService getInstance() {
        if (hippoAdService == null) {
            hippoAdService = new NbAdService();
            hippoAdService.setDelegate(hippoAdService);
        }
        return hippoAdService;
    }

    public void initWithApplication(Application app,CallBackActivity callBackActivity) {
        mCallBackActivity = callBackActivity;
//        暂时使用与开屏广告
        TTAdManagerHolder.init(app,"5069114");
    }

    public void initActivity(Activity activity,
                             final String appKey,
                             final String bannerKey,
                             final String nativeKey,
                             final float w,final float h, final Resources resources) {
        mActivity = activity;
        mAppKey = appKey;
        mBannerKey = bannerKey;
        mNativeKey = nativeKey;
        mResources = resources;
        gameAdW = w;
        gameAdH = h;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        @SuppressLint("WrongViewCast") LinearLayout fragment_bannerad = (LinearLayout) mActivity.findViewById(R.id.ad_banner);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mViewRoot = inflater.inflate(R.layout.ad_activity_layout, fragment_bannerad, false);

        mActivity.addContentView(mViewRoot, params);

        SplashActivity splashActivity = new SplashActivity();
        splashActivity.initSplashActivity(mActivity);

        initData();
    }

    private void initData() {

        adBannerContainer = mViewRoot.findViewById(R.id.ad_banner);
        adNativeContainer = mViewRoot.findViewById(R.id.ad_native);
        NewApiUtils.printLog("start init sdk");
        NmAds.init(mActivity, mAppKey, new InitCallback() {
            @Override
            public void onSuccess() {
                NewApiUtils.printLog("init success");
                setVideoListener();
                setInterstitialListener();

                loadAndShowBanner();
                loadAndShowNative();
            }

            @Override
            public void onError(Error error) {
                NewApiUtils.printLog("init failed " + error.toString());
            }
        });
    }

    private void onLoad() {

    }

    private void setVideoListener() {
        RewardedVideoAd.setAdListener(new RewardedVideoListener() {

            @Override
            public void onRewardedVideoAvailabilityChanged(boolean b) {

            }

            @Override
            public void onRewardedVideoAdShowed(com.nbmediation.sdk.utils.model.Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdShowed " + scene);
            }

            @Override
            public void onRewardedVideoAdShowFailed(com.nbmediation.sdk.utils.model.Scene scene, Error error) {
                NewApiUtils.printLog("onRewardedVideoAdShowFailed " + scene);
            }

            @Override
            public void onRewardedVideoAdClicked(com.nbmediation.sdk.utils.model.Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdClicked " + scene);
                if (mCallBack != null) {
                    mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_AD_CLICK, sceneId,"","","");
                    mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_AD_CLICK, sceneId,"","","");
                }
            }

            @Override
            public void onRewardedVideoAdClosed(com.nbmediation.sdk.utils.model.Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdClosed " + scene);
                if (mIsVideoComplete) {
                    if (mCallBack != null) {
                        mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_SUCCESS,sceneId,"","","");
                        mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_SUCCESS, sceneId,"","","");
                    }
                } else {
                    if (mCallBack != null) {
                        mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_CANCEL, sceneId,"","","");
                        mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_CANCEL, sceneId,"","","");
                    }
                }
                mIsVideoComplete = false;
            }

            @Override
            public void onRewardedVideoAdStarted(com.nbmediation.sdk.utils.model.Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdStarted " + scene);
            }

            @Override
            public void onRewardedVideoAdEnded(com.nbmediation.sdk.utils.model.Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdEnded " + scene);
            }

            @Override
            public void onRewardedVideoAdRewarded(com.nbmediation.sdk.utils.model.Scene scene) {
                NewApiUtils.printLog("onRewardedVideoAdRewarded " + scene);
                mIsVideoComplete = true;
            }
        });
    }


    private void setInterstitialListener() {
        InterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdAvailabilityChanged(boolean b) {

            }

            @Override
            public void onInterstitialAdShowed(Scene scene) {

            }

            @Override
            public void onInterstitialAdShowFailed(Scene scene, Error error) {

            }

            @Override
            public void onInterstitialAdClosed(Scene scene) {
                if (mCallBack != null) {
                    mCallBack.onCallBack(AppMacros.AT_Interstitial,AppMacros.CALL_CANCEL, sceneId,"","","");
                    mCallBack.onAdStatusListen(AppMacros.AT_Interstitial,AppMacros.CALL_CANCEL, sceneId,"","","");
                }
            }

            @Override
            public void onInterstitialAdClicked(Scene scene) {
                if (mCallBack != null) {
                    mCallBack.onCallBack(AppMacros.AT_Interstitial, AppMacros.CALL_AD_CLICK, sceneId,"","","");
                    mCallBack.onAdStatusListen(AppMacros.AT_Interstitial, AppMacros.CALL_AD_CLICK, sceneId,"","","");
                }
            }
        });
    }

    public void loadAndShowBanner() {
        adBannerContainer.removeAllViews();

        if (bannerAd != null) {
            bannerAd.destroy();
        }
        bannerAd = new BannerAd(mActivity, mBannerKey, new BannerAdListener() {
            @Override
            public void onAdReady(View view) {
                try {
                    mBannerView = view;
                    if (null != view.getParent()) {
                        ((ViewGroup) view.getParent()).removeView(mBannerView);
                    }
                    if(mCallBack != null)
                        mCallBack.onAdStatusListen(AppMacros.AT_Banner_Bottom, AppMacros.CALL_AD_LOADED, "Banner","","","");
                    showBannerAds();
                } catch (Exception e) {
                    Log.e("AdtDebug", e.getLocalizedMessage());
                }
            }

            @Override
            public void onAdFailed(String error) {
                if(mCallBack != null)
                    mCallBack.onAdStatusListen(AppMacros.AT_Banner_Bottom, AppMacros.CALL_ERROR, "Banner","","",error);
            }

            @Override
            public void onAdClicked() {
                if(mCallBack != null)
                    mCallBack.onAdStatusListen(AppMacros.AT_Banner_Bottom, AppMacros.CALL_AD_CLICK, "Banner","","","");
            }
        });
        bannerAd.setAdSize(AdSize.AD_SIZE_320X50);
        bannerAd.loadAd();
    }

    private void showBannerAds(){
        if(mBannerView != null){
            adBannerContainer.removeAllViews();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            adBannerContainer.addView(mBannerView, layoutParams);

            if(mCallBack != null)
                mCallBack.onAdStatusListen(AppMacros.AT_Banner_Bottom, AppMacros.CALL_AD_SHOW, sceneId,"","","");
        }
    }

    //region Native
    public void loadAndShowNative() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        mNativeInfo = null;
        adNativeContainer.removeAllViews();
        nativeAd = new NativeAd(mActivity, mNativeKey, new NativeAdListener() {
            @Override
            public void onAdFailed(String msg) {
                Log.e("AdtDebug", msg);

                if(mCallBack != null)
                    mCallBack.onAdStatusListen(AppMacros.AT_Native, AppMacros.CALL_ERROR, "Native","","",msg);
            }

            @Override
            public void onAdReady(AdInfo info) {
                mNativeInfo = info;
                if(mCallBack != null)
                    mCallBack.onAdStatusListen(AppMacros.AT_Native, AppMacros.CALL_AD_LOADED, "Native","","","");
            }

            @Override
            public void onAdClicked() {
                Log.e("AdtDebug","onAdClicked");
                if(mCallBack != null)
                    mCallBack.onAdStatusListen(AppMacros.AT_Native, AppMacros.CALL_AD_CLICK, "Native","","","");
            }
        });
        nativeAd.loadAd();
    }

    private void showNative(){
        if(mNativeInfo != null){
            adNativeContainer.removeAllViews();
            if(mCallBack != null) mCallBack.onAdStatusListen(AppMacros.AT_Native, AppMacros.CALL_AD_SHOW, sceneId,"","","");
            adView = LayoutInflater.from(mActivity).inflate(R.layout.native_ad_layout, null);

            TextView title = adView.findViewById(R.id.ad_title);
            if (!TextUtils.isEmpty(mNativeInfo.getTitle())) {
                title.setText(mNativeInfo.getTitle());
            }

            TextView desc = adView.findViewById(R.id.ad_desc);
            if (!TextUtils.isEmpty(mNativeInfo.getDesc())) {
                desc.setText(mNativeInfo.getDesc());
            }

            Button btn = adView.findViewById(R.id.ad_btn);
            if (!TextUtils.isEmpty(mNativeInfo.getCallToActionText())) {
                btn.setText(mNativeInfo.getCallToActionText());
            } else {
                btn.setVisibility(View.GONE);
            }

            MediaView mediaView = adView.findViewById(R.id.ad_media);

            nativeAdView = new NativeAdView(mActivity);

            AdIconView adIconView = adView.findViewById(R.id.ad_icon_media);
            RelativeLayout adDescRl = adView.findViewById(R.id.ad_desc_rl);
            if (mNativeInfo.isTemplate())
            {
                adDescRl.setVisibility(View.GONE);
            }

            //DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
            mediaView.getLayoutParams().height = (int) gameAdH;//(int) (displayMetrics.widthPixels / (1080.0 / 627.0));

            nativeAdView.addView(adView);

            nativeAdView.setTitleView(title);
            nativeAdView.setDescView(desc);
            nativeAdView.setAdIconView(adIconView);
            nativeAdView.setCallToActionView(btn);
            nativeAdView.setMediaView(mediaView);

            nativeAd.registerNativeAdView(nativeAdView);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            adNativeContainer.addView(nativeAdView, layoutParams);
        }
    }
    //endregion

    /****************************************************************************************************/

    /****************************************************************************************************/

    public void showAd(final int type, final String placeId, final int x, final int y) {
        sceneId = placeId;
        if (type == AppMacros.AT_RewardVideo) {
            if (RewardedVideoAd.isReady()) {
                RewardedVideoAd.showAd();
            } else {
                if (mCallBack != null) {
                    mCallBack.onCallBack(type, AppMacros.CALL_FALIED,sceneId,"","","");
                    mCallBack.onAdStatusListen(type, AppMacros.CALL_FALIED, sceneId,"","","");
                }
            }
        } else if (type == AppMacros.AT_Banner_Bottom) {
            showBannerAds();
        } else if (type == AppMacros.AT_Interstitial) {
            if(InterstitialAd.isReady()){
                InterstitialAd.showAd();
            }
            else {
                if (mCallBack != null) {
                    mCallBack.onCallBack(type, AppMacros.CALL_FALIED,sceneId,"","","");
                    mCallBack.onAdStatusListen(type, AppMacros.CALL_FALIED, sceneId,"","","");
                }
            }
        } else if (type == AppMacros.AT_Native) {

            adNativeContainer.setX(0);
            adNativeContainer.setY(y);
            showNative();


        }
    }

    public void closeAd(final int type) {
        if (type == AppMacros.AT_Banner_Bottom){
            adBannerContainer.removeAllViews();
            loadAndShowBanner();
        }
        else if (type == AppMacros.AT_Native){
            adNativeContainer.removeAllViews();
            loadAndShowNative();
        }
    }

    public boolean checkAD(final int type, final String placeId) {
       return false;
    }


    public void onResume() {

    }

    public void onPause() {

    }


    public void showSplashActivity() {
        Intent intent = new Intent(mActivity, SplashActivity.class);
        mActivity.startActivity(intent);
    }

    public void goToMainActivity(Context packageContext){
//        if(mCallBackActivity!=null) {
////            mCallBackActivity.goToMainActivity(packageContext);
////        }
//        Intent intent = new Intent(packageContext, mActivity);
//        mActivity.startActivity(intent);
    }
}
