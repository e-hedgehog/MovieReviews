package com.ehedgehog.android.moviereviews.network;

import com.ehedgehog.android.moviereviews.model.ReviewsResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReviewsService {

    @GET("all.json")
    Observable<ReviewsResponse> getMovieReviews(@Query("offset") int offset);

}
