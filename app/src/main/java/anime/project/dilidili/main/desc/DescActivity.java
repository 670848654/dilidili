package anime.project.dilidili.main.desc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import anime.project.dilidili.R;
import anime.project.dilidili.api.Api;
import anime.project.dilidili.database.DatabaseUtil;
import anime.project.dilidili.adapter.DescAdapter;
import anime.project.dilidili.main.base.BaseActivity;
import anime.project.dilidili.main.base.BaseView;
import anime.project.dilidili.bean.AnimeDescBean;
import anime.project.dilidili.bean.AnimeListBean;
import anime.project.dilidili.config.AnimeType;
import anime.project.dilidili.main.video.VideoPresenter;
import anime.project.dilidili.main.video.VideoUtils;
import anime.project.dilidili.main.video.VideoView;
import anime.project.dilidili.main.player.PlayerActivity;
import anime.project.dilidili.main.webview.WebActivity;
import anime.project.dilidili.util.SharedPreferencesUtils;
import anime.project.dilidili.util.StatusBarUtil;
import anime.project.dilidili.util.Utils;
import butterknife.BindView;
import jp.wasabeef.blurry.Blurry;

public class DescActivity extends BaseActivity implements BaseView,DescView,VideoView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private DescAdapter adapter;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<MultiItemEntity> multiItemList = new ArrayList<>();
    private List<AnimeDescBean> drama = new ArrayList<>();
    @BindView(R.id.title_img)
    ImageView imageView;
    @BindView(R.id.collaps_toolbar_layout)
    CollapsingToolbarLayout ct;
    private String videoTitle;
    private String url, diliUrl;
    private String title_t;
    //dialog
    private ProgressDialog p;
    private AlertDialog alertDialog;
    @BindView(R.id.favorite)
    FloatingActionButton favorite;
    private boolean isFavorite;
    private String[] videoUrlArr;
    private String[] videoTitleArr;
    private DescPresenter presenter;
    private VideoPresenter videoPresenter;
    private AnimeListBean animeListBean = new AnimeListBean();

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_desc;
    }

    @Override
    protected void init() {
        StatusBarUtil.setColorForSwipeBack(DescActivity.this, getResources().getColor(R.color.night), 0);
        // 设置右滑动返回
        Slidr.attach(this, Utils.defaultInit());
        getBundle();
        initToolbar();
        initFab();
        initSwipe();
        initAdapter();
        initViews(mRecyclerView);
        presenter = new DescPresenter(this, url, this, this);
        presenter.loadData(true);
    }

    @Override
    protected void initBeforeView() {

    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            url = bundle.getString("url");
            title_t = bundle.getString("name");
        }
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(DescActivity.this, R.string.loading));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void initFab(){
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isFastClick())
                    favoriteAnime();
            }
        });
    }

    public void initSwipe() {
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                multiItemList.clear();
                adapter.setNewData(multiItemList);
                presenter.loadData(true);
            }
        });
        mSwipe.setRefreshing(true);
    }

    public void initAdapter(){
//        mRecyclerView.setNestedScrollingEnabled(false);
        adapter = new DescAdapter(multiItemList, DescActivity.this);
        adapter.openLoadAnimation();
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (Utils.isFastClick()) {
                    final AnimeDescBean bean = (AnimeDescBean) multiItemList.get(position);
                    switch (bean.getType()) {
                        case "play":
                            p = Utils.getProDialog(DescActivity.this, "解析中,请稍后...");
                            Button v = (Button) adapter.getViewByPosition(mRecyclerView, position, R.id.tag_group);
                            v.setBackground(getResources().getDrawable(R.drawable.button_selected, null));
                            drama.get(position-1).setSelect(true);
                            diliUrl = bean.getUrl();
                            videoTitle = bean.getTitle();
                            videoPresenter = new VideoPresenter(animeListBean.getTitle(), bean.getUrl(),DescActivity.this);
                            videoPresenter.loadData(true);
                            break;
                        case "html":
                            Bundle bundle = new Bundle();
                            if (bean.getUrl().indexOf("http") == -1)
                                bundle.putString("url", Api.URL + bean.getUrl());
                            else
                                bundle.putString("url", bean.getUrl());
                            startActivityForResult(new Intent(DescActivity.this, DescActivity.class).putExtras(bundle), 3000);
                            break;
                        case "down":
                            Utils.viewInBrowser(DescActivity.this, bean.getUrl());
                            break;
                    }

                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (Utils.isFastClick()) {
                    final AnimeDescBean bean = (AnimeDescBean) multiItemList.get(position);
                    switch (bean.getType()) {
                        case "down":
                            Utils.putTextIntoClip(DescActivity.this, bean.getTitle());
                            Toast.makeText(DescActivity.this, bean.getTitle() +"已复制到剪切板!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                return true;
            }
        });
        mRecyclerView.setAdapter(adapter);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.isFirstOnly((Boolean) SharedPreferencesUtils.getParam(DescActivity.this, "anim_is_first", true));//init firstOnly state
    }

    public void goToPlay(String videoUrl){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String[] arr = VideoUtils.removeByIndex(videoUrl.split("http"), 0);
                //如果播放地址只有1个
                if (arr.length == 1){
                    String url = "http"+arr[0];
                    if (url.contains(".m3u8") || url.contains(".mp4")){
                        Bundle bundle = new Bundle();
                        switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(),"player",0)){
                            case 0:
                                //调用播放器
                                bundle.putBoolean("is", false);
                                bundle.putString("title_t", title_t);
                                bundle.putString("title", videoTitle);
                                bundle.putString("url", url);
                                bundle.putString("dili", diliUrl);
                                bundle.putSerializable("list", (Serializable) drama);
                                startActivityForResult(new Intent(DescActivity.this, PlayerActivity.class).putExtras(bundle), 0x10);
                                break;
                            case 1:
                                Utils.selectVideoPlayer(DescActivity.this,url);
                                break;
                        }
                    }else {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("is", false);
                        bundle.putString("title", title_t);
                        bundle.putString("url", url);
                        bundle.putString("dili", diliUrl);
                        bundle.putSerializable("list", (Serializable) drama);
                        startActivityForResult(new Intent(DescActivity.this, WebActivity.class).putExtras(bundle), 0x10);
                    }
                }else {
                    videoUrlArr = new String[arr.length];
                    videoTitleArr = new String[arr.length];
                    for (int i=0;i<arr.length;i++) {
                        String str = "http" + arr[i];
                        Log.e("video",str);
                        videoUrlArr[i] = str;
                        java.net.URL  urlHost;
                        try {
                            urlHost = new  java.net.URL(str);
                            if (str.contains(".mp4"))
                                videoTitleArr[i] = urlHost.getHost() + " <MP4> <播放器>";
                            else if (str.contains(".m3u8"))
                                videoTitleArr[i] = urlHost.getHost() + " <M3U8> <播放器>";
                            else
                                videoTitleArr[i] = urlHost.getHost() + " <HTML>";
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                    selectVideoDialog();
                }
            }
        },200);
    }

    private void selectVideoDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择视频源");
        builder.setCancelable(false);
        builder.setItems(videoTitleArr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if (videoUrlArr[index].contains(".m3u8") || videoUrlArr[index].contains(".mp4")){
                    switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(),"player",0)){
                        case 0:
                            //调用播放器
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("is", false);
                            bundle.putString("title", videoTitle);
                            bundle.putString("title_t", title_t);
                            bundle.putString("url", videoUrlArr[index]);
                            bundle.putString("dili", diliUrl);
                            bundle.putSerializable("list", (Serializable) drama);
                            startActivityForResult(new Intent(DescActivity.this, PlayerActivity.class).putExtras(bundle), 0x10);
                            break;
                        case 1:
                            Utils.selectVideoPlayer(DescActivity.this,videoUrlArr[index]);
                            break;
                    }
                }else {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is", false);
                    bundle.putString("title", title_t);
                    bundle.putString("url", videoUrlArr[index]);
                    bundle.putString("dili", diliUrl);
                    bundle.putSerializable("list", (Serializable) drama);
                    startActivityForResult(new Intent(DescActivity.this, WebActivity.class).putExtras(bundle), 0x10);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200 && requestCode == 3000) {
            setResult(200);
        }else if (resultCode == 0x20 && requestCode == 0x10){
            mSwipe.setRefreshing(true);
            multiItemList = new ArrayList<>();
            adapter.notifyDataSetChanged();
            presenter.loadData(true);
        }
    }

    public void favoriteAnime(){
        setResult(200);
        isFavorite = DatabaseUtil.favorite(animeListBean);
        if (isFavorite)
        {
            Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_white_48dp).into(favorite);
            Utils.showSnackbar(toolbar, Utils.getString(DescActivity.this, R.string.join_ok));
        }else {
            Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_border_white_48dp).into(favorite);
            Utils.showSnackbar(toolbar, Utils.getString(DescActivity.this, R.string.join_error));
        }
    }

    @Override
    public void showLoadingView() {
        showEmptyVIew();
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipe.setRefreshing(false);
                ct.setTitle("加载出错");
                mRecyclerView.setLayoutManager(new LinearLayoutManager(DescActivity.this));
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        mSwipe.setRefreshing(true);
        adapter.setEmptyView(emptyView);
    }

    @Override
    public void showSuccessMainView(List<MultiItemEntity> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final GridLayoutManager manager = new GridLayoutManager(DescActivity.this, 4);
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int index = 0;
                        switch (adapter.getItemViewType(position)){
                            case AnimeType.TYPE_LEVEL_0:
                                index = manager.getSpanCount();
                                break;
                            case AnimeType.TYPE_LEVEL_2:
                                index = 2;
                                break;
                            case AnimeType.TYPE_LEVEL_1:
                                index = 1;
                                break;
                        }
                        return index;
                    }
                });
                // important! setLayoutManager should be called after setAdapter
                mRecyclerView.setLayoutManager(manager);
                multiItemList = list;
                mSwipe.setRefreshing(false);
                Glide.with(DescActivity.this).asBitmap().load(animeListBean.getImg()).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (null == resource)
                            imageView.setImageDrawable(getDrawable(R.drawable.urlerror_w));
                        else
                            Blurry.with(DescActivity.this)
                                    .radius(4)
                                    .sampling(2)
                                    .async()
                                    .from(resource)
                                    .into(imageView);
                    }
                });
                ct.setTitle(animeListBean.getTitle());
                adapter.setNewData(multiItemList);
                adapter.expand(0);
            }
        });
    }

    @Override
    public void showSuccessDramaView(List<AnimeDescBean> list) {
        drama = list;
    }

    @Override
    public void showSuccessDescView(AnimeListBean bean) {
        animeListBean = bean;
    }

    @Override
    public void showSuccessFavorite(boolean is) {
        isFavorite = is;
        runOnUiThread(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
                if (!favorite.isShown()){
                    if (isFavorite)
                        Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_white_48dp).into(favorite);
                    else
                        Glide.with(DescActivity.this).load(R.drawable.baseline_favorite_border_white_48dp).into(favorite);
                    favorite.startAnimation(Utils.animationOut(1));
                    favorite.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void getVideoSuccess(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.cancelProDoalog(p);
                goToPlay(url);
            }
        });
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.cancelProDoalog(p);
                VideoUtils.showErrorInfo(DescActivity.this, diliUrl);
            }
        });
    }

    @Override
    public void getVideoError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.cancelProDoalog(p);
                //网络出错
                Toast.makeText(DescActivity.this, Utils.getString(DescActivity.this, R.string.error_700), Toast.LENGTH_LONG).show();
            }
        });
    }
}
