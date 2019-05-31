package com.ehedgehog.android.moviereviews;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ehedgehog.android.moviereviews.model.MovieReview;

public class ReviewDetailsActivity extends SingleFragmentActivity {

    private static final String EXTRA_REVIEW = "com.ehedgehog.android.moviereviews.review";

    public static Intent newIntent(Context context, MovieReview review) {
        Intent intent = new Intent(context, ReviewDetailsActivity.class);
        intent.putExtra(EXTRA_REVIEW, review);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        MovieReview review = (MovieReview) getIntent().getSerializableExtra(EXTRA_REVIEW);
        return ReviewDetailsFragment.newInstance(review);
    }
}
