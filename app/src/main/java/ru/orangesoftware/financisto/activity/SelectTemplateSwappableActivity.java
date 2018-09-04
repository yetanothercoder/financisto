package ru.orangesoftware.financisto.activity;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.adapter.async.SmsTemplateListAsyncAdapter;
import ru.orangesoftware.financisto.adapter.async.TransactionTemplateListSource;
import ru.orangesoftware.financisto.blotter.BlotterFilter;
import ru.orangesoftware.financisto.db.DatabaseAdapter;

public class SelectTemplateSwappableActivity extends AppCompatActivity {
    private static final String TAG = "Financisto." + SelectTemplateSwappableActivity.class.getSimpleName();

    private DatabaseAdapter db;
    private TransactionTemplateListSource cursorSource;
    private SmsTemplateListAsyncAdapter adapter;
    private RecyclerView recyclerView;
    private Parcelable listState;
    
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.draglist_bar_layout);

        db = new DatabaseAdapter(this);
        db.open();

//        Toolbar menu = findViewById(R.id.tool_bar);
//        setSupportActionBar(menu);

        recyclerView = findViewById(R.id.drag_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cursorSource = createSource();
        createAdapter(true);
        ((TextView) findViewById(android.R.id.empty)).setVisibility(View.GONE); // todo.mb: handle later

        if(state != null) listState = state.getParcelable(LIST_STATE_KEY);
    }

    @NonNull
    protected TransactionTemplateListSource createSource() {
        return new TransactionTemplateListSource(db, true, BlotterFilter.SORT_NEWER_TO_OLDER); // todo.mb
    }
}
