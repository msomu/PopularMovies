package com.msomu.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by msomu on 12/04/16.
 */
public class ViewUtil {
    public static LinearLayout createTrailerLayout(final Context context, String name, final String source) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setWeightSum(100f);
        linearLayout.setPadding(10, 10, 10, 10);
        ImageView adjustableImageView = new ImageView(context);
        adjustableImageView.setAdjustViewBounds(true);
        adjustableImageView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 50f));
        adjustableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + source)));
            }
        });
        linearLayout.addView(adjustableImageView);
        TextView textView = new TextView(context);
        textView.setText(name);
        textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        textView.setPadding(20, 0, 0, 0);
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 50f);
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        linearLayout.addView(textView);
        //imageLoader.get("http://img.youtube.com/vi/" + source + "/0.jpg", ImageLoader.getImageListener(adjustableImageView, R.drawable.loading_trailer, R.drawable.error_trailer));
        Picasso.with(context).load("http://img.youtube.com/vi/" + source + "/0.jpg").into(adjustableImageView);
        return linearLayout;
    }

    public static LinearLayout createReviewLayout(Context context, String author, final String content) {
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(10, 10, 10, 10);
        TextView textViewContent = new TextView(context);
        textViewContent.setText("\" " + content + " \"");
        textViewContent.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textViewContent.setPadding(0, 10, 0, 10);
        textViewContent.setLayoutParams(layoutParams);
        textViewContent.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        linearLayout.addView(textViewContent);
        TextView textViewAuthor = new TextView(context);
        textViewAuthor.setText("-" + author);
        textViewAuthor.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        textViewAuthor.setPadding(0, 0, 20, 30);
        textViewAuthor.setLayoutParams(layoutParams);
        textViewAuthor.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        linearLayout.addView(textViewAuthor);
        return linearLayout;
    }

    public static View getLineView(Context context) {
        View view = new View(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setBackgroundColor(Color.GRAY);
        view.setMinimumHeight(2);
        return view;
    }
}
