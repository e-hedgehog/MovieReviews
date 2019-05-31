package com.ehedgehog.android.moviereviews;

import android.content.Context;
import android.util.Log;

import com.ehedgehog.android.moviereviews.model.MovieReview;
import com.ehedgehog.android.moviereviews.model.ReviewsResponse;
import com.ehedgehog.android.moviereviews.network.ReviewsService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class ReviewsPresenter {

    private static final String TAG = "ReviewsPresenter";

    public interface Listener {
        void onStoriesLoaded(List<MovieReview> movieReviews);

        void onLoadingError(String message);
    }

    private Listener mListener;
    private final ReviewsService mReviewsService;

    public ReviewsPresenter(ReviewsService service) {
        mReviewsService = service;
    }

    public void addListener(Listener listener) {
        mListener = listener;
    }

    public Disposable loadStories(Context context, int offset) {
        return getAllStories(offset)
                .map(reviewsResponse -> {
                    Log.i(TAG, "Status = " + reviewsResponse.getStatus());
                    return reviewsResponse.getMovieReviews();
                })
                .flatMap(articles -> {
                    Realm.init(context);
                    Realm.getDefaultInstance().executeTransaction(realm -> {
                        if (offset == 0)
                            realm.delete(MovieReview.class);
                        realm.insert(articles);
                    });
                    return Observable.just(articles);
                })
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Something is wrong", throwable);
                    Realm.init(context);
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<MovieReview> results = realm.where(MovieReview.class).findAll();
                    return Observable.just(realm.copyFromRealm(results));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mListener::onStoriesLoaded, throwable ->
                        mListener.onLoadingError(throwable.getMessage()));
    }

    public Observable<ReviewsResponse> getAllStories(int offset) {
        Log.i(TAG, "offset = " + offset);
        return mReviewsService.getMovieReviews(offset);
    }
}
