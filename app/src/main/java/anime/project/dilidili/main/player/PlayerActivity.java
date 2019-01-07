package anime.project.dilidili.main.player;

import android.app.PictureInPictureParams;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import anime.project.dilidili.R;
import anime.project.dilidili.adapter.DramaAdapter;
import anime.project.dilidili.api.Api;
import anime.project.dilidili.bean.AnimeDescBean;
import anime.project.dilidili.main.base.BaseActivity;
import anime.project.dilidili.main.base.Presenter;
import anime.project.dilidili.main.video.VideoContract;
import anime.project.dilidili.main.video.VideoPresenter;
import anime.project.dilidili.main.webview.WebActivity;
import anime.project.dilidili.util.SharedPreferencesUtils;
import anime.project.dilidili.util.StatusBarUtil;
import anime.project.dilidili.util.Utils;
import anime.project.dilidili.util.VideoUtils;
import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class PlayerActivity extends BaseActivity implements VideoContract.View {
    @BindView(R.id.player)
    JzvdStd player;
    private String witchTitle, url, diliUrl;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private List<AnimeDescBean> list = new ArrayList<>();
    private DramaAdapter dramaAdapter;
    private ProgressDialog p;
    private String animeTitle;
    private String[] videoUrlArr;
    private String[] videoTitleArr;
    @BindView(R.id.nav_view)
    LinearLayout linearLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.anime_title)
    TextView titleView;
    @BindView(R.id.pic)
    ImageView pic;
    private VideoPresenter presenter;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {}

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_play;
    }

    @Override
    protected void init() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        hideGap();
        Bundle bundle = getIntent().getExtras();
        init(bundle);
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    private void init(Bundle bundle) {
        //播放地址
        url = bundle.getString("url");
        //集数名称
        witchTitle = bundle.getString("title");
        //番剧名称
        animeTitle = bundle.getString("animeTitle");
        titleView.setText(animeTitle);
        //源地址
        diliUrl = bundle.getString("dili");
        //剧集list
        list = (List<AnimeDescBean>) bundle.getSerializable("list");
        //禁止冒泡
        linearLayout.setOnClickListener(view -> {
            return;
        });
        linearLayout.getBackground().mutate().setAlpha(150);//0~255透明度值
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        player.backButton.setOnClickListener(v -> {
            Jzvd.releaseAllVideos();
            finish();
        });
        if (Utils.isPad()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                pic.setVisibility(View.GONE);
            else
                pic.setVisibility(View.VISIBLE);
        } else
            pic.setVisibility(View.GONE);
        player.setUp(url, witchTitle, Jzvd.SCREEN_WINDOW_FULLSCREEN);
        Glide.with(PlayerActivity.this).load(R.drawable.baseline_view_module_white_48dp).into(player.fullscreenButton);
        player.fullscreenButton.setOnClickListener(view -> {
            if (Utils.isFastClick())
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else
                    drawerLayout.openDrawer(GravityCompat.END);
        });
        Glide.with(PlayerActivity.this).load(R.drawable.baseline_arrow_back_white_24dp).apply(new RequestOptions().centerCrop()).into(player.backButton);
        player.backButton.setPadding(0, 0, 15, 0);
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        player.startButton.performClick();
        player.startVideo();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @OnClick(R.id.pic)
    public void startPic() {
        drawerLayout.closeDrawer(GravityCompat.END);
        enterPicInPic();
    }

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        dramaAdapter = new DramaAdapter(this, list);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (Utils.isFastClick()) {
                setResult(0x20);
                drawerLayout.closeDrawer(GravityCompat.END);
                final AnimeDescBean bean = (AnimeDescBean) adapter.getItem(position);
                switch (bean.getType()) {
                    case "play":
                        p = Utils.getProDialog(PlayerActivity.this, R.string.parsing);
                        Button v = (Button) adapter.getViewByPosition(recyclerView, position, R.id.tag_group);
                        v.setBackgroundResource(R.drawable.button_selected);
                        diliUrl = bean.getUrl().startsWith("http") ? bean.getUrl() : Api.URL + bean.getUrl();
                        witchTitle = animeTitle + " - " + bean.getTitle();
                        presenter = new VideoPresenter(animeTitle, diliUrl, PlayerActivity.this);
                        presenter.loadData(true);
                        break;
                }

            }
        });
    }

    public void goToPlay(String videoUrl) {
        new Handler().postDelayed(() -> {
            String[] arr = VideoUtils.removeByIndex(videoUrl.split("http"), 0);
            //如果播放地址只有1个
            if (arr.length == 1) {
                url = "http" + arr[0];
                if (url.contains(".m3u8") || url.contains(".mp4")) {
                    switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
                        case 0:
                            //调用播放器
                            Jzvd.releaseAllVideos();
                            player.setUp(url, witchTitle, Jzvd.SCREEN_WINDOW_FULLSCREEN);
                            Glide.with(PlayerActivity.this).load(R.drawable.baseline_view_module_white_48dp).into(player.fullscreenButton);
                            player.startVideo();
                            break;
                        case 1:
                            Jzvd.releaseAllVideos();
                            Utils.selectVideoPlayer(PlayerActivity.this, url);
                            break;
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", animeTitle);
                    bundle.putString("url", url);
                    bundle.putString("dili", diliUrl);
                    bundle.putSerializable("list", (Serializable) list);
                    startActivity(new Intent(PlayerActivity.this, WebActivity.class).putExtras(bundle));
                    PlayerActivity.this.finish();
                }
            } else {
                videoUrlArr = new String[arr.length];
                videoTitleArr = new String[arr.length];
                VideoUtils.showMultipleVideoSources(this,
                        arr,
                        videoTitleArr,
                        videoUrlArr,
                        (dialog, index) -> {
                            url = videoUrlArr[index];
                            if (url.contains(".m3u8") || url.contains(".mp4")) {
                                switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
                                    case 0:
                                        //调用播放器
                                        Jzvd.releaseAllVideos();
                                        player.setUp(url, witchTitle, Jzvd.SCREEN_WINDOW_FULLSCREEN);
                                        Glide.with(PlayerActivity.this).load(R.drawable.baseline_view_module_white_48dp).into(player.fullscreenButton);
                                        player.startVideo();
                                        break;
                                    case 1:
                                        Jzvd.releaseAllVideos();
                                        Utils.selectVideoPlayer(PlayerActivity.this, videoUrlArr[index]);
                                        break;
                                }
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString("title", animeTitle);
                                bundle.putString("url", url);
                                bundle.putString("dili", diliUrl);
                                bundle.putSerializable("list", (Serializable) list);
                                startActivity(new Intent(PlayerActivity.this, WebActivity.class).putExtras(bundle));
                                PlayerActivity.this.finish();
                            }
                        });
            }
        }, 200);
    }

    @OnClick({R.id.select_player, R.id.open_in_browser})
    public void onClick(TextView view) {
        switch (view.getId()) {
            case R.id.select_player:
                Utils.selectVideoPlayer(this, url);
                break;
            case R.id.open_in_browser:
                Utils.viewInBrowser(this, diliUrl);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        else {
            Jzvd.releaseAllVideos();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!PlayerActivity.this.isInMultiWindowMode())
                JzvdStd.goOnPlayOnPause();
        } else
            JzvdStd.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!PlayerActivity.this.isInMultiWindowMode())
                JzvdStd.goOnPlayOnResume();
        } else
            JzvdStd.goOnPlayOnResume();
    }

    /**
     * Android 8.0 画中画
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPicInPic() {
//        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        // 设置宽高比例值，第一个参数表示分子，第二个参数表示分母
        // 下面的10/5=2，表示画中画窗口的宽度是高度的两倍
//        Rational aspectRatio = new Rational(10,5);
        // 设置画中画窗口的宽高比例
//        builder.setAspectRatio(aspectRatio);
        // 进入画中画模式，注意enterPictureInPictureMode是Android8.0之后新增的方法
//        enterPictureInPictureMode(builder.build());
        PictureInPictureParams builder = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(builder);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            player.fullscreenButton.setVisibility(View.INVISIBLE);
            player.backButton.setVisibility(View.INVISIBLE);
            player.titleTextView.setVisibility(View.INVISIBLE);
            player.batteryTimeLayout.setVisibility(View.INVISIBLE);
        } else {
            player.fullscreenButton.setVisibility(View.VISIBLE);
            player.backButton.setVisibility(View.VISIBLE);
            player.titleTextView.setVisibility(View.VISIBLE);
            player.batteryTimeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void cancelDialog() {
        Utils.cancelProDialog(p);
    }

    @Override
    public void getVideoSuccess(String url) {
        runOnUiThread(() -> goToPlay(url));
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> VideoUtils.showErrorInfo(PlayerActivity.this, diliUrl));
    }

    @Override
    public void getVideoError() {
        //网络出错
        runOnUiThread(() -> application.showToastMsg(Utils.getString(R.string.error_700)));
    }

    @Override
    public void showSuccessDramaView(List<AnimeDescBean> dramaList) {
        list = dramaList;
        runOnUiThread(() -> dramaAdapter.setNewData(list));

    }

    @Override
    public void errorDramaView() {
        runOnUiThread(() -> application.showToastMsg("获取剧集信息出错"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != presenter)
            presenter.detachView();
    }
}
