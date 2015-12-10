/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 baoyongzhang <baoyz94@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.tubb.smrv.demo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tubb.smrv.SwipeMenu;
import com.tubb.smrv.SwipeMenuCreator;
import com.tubb.smrv.SwipeMenuItem;
import com.tubb.smrv.SwipeMenuRecyclerView;
import com.tubb.smrv.SwipeMenuRecyclerViewAdapter;

import java.util.List;

/**
 * SwipeMenuListView
 * Created by baoyz on 15/6/29.
 */
public class SimpleRvActivity extends Activity {

    private Context mContext;
    private List<ApplicationInfo> mAppList;
    private AppAdapter mAdapter;
    private SwipeMenuRecyclerView mListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final int MENU_OPEN_TYPE = 1;
    private static final int MENU_DELETE_TYPE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mContext = this;
        mAppList = getPackageManager().getInstalledApplications(0);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(mContext, "刷新成功", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        mListView = (SwipeMenuRecyclerView) findViewById(R.id.listView);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AppAdapter(this, mListView, mAppList);
        mListView.setAdapter(mAdapter);

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item type
                openItem.setType(MENU_OPEN_TYPE);
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item type
                deleteItem.setType(MENU_DELETE_TYPE);
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuRecyclerView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                ApplicationInfo item = mAppList.get(position);
                SwipeMenuItem swipeMenuItem = menu.getMenuItem(index);
                switch (swipeMenuItem.getType()) {
                    case MENU_OPEN_TYPE:
                        open(item);
                        break;
                    case MENU_DELETE_TYPE:
                        mAppList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        break;
                }
                return false;
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuRecyclerView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // other setting
		mListView.setCloseInterpolator(new BounceInterpolator());

    }

    private void open(ApplicationInfo item) {
        // open app
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(item.packageName);
        List<ResolveInfo> resolveInfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        if (resolveInfoList != null && resolveInfoList.size() > 0) {
            ResolveInfo resolveInfo = resolveInfoList.get(0);
            String activityPackageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName componentName = new ComponentName(
                    activityPackageName, className);

            intent.setComponent(componentName);
            startActivity(intent);
        }
    }

    class AppAdapter extends SwipeMenuRecyclerViewAdapter {

        private static final int VIEW_TYPE_ENABLE = 0;
        private static final int VIEW_TYPE_DISABLE = 1;

        List<ApplicationInfo> mAppList;

        public AppAdapter(Context context, SwipeMenuRecyclerView rv, List<ApplicationInfo> appList){
            super(context, rv);
            mAppList = appList;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mAppList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(position % 2 == 0){
                return VIEW_TYPE_DISABLE;
            }else{
                return VIEW_TYPE_ENABLE;
            }
        }

        @Override
        public boolean swipeEnableByViewType(int viewType) {
            if(viewType == VIEW_TYPE_ENABLE) return true;
            else if(viewType == VIEW_TYPE_DISABLE) return false;
            else return true; // default
        }

        @Override
        public View onCreateItemView(ViewGroup viewGroup, int viewType) {
            return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_app, viewGroup, false);
        }

        @Override
        public RecyclerView.ViewHolder onCreateWrapViewHolder(View itemView, int viewType) {
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindWrapViewHolder(RecyclerView.ViewHolder vh, final int position) {
            final ApplicationInfo item = mAppList.get(position);
            final MyViewHolder myViewHolder = (MyViewHolder)vh;
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    open(item);
                }
            });
            myViewHolder.btGood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(myViewHolder.itemView.getContext(), "Good Button", Toast.LENGTH_SHORT).show();
                }
            });
            myViewHolder.tv_name.setText(item.loadLabel(myViewHolder.itemView.getContext().getPackageManager()));
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ViewGroup vg;
        ImageView iv_icon;
        TextView tv_name;
        View btGood;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.vg = (ViewGroup)itemView;
            iv_icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            btGood = itemView.findViewById(R.id.btGood);
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_left) {
            mListView.setSwipeDirection(SwipeMenuRecyclerView.DIRECTION_LEFT);
            return true;
        }
        if (id == R.id.action_right) {
            mListView.setSwipeDirection(SwipeMenuRecyclerView.DIRECTION_RIGHT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
