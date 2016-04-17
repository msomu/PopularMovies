/*
 * Copyright (C) 2015 Antonio Leiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.msomu.popularmovies.main;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.msomu.popularmovies.CursorRecyclerViewAdapter;
import com.msomu.popularmovies.R;
import com.msomu.popularmovies.Utility;
import com.msomu.popularmovies.data.MoviesContract;
import com.msomu.popularmovies.model.MovieModel;

public class RecyclerViewAdapter extends CursorRecyclerViewAdapter<RecyclerViewAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private Context context;

    public RecyclerViewAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        return new ViewHolder(v);
    }

//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        MovieModel item = items.get(position);
//        holder.text.setText(item.getText());
//        holder.image.setImageBitmap(null);
//        Picasso.with(holder.image.getContext()).load(item.getImage()).into(holder.image);
//        holder.itemView.setTag(item);
//    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final MovieModel item = MovieModel.from(cursor);
        holder.text.setText(item.getText());
        holder.image.setImageBitmap(null);
        //Picasso.with(holder.image.getContext()).load(item.getImage()).into(holder.image);
        Utility.renderImage(context, item.getImage(), holder.image);
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(MoviesContract.MoviesEntry.buildMovieUri(item.getId()));
            }
        });
    }

    public interface OnItemClickListener {

        void onItemClick(Uri contentUri);

    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
