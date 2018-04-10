package com.example.diksha.chatapplication;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;

import static android.content.ContentValues.TAG;

/**
 * Created by diksha on 4/4/18.
 */

public abstract class InfiniteRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    private int visibleThreshold = 10;
    private int currentPage = 0;
    private  int previousTotalItemCount = 0;
    private boolean loading = true;
    private int startingPageIndex = 0;

    private RecyclerView.LayoutManager mLayoutManager;

    public InfiniteRecyclerViewScrollListener(LinearLayoutManager layoutManager){
        this.mLayoutManager = layoutManager;
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int firstVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();
        firstVisibleItemPosition = ((LinearLayoutManager)mLayoutManager).findFirstVisibleItemPosition();

        if(totalItemCount < previousTotalItemCount){
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0){
                this.loading = true;
            }
        }

        if (loading && (totalItemCount > previousTotalItemCount)){
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        //check for scroll up
        if(dy < 0) {
            if (!loading && firstVisibleItemPosition <= visibleThreshold) {
                currentPage++;
                onLoadMore(currentPage, totalItemCount, recyclerView);
                loading = true;
            }
        }
    }

    public abstract void onLoadMore(int page, int totalItemCount, RecyclerView view);
}
