/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.text.InputType;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.db.DatabaseHelper;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.model.MyEntity;

import static ru.orangesoftware.financisto.activity.AbstractActivity.setVisibility;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 7/2/12 9:25 PM
 */
public abstract class MyEntitySelector<T extends MyEntity, A extends AbstractActivity> {

    protected final A activity;
    protected final MyEntityManager em;
    private final ActivityLayout x;
    private final boolean isShow;
    private final int layoutId;
    private final int layoutBtnId;
    private final int labelResId;
    private final int defaultValueResId;
    private final int filterToggleId;

    private View node;
    private TextView text;
    private AutoCompleteTextView autoCompleteFilter;
    private SimpleCursorAdapter filterAdapter;
    private List<T> entities;
    private ListAdapter adapter;

    private long selectedEntityId = 0;

    public MyEntitySelector(A activity, MyEntityManager em, ActivityLayout x, boolean isShow,
                            int layoutId, int layoutBtnId, int labelResId, int defaultValueResId, int filterToggleId) {
        this.activity = activity;
        this.em = em;
        this.x = x;
        this.isShow = isShow;
        this.layoutId = layoutId;
        this.layoutBtnId = layoutBtnId;
        this.labelResId = labelResId;
        this.defaultValueResId = defaultValueResId;
        this.filterToggleId = filterToggleId;
    }

    protected abstract Class getEditActivityClass();

    public void fetchEntities() {
        fetchEntities(true);
    }

    public List<T> getEntities() {
        return entities;
    }

    public void fetchEntities(boolean defaultAdapter) {
        entities = fetchEntities(em);
        adapter = createAdapter(activity, entities);
    }

    protected abstract List<T> fetchEntities(MyEntityManager em);

    protected abstract ListAdapter createAdapter(Activity activity, List<T> entities);

    protected abstract SimpleCursorAdapter createFilterAdapter();

    public TextView createNode(LinearLayout layout) {
        return createNode(layout, R.layout.select_entry_plus);
    }

    public TextView createNode(LinearLayout layout, int nodeLayoutId) {
        if (isShow) {
            final Pair<TextView, AutoCompleteTextView> nodes = x.addListNodeWithButtonAndAutoComplete(layout, nodeLayoutId, layoutId, layoutBtnId, true, labelResId, defaultValueResId, filterToggleId);
            text = nodes.first;
            autoCompleteFilter = nodes.second;
            node = (View) this.text.getTag();
        }
        return text;
    }

    private void initAutoCompleteFilter(final AutoCompleteTextView filterTxt) {
        filterAdapter = createFilterAdapter();
        filterTxt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_VARIATION_FILTER);
        filterTxt.setThreshold(1);
        filterTxt.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                filterTxt.setAdapter(filterAdapter);
                filterTxt.selectAll();
            }
        });
        filterTxt.setOnItemClickListener((parent, view, position, id) -> {
            activity.onSelectedId(layoutId, id);
            ToggleButton toggleBtn = (ToggleButton) filterTxt.getTag();
            toggleBtn.performClick();
        });
    }

    public void onClick(int id) {
        if (id == layoutId) {
            pickEntity();
        } else if (id == layoutBtnId) { // todo.mb: fix here filter case
            Intent intent = new Intent(activity, getEditActivityClass());
            activity.startActivityForResult(intent, layoutBtnId);
        } else if (id == filterToggleId) {
            if (filterAdapter == null) initAutoCompleteFilter(autoCompleteFilter);
        }
    }

    private void pickEntity() {
        int selectedEntityPos = MyEntity.indexOf(entities, selectedEntityId);
        x.selectPosition(activity, layoutId, labelResId, adapter, selectedEntityPos);
    }

    public void onSelectedPos(int id, int selectedPos) {
        if (id == layoutId) onEntitySelected(selectedPos);
    }

    public void onSelectedId(int id, long selectedId) {
        if (id == layoutId) selectEntity(selectedId);
    }

    private void onEntitySelected(int selectedPos) {
        T e = entities.get(selectedPos);
        selectEntity(e);
    }

    public void selectEntity(long entityId) {
        if (isShow) {
            T e = MyEntity.find(entities, entityId);
            selectEntity(e);
        }
    }

    private void selectEntity(T e) {
        if (isShow && e != null) {
            text.setText(e.title);
            selectedEntityId = e.id;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == layoutBtnId) {
            onNewEntity(data);
        }
    }

    private void onNewEntity(Intent data) {
        fetchEntities();
        long entityId = data.getLongExtra(DatabaseHelper.EntityColumns.ID, -1);
        if (entityId != -1) {
            selectEntity(entityId);
        }
    }

    public void setNodeVisible(boolean visible) {
        if (isShow) {
            setVisibility(node, visible ? View.VISIBLE : View.GONE);
        }
    }

    public long getSelectedEntityId() {
        return node == null || node.getVisibility() == View.GONE ? 0 : selectedEntityId;
    }

}
