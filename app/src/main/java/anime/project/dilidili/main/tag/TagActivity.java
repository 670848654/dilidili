package anime.project.dilidili.main.tag;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import anime.project.dilidili.R;
import anime.project.dilidili.adapter.TagAdapter;
import anime.project.dilidili.main.animelist.AnimeListActivity;
import anime.project.dilidili.main.base.BaseActivity;
import anime.project.dilidili.main.base.BaseView;
import anime.project.dilidili.bean.HomeBean;
import anime.project.dilidili.util.StatusBarUtil;
import anime.project.dilidili.util.Utils;
import butterknife.BindView;

public class TagActivity extends BaseActivity implements BaseView,TagView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    private TagAdapter adapter;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<MultiItemEntity> tagList = new ArrayList<>();
    private TagPresenter presenter;

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_anime;
    }

    @Override
    protected void init() {
        StatusBarUtil.setColorForSwipeBack(this, getResources().getColor(R.color.night), 0);
        // 设置右滑动返回
        Slidr.attach(this, Utils.defaultInit());
        initViews(mRecyclerView);
        initToolbar();
        initSwipe();
        initAdapter();
        presenter = new TagPresenter(this, this);
        presenter.loadData(true);
    }

    @Override
    protected void initBeforeView() {

    }

    public void initToolbar(){
        toolbar.setTitle(Utils.getString(TagActivity.this, R.string.tag_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void initSwipe(){
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onRefresh() {
                tagList.clear();
                adapter.setNewData(tagList);
                presenter.loadData(true);
            }
        });
    }

    public void initAdapter(){
        adapter = new TagAdapter(tagList);
        adapter.openLoadAnimation();
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (Utils.isFastClick()) {
                    final HomeBean bean = (HomeBean) tagList.get(position);
                    String title = "";
                    String reg = "([0-9])";
                    Pattern p = Pattern.compile(reg);
                    Matcher m = p.matcher(bean.getDesc());
                    while (m.find()) {
                        title += m.group();
                    }
                    if (!title.isEmpty())
                        title += "年";
                    Bundle bundle = new Bundle();
                    bundle.putString("title", title + bean.getTitle());
                    bundle.putString("url", bean.getUrl());
                    startActivity(new Intent(TagActivity.this, AnimeListActivity.class).putExtras(bundle));
                }
            }
        });
        mRecyclerView.setAdapter(adapter);
        final GridLayoutManager manager = new GridLayoutManager(this, 4);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == TagAdapter.TYPE_LEVEL_1 ? 1 : manager.getSpanCount();
            }
        });
        // important! setLayoutManager should be called after setAdapter
        mRecyclerView.setLayoutManager(manager);
    }

    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
    }

    @Override
    public void showSuccessView(List<MultiItemEntity> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipe.setRefreshing(false);
                tagList = list;
                adapter.setNewData(tagList);
            }
        });
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipe.setRefreshing(false);
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        adapter.setEmptyView(emptyView);
    }
}
