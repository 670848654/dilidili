package anime.project.dilidili.util;

import android.Manifest;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import anime.project.dilidili.BuildConfig;
import anime.project.dilidili.R;
import anime.project.dilidili.application.DiliDili;
import anime.project.dilidili.custom.CircleImageView;

public class Utils {
    private final static Pattern IMAGE_PATTERN = Pattern.compile("http://(.*jpg|.*jpeg|.*png)");
    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }

    public static void creatFile() {
        File dataDir = new File(Environment.getExternalStorageDirectory() + "/DiliDiliAnime/Database");
        if (!dataDir.exists())
            dataDir.mkdirs();
    }

    // 两次点击按钮之间的点击间隔不能少于500毫秒
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    /**
     * 加载框
     * @return
     */
    public static ProgressDialog getProDialog(Context context, @StringRes int id) {
        ProgressDialog p = new ProgressDialog(context);
        p.setMessage(getString(id));
        p.setCancelable(false);
        p.show();
        return p;
    }

    /**
     * 下载进度条
     * @param context
     * @return
     */
    public static ProgressDialog showProgressDialog(Context context){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        pd.setCancelable(false);// 设置是否可以通过点击Back键取消
        pd.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        pd.setMax(100);
        pd.setMessage(getString(R.string.download));
        return pd;
    }

    /**
     * 关闭加载框
     *
     * @param p
     */
    public static void cancelProDialog(ProgressDialog p) {
        if (p != null)
            p.dismiss();
    }

    /**
     * 选择视频播放器
     *
     * @param url
     */
    public static void selectVideoPlayer(Context context, String url) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.setDataAndType(Uri.parse(url), "video/*");
        // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
        // 官方解释 : Name of the component implementing an activity that can display the intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, "请选择视频播放器"));
        } else {
            DiliDili.getInstance().showToastMsg("没有找到匹配的程序");
        }
    }

    /**
     * 通过浏览器打开
     *
     * @param url
     */
    public static void viewInBrowser(Context context, String url) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, "请通过浏览器打开"));
        } else {
            DiliDili.getInstance().showToastMsg("没有找到匹配的程序");
        }
    }

    public static void viewInChrome(Context context, String url){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        //Sets the toolbar color.
        builder.setToolbarColor(context.getResources().getColor(R.color.night));
        Bitmap closeBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.baseline_close_white_48dp);
        builder.setCloseButtonIcon(closeBitmap);// 关闭按钮
        builder.setShowTitle(true); //显示网页标题
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    public static String getString(@StringRes int id) {
        return getContext().getResources().getString(id);
    }

    public static String[] getArray(@ArrayRes int id){
        return getContext().getResources().getStringArray(id);
    }

    /**
     * 获取当前日期是星期几
     *
     * @param dt
     * @return + 1 当前日期是星期几
     */
    public static int getWeekOfDate(Date dt) {
        int[] weekDays = {6, 0, 1, 2, 3, 4, 5};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * info
     */
    public static void showX5Info(Context context) {
        AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setPositiveButton(getString(R.string.x5_info_positive), null);
        builder.setMessage(getString(R.string.x5_info));
        builder.setTitle(getString(R.string.x5_info_title));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            SharedPreferencesUtils.setParam(getContext(), "show_x5_info", false);
            alertDialog.dismiss();
        });
    }


    public static void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public static ObjectAnimator tada(View view) {
        return tada(view, 2f);
    }

    public static ObjectAnimator tada(View view, float shakeFactor) {

        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(.1f, -3f * shakeFactor),
                Keyframe.ofFloat(.2f, -3f * shakeFactor),
                Keyframe.ofFloat(.3f, 3f * shakeFactor),
                Keyframe.ofFloat(.4f, -3f * shakeFactor),
                Keyframe.ofFloat(.5f, 3f * shakeFactor),
                Keyframe.ofFloat(.6f, -3f * shakeFactor),
                Keyframe.ofFloat(.7f, 3f * shakeFactor),
                Keyframe.ofFloat(.8f, -3f * shakeFactor),
                Keyframe.ofFloat(.9f, 3f * shakeFactor),
                Keyframe.ofFloat(1f, 0)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhRotate).
                setDuration(1000);
    }

    public static ObjectAnimator nope(View view) {
        int delta = view.getResources().getDimensionPixelOffset(R.dimen.spacing_medium);
        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -delta),
                Keyframe.ofFloat(.26f, delta),
                Keyframe.ofFloat(.42f, -delta),
                Keyframe.ofFloat(.58f, delta),
                Keyframe.ofFloat(.74f, -delta),
                Keyframe.ofFloat(.90f, delta),
                Keyframe.ofFloat(0f, 0)
        );
        return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).
                setDuration(500);
    }

    /**
     * 隐藏动画&显示动画
     * type 0 隐藏  1 显示
     *
     * @return
     */
    public static ScaleAnimation animationOut(int type) {
        ScaleAnimation scaleAnimation = null;
        switch (type) {
            case 0:
                scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                break;
            case 1:
                scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                break;
        }
        return scaleAnimation;
    }

    /**
     * 复制提取码到剪切板
     */
    public static void putTextIntoClip(String string) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象
        ClipData clipData = ClipData.newPlainText("string", string);
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData);
    }

    /**
     * 滑动返回配置
     * @return
     */
    public static SlidrConfig defaultInit() {
        SlidrConfig.Builder mBuilder;
        mBuilder = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(5f)
                .distanceThreshold(.35f)
                .edge(true | false)
                .edgeSize(0.18f);// The % of the screen that counts as the edge, default 18%;
        return mBuilder.build();
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad() {
        return (getContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 获取图片url
     * @return
     */
    public static String getImageUrl(String style){
        String url = "";
        Matcher m = IMAGE_PATTERN.matcher(style);
        while (m.find()) {
            url = m.group();
            break;
        }
        return url;
    }

    /**
     * 设置默认图片
     * @param url
     */
    public static void setDefaultImage(Context context, String url, ImageView imageView){
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true).build();
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error);
        Glide.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .apply(options)
                .into(imageView);
    }

    /**
     * 设置圆形图片
     * @param context
     * @param url
     * @param imageView
     */
    public static void setCircleImage(Context context, String url, CircleImageView imageView){
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .dontAnimate()//防止设置placeholder导致第一次不显示网络图片,只显示默认图片的问题
                .placeholder(R.drawable.loading)
                .error(R.drawable.error);
        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * 设置Palette
     * @param context
     * @param url
     * @param cardView
     * @param textView
     */
    public static void setCardBg(Context context, String url, CardView cardView, TextView textView) {
        Glide.with(context).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Palette.from(resource).generate(palette -> {
                    Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                    if (swatch != null) {
                        cardView.setCardBackgroundColor(swatch.getRgb());
                        textView.setTextColor(swatch.getBodyTextColor());
                    }
                });
            }
        });
    }

    public static void setCardDefaultBg(Context context, CardView cardView, TextView textView) {
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.window_bg));
        textView.setTextColor(context.getResources().getColor(R.color.text_color_primary));
    }

    public static String getASVersionName(){
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 删除文件
     * @param root
     */
    public static void deleteAllFiles(File root) {
        Log.e("删除文件",root.toString());
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }

    /**
     * 安装应用
     * @param activity
     */
    public static void startInstall(Activity activity) {
        //权限不存在，申请权限，并跳到当前包
        if(!isGranted(activity,Manifest.permission.REQUEST_INSTALL_PACKAGES)){
            Uri packageURI = Uri.parse("package:" + activity.getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
            activity.startActivityForResult(intent, 10001);
        }else {
            install(activity);
        }
    }

    /**
     * 安装应用
     * @param activity
     */
    private static void install(Activity activity){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //android 7.0权限问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", new File(Environment.getExternalStorageDirectory(), "base.apk"));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "base.apk")), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    /**
     * 判断是否为Android O+
     * @param activity
     * @param permission
     * @return
     */
    private static boolean isGranted(Activity activity,String permission) {
        // 8.0 权限 安装apk 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return activity.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    public static void findNewVersion(Context context,
                                       String version,
                                       String body,
                                       DialogInterface.OnClickListener posListener,
                                       DialogInterface.OnClickListener negListener) {
        AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setMessage(body);
        builder.setTitle("发现新版本 " + version);
        builder.setPositiveButton("马上更新", posListener);
        builder.setNegativeButton("暂不更新", negListener);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 判断是否有NavigationBar
     *
     * @param activity
     * @return
     */
    public static boolean checkHasNavigationBar(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 获得NavigationBar的高度
     */
    public static int getNavigationBarHeight(Activity activity) {
        int result = 0;
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && checkHasNavigationBar(activity)) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result + 5;
    }

    /**
     * dp转px
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    /**
     * X5内核加载状态
     * @return
     */
    public static boolean getX5State() {
        return (boolean) SharedPreferencesUtils.getParam(getContext(), "X5State", false);
    }

    /**
     * 是否启用x5内核
     * @return
     */
    public static boolean loadX5() {
        return (boolean) SharedPreferencesUtils.getParam(getContext(), "loadX5", false);
    }

    public static void deleteDataBase() {
        File dataBase = new File(Environment.getExternalStorageDirectory() + "/DiliDiliAnime/Database/dilidili.db");
        if (dataBase.exists())
            dataBase.delete();
    }

    /**
     * 新版本更新信息
     * @param context
     */
    public static void showNewVersionInfo(Context context) {
        AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setPositiveButton(getString(R.string.x5_info_positive), null);
        builder.setMessage(getString(R.string.nwe_version_info));
        builder.setTitle(getString(R.string.x5_info_title));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            SharedPreferencesUtils.setParam(getContext(), "v2.3.6", true);
            alertDialog.dismiss();
        });
    }
}
