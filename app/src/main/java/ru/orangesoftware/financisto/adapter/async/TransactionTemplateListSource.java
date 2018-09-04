package ru.orangesoftware.financisto.adapter.async;

import android.database.Cursor;
import ru.orangesoftware.financisto.blotter.BlotterFilter;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.filter.WhereFilter;
import ru.orangesoftware.financisto.model.Transaction;

public class TransactionTemplateListSource extends CursorItemSource<Transaction> {
    
    private final DatabaseAdapter db;
    private final String sortOrder;
    private volatile String filter;
    private final WhereFilter blotterFilter;

    public TransactionTemplateListSource(DatabaseAdapter db, boolean prepareCursor, String sortOrder) {
        this.db = db;
        this.sortOrder = sortOrder;
        this.blotterFilter = new WhereFilter("templates")
                .eq(BlotterFilter.IS_TEMPLATE, String.valueOf(1))
                .eq(BlotterFilter.PARENT_ID, String.valueOf(0));
        
        if (prepareCursor) prepareCursor();
    }

    @Override
    public Cursor initCursor() {
        return db.getAllTemplates(blotterFilter, sortOrder);
    }

    @Override
    protected Transaction loadItem() {
        return Transaction.fromBlotterCursor(cursor);
    }

    @Override
    public Class<Transaction> clazz() {
        return Transaction.class;
    }

    @Override
    public void setConstraint(CharSequence constraint) {
        filter = constraint == null ? null : constraint.toString();
    }
}
