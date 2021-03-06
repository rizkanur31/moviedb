package com.example.Rizka.themoviedb;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Rizka.themoviedb.data.FavouriteDBHelper;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.ArrayList;
import java.util.List;

public class MovieActivity extends AppCompatActivity {

    public static String MOVIE_ID = "movie_id";

    private static String YOUTUBE_VIDEO_URL = "http://www.youtube.com/watch?v=%s";
    private static String YOUTUBE_THUMBNAIL_URL = "http://img.youtube.com/vi/%s/0.jpg";
    private static String POSTER_URL = "https://image.tmdb.org/t/p/w185";

    private ImageView movieBackdrop;
    private TextView movieTitle;
    private TextView movieGenres;
    private TextView movieOverview;
    private TextView movieOverviewLabel;
    private TextView movieReleaseDate;
    private RatingBar movieRating;
    private TextView trailersLabel;
    private LinearLayout movieTrailers;
    private LinearLayout movieReviews;
    private LinearLayout movieCasts;
    private TextView reviewsLabel;
    private TextView castsLabel;
    FavouriteDBHelper favouriteDBHelper;
    private Movie favourite;
    MaterialFavoriteButton materialFavoriteButton;
    private MoviesRepository moviesRepository;
    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        movieId = getIntent().getIntExtra(MOVIE_ID, movieId);

        moviesRepository = MoviesRepository.getInstance();

        setupToolbar();

        initUI();

        getMovie();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initUI() {
        movieBackdrop = findViewById(R.id.movieDetailsBackdrop);
        movieTitle = findViewById(R.id.movieDetailsTitle);
        movieGenres = findViewById(R.id.movieDetailsGenres);
        movieOverview = findViewById(R.id.movieDetailsOverview);
        movieOverviewLabel = findViewById(R.id.summaryLabel);
        movieReleaseDate = findViewById(R.id.movieDetailsReleaseDate);
        movieRating = findViewById(R.id.movieDetailsRating);
        movieTrailers = findViewById(R.id.movieTrailers);
        movieReviews = findViewById(R.id.movieReviews);
        movieCasts= findViewById(R.id.movieCast);
        trailersLabel = findViewById(R.id.trailersLabel);
        castsLabel =findViewById(R.id.castLabels);
        reviewsLabel = findViewById(R.id.reviewsLabel);
        materialFavoriteButton =  findViewById(R.id.favourite);
    }

    private void getMovie() {
        moviesRepository.getMovie(movieId, new OnGetMovieCallback() {
            @Override
            public void onSuccess(final Movie movie) {
                movieTitle.setText(movie.getTitle());
                movieOverviewLabel.setVisibility(View.VISIBLE);
                movieOverview.setText(movie.getOverview());
                movieRating.setVisibility(View.VISIBLE);
                movieRating.setRating(movie.getRating() / 2);
                getGenres(movie);
                getTrailers(movie);
                getReviews(movie);
                getCasts(movie);
                Boolean flag = isFavourite(movie);
                if(flag) {
                    materialFavoriteButton.setFavorite(true);
                    materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if (favorite==true) {

                                Toast.makeText(MovieActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
                                movie.setFavouriteflag(true);
                                saveFavourite(movie);

                            } else {
                                Toast.makeText(MovieActivity.this, "Removed From Favourites", Toast.LENGTH_SHORT).show();
                                movie.setFavouriteflag(false);
                                favouriteDBHelper = new FavouriteDBHelper(MovieActivity.this);
                                favouriteDBHelper.deleteFavourite(movie.getId());
                            }
                        }
                    });
                }
                else {
                    materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if (favorite==true) {

                                Toast.makeText(MovieActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
                                movie.setFavouriteflag(true);
                                saveFavourite(movie);
                            }
                            else {
                                Toast.makeText(MovieActivity.this, "Removed From Favourites", Toast.LENGTH_SHORT).show();
                                movie.setFavouriteflag(false);
                                favouriteDBHelper = new FavouriteDBHelper(MovieActivity.this);
                                favouriteDBHelper.deleteFavourite(movie.getId());

                            }
                        }
                    });
                }
                movieReleaseDate.setText(movie.getReleaseDate());
                if (!isFinishing()) {
                    Glide.with(MovieActivity.this)
                            .load(movie.getBackdrop())
                            .apply(RequestOptions.placeholderOf(R.drawable.load))
                            .into(movieBackdrop);
                }
            }

            @Override
            public void onError() {
                finish();
            }
        });
    }

    private void getReviews(Movie movie) {
        moviesRepository.getReviews(movie.getId(), new OnGetReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                reviewsLabel.setVisibility(View.VISIBLE);
                movieReviews.removeAllViews();
                for (Review review : reviews) {
                    View parent = getLayoutInflater().inflate(R.layout.review, movieReviews, false);
                    TextView author = parent.findViewById(R.id.reviewAuthor);
                    TextView content = parent.findViewById(R.id.reviewContent);
                    author.setText(review.getAuthor());
                    content.setText(review.getContent());
                    movieReviews.addView(parent);
                }
            }

            @Override
            public void onError() {
                // Do nothing
            }
        });
    }

    private void getGenres(final Movie movie) {
        moviesRepository.getGenres(new OnGetGenresCallback() {
            @Override
            public void onSuccess(List<Genre> genres) {
                if (movie.getGenres() != null) {
                    List<String> currentGenres = new ArrayList<>();
                    for (Genre genre : movie.getGenres()) {
                        currentGenres.add(genre.getName());
                    }
                    movieGenres.setText(TextUtils.join(", ", currentGenres));
                }
            }

            @Override
            public void onError() {
                showError();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getTrailers(Movie movie) {
        moviesRepository.getTrailers(movie.getId(), new OnGetTrailersCallback() {
            @Override
            public void onSuccess(List<Trailer> trailers) {
                trailersLabel.setVisibility(View.VISIBLE);
                movieTrailers.removeAllViews();
                String s,s1;
                for (final Trailer trailer : trailers) {
                    View parent = getLayoutInflater().inflate(R.layout.thumbnail_trailer, movieTrailers, false);
                    ImageView thumbnail = parent.findViewById(R.id.thumbnail);
                    thumbnail.requestLayout();
                    thumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showTrailer(String.format(YOUTUBE_VIDEO_URL, trailer.getKey()));
                        }
                    });
                    s = String.format(YOUTUBE_THUMBNAIL_URL, trailer.getKey());
                    Glide.with(MovieActivity.this)
                            .load(s)
                            .apply(RequestOptions.placeholderOf(R.drawable.load).centerCrop())
                            .into(thumbnail);
                    movieTrailers. addView(parent);
                }
            }
            @Override
            public void onError() {
                // Do nothing
                trailersLabel.setVisibility(View.GONE);
            }
        });
    }

    private void getCasts(Movie movie) {
        moviesRepository.getCasts(movie.getId(), new OnGetsCastsCallback() {
            @Override
            public void onSuccess(List<Cast> casts) {
                castsLabel.setVisibility(View.VISIBLE);
                movieCasts.removeAllViews();
                for (final Cast cast : casts) {
                    View parent = getLayoutInflater().inflate(R.layout.cast, movieCasts, false);
                    TextView character = parent.findViewById(R.id.charactername);
                    TextView name = parent.findViewById(R.id.castname);
                    ImageView moviecast = parent.findViewById(R.id.castimage);
                    moviecast.requestLayout();
                    Glide.with(MovieActivity.this)
                            .load(cast.getProfilePath())
                            .apply(RequestOptions.placeholderOf(R.drawable.load).centerCrop())
                            .into(moviecast);
                    character.setText(cast.getCharacter());
                    name.setText(cast.getName());
                    movieCasts.addView(parent);
                }
            }

            @Override
            public void onError() {
                // Do nothing
            }
        });
    }

    public Boolean isFavourite(Movie movie)
    {
        int f = 0;
        List<Movie> movies = new ArrayList<>();
        favouriteDBHelper = new FavouriteDBHelper(MovieActivity.this);
        movies.addAll(favouriteDBHelper.getAllFavourite());
        if(movies.isEmpty() || movies== null)
        {
            return false;
        }
        else {
            for (Movie m : movies) {
                if (m.getId() == movie.getId()) {
                    f = 1;
                    break;
                }
            }
            if (f == 1)
                return true;
            else
                return false;
        }
    }

    public void saveFavourite(Movie movie)
    {
        favouriteDBHelper = new FavouriteDBHelper(MovieActivity.this);
        favourite = new Movie();
        int movie_id = movie.getId();
        String rate = String.valueOf(movie.getRating());
        String poster = movie.getPosterPath();

        favourite.setId(movie_id);
        favourite.setTitle(movie.getTitle());
        favourite.setPosterPath(poster);
        favourite.setRating(Float.parseFloat(rate));
        favourite.setReleaseDate(movie.getReleaseDate());
        favourite.setOverview(movie.getOverview());
        favourite.setFavouriteflag(true);
        favouriteDBHelper.addFavourite(favourite);
    }

    private void showTrailer(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void showError() {
        Toast.makeText(MovieActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
    }
}

