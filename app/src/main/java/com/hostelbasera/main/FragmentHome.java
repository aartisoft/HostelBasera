package com.hostelbasera.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hostelbasera.R;
import com.hostelbasera.apis.HttpRequestHandler;
import com.hostelbasera.apis.PostRequest;
import com.hostelbasera.model.GetPropertyDetailModel;
import com.hostelbasera.utility.Constant;
import com.hostelbasera.utility.Globals;
import com.hostelbasera.utility.PaginationProgressBarAdapter;
import com.hostelbasera.utility.Toaster;
import com.paginate.Paginate;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentHome extends Fragment implements Paginate.Callbacks, SwipeRefreshLayout.OnRefreshListener {

    Globals globals;
    View view;
    @BindView(R.id.tv_search)
    TextView tvSearch;
    @BindView(R.id.tv_girls)
    TextView tvGirls;
    @BindView(R.id.tv_boys)
    TextView tvBoys;
    @BindView(R.id.t_both)
    TextView tBoth;
    @BindView(R.id.tv_near_me)
    TextView tvNearMe;
    @BindView(R.id.tv_hostel_suggestion)
    TextView tvHostelSuggestion;
    @BindView(R.id.rv_hostel)
    RecyclerView rvHostel;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.rotate_loading)
    RotateLoading rotateLoading;
    @BindView(R.id.img_icon)
    ImageView imgIcon;
    @BindView(R.id.fl_rotate_loading)
    FrameLayout flRotateLoading;
    @BindView(R.id.tv_no_data_found)
    TextView tvNoDataFound;
    @BindView(R.id.ll_hostel)
    LinearLayout llHostel;

    private Paginate paginate;
    private boolean loading = false;

    //   DashboardPagerAdapter mDashboardPagerAdapter;
    int currentPage = 0;
    int pageNo = 1;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.

    GetPropertyDetailModel getPropertyDetailModel;

    ArrayList<GetPropertyDetailModel.PropertyDetail> arrPropertyDetailArrayList;

    AdapterHomePropertyDetail adapterHomePropertyDetail;
    DashboardActivity activity;

    public static FragmentHome newInstance(/*AllCategoriesDetailModel model*/) {
        FragmentHome fragment = new FragmentHome();
//        fragment.allCategoriesModel = model;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @OnClick(R.id.tv_search)
    public void onTvSearchClicked() {
    }

    @OnClick(R.id.tv_girls)
    public void onTvGirlsClicked() {
    }

    @OnClick(R.id.tv_boys)
    public void onTvBoysClicked() {
    }

    @OnClick(R.id.t_both)
    public void onTBothClicked() {
    }

    @OnClick(R.id.tv_near_me)
    public void onTvNearMeClicked() {
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        Globals.hideKeyboard(getActivity());
        globals = ((Globals) getActivity().getApplicationContext());
        activity = (DashboardActivity) getActivity();
        progressBar.setVisibility(View.GONE);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this);

//        searchView.setQuery("", false);
//        searchView.setIconified(false);
//        searchView.clearFocus();
        tvHostelSuggestion.setTypeface(tvHostelSuggestion.getTypeface(), Typeface.BOLD);
        arrPropertyDetailArrayList = new ArrayList<>();
        tvNoDataFound.setText("");

        if (Globals.isNetworkAvailable(getActivity())) {
//            vpBanner.setVisibility(View.GONE);
//            getSliderImages();
            getPropertyListData(true);
        } else {
            showNoRecordFound(getString(R.string.no_data_found));
            Toaster.shortToast(R.string.no_internet_msg);
        }
    }

    @OnClick(R.id.tv_search)
    public void onSearchClick() {
        //TODO : Remove comment once Search Activity ready
//        startActivity(new Intent(getActivity(), SearchActivity.class));
        Toaster.shortToast("Search");
    }


    private void showNoRecordFound(String no_data_found) {
        loading = false;
        llHostel.setVisibility(View.GONE);
        if (tvNoDataFound.getVisibility() == View.GONE) {
            tvNoDataFound.setVisibility(View.VISIBLE);
            tvNoDataFound.setText(no_data_found);
        }
    }

    private void hideNoRecordFound() {
        llHostel.setVisibility(View.VISIBLE);
        if (tvNoDataFound.getVisibility() == View.VISIBLE)
            tvNoDataFound.setVisibility(View.GONE);
    }


    public void stopLoader() {
        rotateLoading.stop();
        rotateLoading.setVisibility(View.GONE);
        flRotateLoading.setVisibility(View.GONE);
    }

    public void startLoader() {
        flRotateLoading.setVisibility(View.VISIBLE);
        rotateLoading.setVisibility(View.VISIBLE);
        rotateLoading.start();
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(10000);
        rotate.setInterpolator(new LinearInterpolator());

        rotate.setRepeatCount(Animation.INFINITE);
        imgIcon.startAnimation(rotate);
    }

    public void getPropertyListData(boolean showProgress) {
        JSONObject postData = HttpRequestHandler.getInstance().getPropertyListDataParam(pageNo, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        if (postData != null) {
            if (!swipeRefreshLayout.isRefreshing() && showProgress)
                progressBar.setVisibility(View.VISIBLE);
//            startLoader();

            new PostRequest(getActivity(), getString(R.string.getPropertyList),
                    postData, false, new PostRequest.OnPostServiceCallListener() {
                @Override
                public void onSucceedToPostCall(JSONObject response) {
                    if (progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);
//                    stopLoader();
                    getPropertyDetailModel = new Gson().fromJson(response.toString(), GetPropertyDetailModel.class);
                    if (getPropertyDetailModel.propertyDetail != null && !getPropertyDetailModel.propertyDetail.isEmpty()) {
                        if (swipeRefreshLayout.isRefreshing()) {
                            stopRefreshing();
                            rvHostel.setAdapter(null);
                            arrPropertyDetailArrayList.clear();
                            adapterHomePropertyDetail.notifyDataSetChanged();
                        }
                        setupList(getPropertyDetailModel.propertyDetail);
                    } else {
                        stopRefreshing();
                        if (pageNo == 1) {
                            showNoRecordFound("");
                            Toaster.shortToast(getPropertyDetailModel.message);
                        }
                    }
                }

                @Override
                public void onFailedToPostCall(int statusCode, String msg) {
                    if (progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);
                    if (paginate != null)
                        paginate.unbind();
                    Toaster.shortToast(msg);
                }
            }).execute();
        }
        Globals.hideKeyboard(getActivity());
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setupList(ArrayList<GetPropertyDetailModel.PropertyDetail> homePageStoresDetailArrayList) {
        if (homePageStoresDetailArrayList != null && !homePageStoresDetailArrayList.isEmpty()) {
            arrPropertyDetailArrayList.addAll(homePageStoresDetailArrayList);
            setAdapter();
        } else
            showNoRecordFound(getString(R.string.no_data_found));
    }

    private void setAdapter() {
        hideNoRecordFound();
        if (adapterHomePropertyDetail == null) {
            if (paginate != null) {
                paginate.unbind();
            }
            adapterHomePropertyDetail = new AdapterHomePropertyDetail(getActivity());
        }
        loading = false;
        adapterHomePropertyDetail.doRefresh(arrPropertyDetailArrayList);

        if (rvHostel.getAdapter() == null) {
            rvHostel.setHasFixedSize(false);
            rvHostel.setLayoutManager(new GridLayoutManager(getContext(), Constant.GRID_SPAN));
            rvHostel.setItemAnimator(new DefaultItemAnimator());
            rvHostel.setAdapter(adapterHomePropertyDetail);
            if (arrPropertyDetailArrayList.size() < getPropertyDetailModel.total_properties && rvHostel != null) {
                paginate = Paginate.with(rvHostel, this)
                        .setLoadingTriggerThreshold(Constant.progress_threshold_2)
                        .addLoadingListItem(Constant.addLoadingRow)
                        .setLoadingListItemCreator(new PaginationProgressBarAdapter())
                        .setLoadingListItemSpanSizeLookup(() -> Constant.GRID_SPAN)
                        .build();
            }
        }

        adapterHomePropertyDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), HostelDetailActivity.class).putExtra(Constant.Property_id, arrPropertyDetailArrayList.get(position).property_id));
            }
        });
    }

    @Override
    public void onRefresh() {
        if (Globals.isNetworkAvailable(getActivity())) {
            pageNo = 1;
            getPropertyListData(true);
        } else {
            Toaster.shortToast(R.string.no_internet_msg);
        }
    }

    @Override
    public void onLoadMore() {
        loading = true;
        pageNo++;
        getPropertyListData(false);
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return arrPropertyDetailArrayList.size() >= getPropertyDetailModel.total_properties;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

