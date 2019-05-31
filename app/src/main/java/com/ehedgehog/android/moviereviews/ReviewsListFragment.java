package com.ehedgehog.android.moviereviews;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ehedgehog.android.moviereviews.model.MovieReview;
import com.ehedgehog.android.moviereviews.network.ApiFactory;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class ReviewsListFragment extends Fragment {

    private static final String TAG = "ReviewsListFragment";
    private static final int ITEMS_PER_PAGE = 20;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
//    private Spinner mCategorySpinner;

    private ReviewsPresenter mPresenter;

    //    private String mCategory;
    private int mCurrentPage;
    private boolean isLoading;

    private Disposable mStoriesSubscription;

    public static ReviewsListFragment newInstance() {
        return new ReviewsListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        isOnline();

        mCurrentPage = 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews_list, container, false);

        setupStoriesPresenter();

        mRefreshLayout = view.findViewById(R.id.reviews_list_refresh_container);
        setupSwipeRefresh();

//        mCategorySpinner = view.findViewById(R.id.stories_category_spinner);
//        setupCategorySpinner();

        mProgressBar = view.findViewById(R.id.reviews_progress_bar);
        mRecyclerView = view.findViewById(R.id.reviews_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemsCount = layoutManager.getChildCount();
                int invisibleItemsCount = layoutManager.findFirstVisibleItemPosition();
                int totalItemsCount = layoutManager.getItemCount();
                if ((visibleItemsCount + invisibleItemsCount) >= totalItemsCount) {
                    if ((mCurrentPage < 1000) && !isLoading) {
                        Log.i(TAG, "Loading new data...");
                        ++mCurrentPage;
                        loadStories(mCurrentPage);
                    }
                }
            }
        });

        loadStories(mCurrentPage);

        return view;
    }

    @Override
    public void onPause() {
        if (mStoriesSubscription != null)
            mStoriesSubscription.dispose();

        super.onPause();
    }

    private void loadStories(int currentPage) {
        isLoading = true;
        mStoriesSubscription = mPresenter
                .loadStories(getActivity(), currentPage*ITEMS_PER_PAGE);
    }

    private void setupStoriesPresenter() {
        mPresenter = new ReviewsPresenter(ApiFactory.buildReviewsService());
        mPresenter.addListener(new ReviewsPresenter.Listener() {
            @Override
            public void onStoriesLoaded(List<MovieReview> movieReviews) {
                updateUI(movieReviews);
            }

            @Override
            public void onLoadingError(String message) {
                Log.e(TAG, message);
            }
        });
    }

    private void updateUI(List<MovieReview> movieReviews) {
        ReviewsAdapter adapter = (ReviewsAdapter) mRecyclerView.getAdapter();
        if (adapter == null) {
            adapter = new ReviewsAdapter(getActivity(), movieReviews);
            mRecyclerView.setAdapter(adapter);
        } else {
            if (mCurrentPage == 0) {
                adapter.setMovieReviews(movieReviews);
                adapter.notifyDataSetChanged();
            } else {
                adapter.addAll(movieReviews);
                adapter.notifyItemRangeInserted(
                        mCurrentPage * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
            }
        }

        isLoading = false;
        mProgressBar.setVisibility(View.GONE);
    }

    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info != null && info.isConnected())
            return true;

        Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setupSwipeRefresh() {
        mRefreshLayout.setColorSchemeResources(
                android.R.color.black,
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_dark
        );
        mRefreshLayout.setOnRefreshListener(() -> {
            mRefreshLayout.setRefreshing(true);
            if (isOnline()) {
                mCurrentPage = 0;
                loadStories(mCurrentPage);
            }
            mRefreshLayout.setRefreshing(false);
        });
    }

//    private void setupCategorySpinner() {
//        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.categories_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mCategorySpinner.setAdapter(adapter);
//
//        String[] categories = getActivity()
//                .getResources().getStringArray(R.array.categories_array);
//        mCategory = categories[StoriesPreferences.getStoredCategory(getActivity())];
//
//        mCategorySpinner.setSelection(StoriesPreferences.getStoredCategory(getActivity()));
//        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mCategory = categories[position];
//                StoriesPreferences.setStoredCategory(getActivity(), position);
//                mProgressBar.setVisibility(View.VISIBLE);
//
//                loadStories(mCategory);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }

}
