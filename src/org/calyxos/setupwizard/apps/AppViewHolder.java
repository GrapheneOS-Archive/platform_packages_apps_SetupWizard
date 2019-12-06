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
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.calyxos.setupwizard.R;
import org.calyxos.setupwizard.apps.AppAdapter.AppItemListener;


class AppViewHolder extends ViewHolder {

    private final ImageView icon;
    private final TextView name;
    private final TextView summary;
    private final CheckBox checkBox;
    private final AppItemListener listener;

    AppViewHolder(@NonNull View v, AppItemListener listener) {
        super(v);
        this.listener = listener;
        icon = v.findViewById(R.id.icon);
        name = v.findViewById(R.id.name);
        summary = v.findViewById(R.id.summary);
        checkBox = v.findViewById(R.id.checkBox);
        v.setOnClickListener(view -> checkBox.toggle());
    }

    void bind(AppItem item) {
        icon.setImageDrawable(item.icon);
        name.setText(item.name);
        summary.setText(item.summary);
        // prevent recycled listener from getting called when re-binding
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(item.checked);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) listener.onItemUnchecked();
            item.checked = isChecked;
        });
    }

}
