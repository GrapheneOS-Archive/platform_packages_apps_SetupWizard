/*
 * Copyright (C) 2019 The Calyx Institute
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.calyxos.setupwizard.apps;

import android.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedList.Callback;

import org.calyxos.setupwizard.R;

import java.util.ArrayList;

class AppAdapter extends Adapter<AppViewHolder> {

    private final AppItemListener listener;
    private final SortedList<AppItem> items = new SortedList<>(AppItem.class, new Callback<AppItem>() {
        @Override
        public int compare(AppItem i1, AppItem i2) {
            if (i1.name == null) return 1;
            if (i2.name == null) return -1;
            return i1.name.toString().compareTo(i2.name.toString());
        }

        @Override
        public void onChanged(int pos, int count) {
            notifyItemRangeChanged(pos, count);
        }

        @Override
        public boolean areContentsTheSame(AppItem i1, AppItem i2) {
            return i1.allEquals(i2);
        }

        @Override
        public boolean areItemsTheSame(AppItem i1, AppItem i2) {
            return i1.equals(i2);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int position, int count) {
            notifyItemMoved(position, count);
        }
    });

    AppAdapter(AppItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_app, viewGroup, false);
        return new AppViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder viewHolder, int i) {
        viewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void addItem(AppItem item) {
        items.add(item);
    }

    void setAllChecked(boolean checked) {
        for (int i = 0; i < items.size(); i++) {
            AppItem item = items.get(i);
            if (item.checked != checked) {
                item.checked = checked;
                notifyItemChanged(i, item);
            }
        }
    }

    ArrayList<String> getSelectedPackageIdPaths() {
        ArrayList<String> packageIds = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            AppItem item = items.get(i);
            if (item.checked) packageIds.add(item.path);
        }
        return packageIds;
    }

    interface AppItemListener {
        void onItemUnchecked();
    }

}
