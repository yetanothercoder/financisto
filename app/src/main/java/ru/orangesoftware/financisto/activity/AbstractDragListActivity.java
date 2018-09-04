package ru.orangesoftware.financisto.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.adapter.async.AsyncAdapter;
import ru.orangesoftware.financisto.adapter.async.CursorItemSource;
import ru.orangesoftware.financisto.adapter.dragndrop.ItemTouchHelperAdapter;
import ru.orangesoftware.financisto.adapter.dragndrop.SimpleItemTouchHelperCallback;
import ru.orangesoftware.financisto.db.DatabaseAdapter;

import java.util.Objects;

public abstract class AbstractDragListActivity<E, 
        S extends CursorItemSource<E>, 
        VH extends RecyclerView.ViewHolder, 
        A extends AsyncAdapter<E, VH> & ItemTouchHelperAdapter> extends AppCompatActivity {

    private static final String LIST_STATE_KEY = "LIST_STATE";
    
    private DatabaseAdapter db;
    private S cursorSource;
    private A adapter;
    private RecyclerView recyclerView;
    private Parcelable listState;
    
    private final boolean dragnDrop;
    private final int layoutId;

    protected AbstractDragListActivity(A adapter, boolean dragnDrop, int layoutId) {
        this.dragnDrop = dragnDrop;
        this.adapter = Objects.requireNonNull(adapter);
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(layoutId);

        db = new DatabaseAdapter(this);
        db.open();

//        Toolbar menu = findViewById(R.id.tool_bar);
//        setSupportActionBar(menu);

        recyclerView = findViewById(R.id.drag_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cursorSource = createSource();
        createAdapter(dragnDrop);
        ((TextView) findViewById(android.R.id.empty)).setVisibility(View.GONE); // todo.mb: handle later

        if(state != null) listState = state.getParcelable(LIST_STATE_KEY);
    }

    @NonNull
    protected abstract S createSource();

    private void createAdapter(boolean dragnDrop) {
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (dragnDrop) {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(recyclerView);
        }
        adapter.onStart(recyclerView);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        listState = recyclerView.getLayoutManager().onSaveInstanceState(); // https://stackoverflow.com/a/28262885/365675
        state.putParcelable(LIST_STATE_KEY, listState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listState != null) recyclerView.getLayoutManager().onRestoreInstanceState(listState);
    }

    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            adapter.reloadVisibleItems();
        } else if (resultCode == RESULT_CANCELED) {
            adapter.revertSwipeBack();
        }
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        adapter.onStop(recyclerView);

        super.onDestroy();
    }
}
