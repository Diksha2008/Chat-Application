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

        Log.i(TAG, "onScrolled: totalitemcount" + totalItemCount);
        Log.i(TAG, "onScrolled: previoustotalitemcount" + previousTotalItemCount);
        Log.i(TAG, "onScrolled: firstvisibleitemposition" + firstVisibleItemPosition);

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if(totalItemCount < previousTotalItemCount){
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0){
                this.loading = true;
            }
        }

        //data has already been loaded as total items are greater than its previous value, hence change it to false
        if (loading && (totalItemCount > previousTotalItemCount)){
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        //check for scroll up
        if(dy < 0) {
            //if it is not loading and when among all the loaded items the firstVisible loaded item becomes less than threshold
            if (!loading && firstVisibleItemPosition <= visibleThreshold) {
                currentPage++;
                onLoadMore(currentPage, totalItemCount, recyclerView);
                loading = true;
            }
        }
    }

    public abstract void onLoadMore(int page, int totalItemCount, RecyclerView view);
}
