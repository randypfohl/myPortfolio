package com.education.pfohl.popularmovies2.MovieList;

import android.content.Intent;
import android.database.ContentObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.education.pfohl.popularmovies2.MovieDetails.DetailActivity;
import com.education.pfohl.popularmovies2.NetworkUtils;
import com.education.pfohl.popularmovies2.R;
import com.education.pfohl.popularmovies2.Repository.MovieRepoContract;
import com.education.pfohl.popularmovies2.Repository.Repository;
import com.education.pfohl.popularmovies2.models.Movie;
import com.education.pfohl.popularmovies2.models.MoviePage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieListActivity extends AppCompatActivity {

    private MovieImageAdapter movieAdapter;
    private List<Movie> movies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        GridView gridView = findViewById(R.id.gridView);
        this.movieAdapter = new MovieImageAdapter(this, R.layout.movie_list_item, new ArrayList<Movie>());
        gridView.setAdapter(movieAdapter);
        gridView.setOnItemClickListener(

                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Movie movie = movieAdapter.getItem(i);

                        if (movie != null) {
                            Intent openDetail = new Intent(getApplicationContext(), DetailActivity.class)
                                    .putExtra(getString(R.string.movie_object), movie.getId());

                            startActivity(openDetail);
                        } else {
                            //in case of failure do not start activity but invite to retry
                            Toast.makeText(getApplicationContext(), "Cannot show movie details right now, try again soon", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("getting popular movies", "wtf");

        NetworkUtils.getPopularMovies(this, new Callback<MoviePage>() {
            @Override
            public void onResponse(Call<MoviePage> call, Response<MoviePage> response) {
                Log.d("got popular movies", "wtf");
                Repository.addMovies(getApplicationContext(), response.body().getResults());
            }

            @Override
            public void onFailure(Call<MoviePage> call, Throwable t) {
                Log.d("faild getting movies", "wtf");

            }
        });

        getContentResolver().registerContentObserver(
                MovieRepoContract.MovieEntry.CONTENT_URI, false, new ContentObserver(null) {

                    @Override
                    public void onChange(boolean selfChange) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                movieAdapter.clear();
                                movieAdapter.addAll( Repository.getMovies(getApplicationContext()));
                            }
                        });
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
