package de.vote.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class forms together with an implementation and the 
 * <code>pageabletable.xhtml</code> template a table controls which enable the
 * user to browse through a huge table site by site.
 * To display a table with this feature one needs to access the entries field
 * via EL, e.g. <code>${beanName.entries}</code>, in a normal h:dataTable.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 * @param <T>
 */
public abstract class AbstractPageableTable<T> implements Serializable {
    
    /**
     * Time to cache the list. During this time the data is not updated
     * if not invalidate() is called.
     */
    protected final int CACHETIME = 250;
    
    /**
     * Number of elements to display per page
     */
    private int pageSize = 7, oldPage = -1, page = 1, pageCount;
    
    private List<T> entries;
    
    private long lastReload = 0;
    
    /**
     * Constructs a new instance.
     */
    public AbstractPageableTable() { }
    
    /**
     * Constructs a new instance.
     * 
     * @param pageSize The number of elements to display per page
     */
    public AbstractPageableTable(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public Iterable<T> getEntries() {
        refresh();
        return entries;
    }

    public int getPageCount() {
        refresh();
        return pageCount;
    }
    
    private void refresh() {
        if (!(System.currentTimeMillis() - lastReload > CACHETIME)) {
            if (entries != null) {
                if (oldPage == page) {
                    return;
                }
            }   
        }
        
        if (entries == null) {
            entries = new ArrayList<>();
        }
        
        entries.clear();
        
        int totalResults;
        if (isViewAll()) {
            totalResults = load(0, -1, entries);
        } else {
            totalResults = load((page - 1) * pageSize, pageSize, entries);            
        }
        
        oldPage = page;
        pageCount = ((int) totalResults + pageSize - 1) / pageSize;
        lastReload = System.currentTimeMillis();
    }
    
    /**
     * Get the current page.
     * 
     * @return The current page, ranging from 1 to *.
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the current page.
     * 
     * @param page The page to set. If the argument is less than zero, page zero
     * is set. This means, that all entries are displayed.
     */
    public void setPage(int page) {
        this.oldPage = this.page;
        if (page < 1) {
            this.page = 0;
        } else if (page >= getPageCount()) {
            this.page = getPageCount();
        } else {
            this.page = page;
        }
    }

    public boolean isEmpty() {
        return entries == null ? false : entries.isEmpty();
    }
    
    public boolean isViewAll() {
        return page < 1;
    }
    
    public boolean canPrevious() {
        return page > 1;
    }
    
    public boolean canNext() {
        return page < getPageCount();
    }
    
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
 
    public void invalidate() {
        this.oldPage = -1;
        this.entries = null;
    }
    
    /**
     * Callback method to load entities to display.
     * 
     * @param start The start index.
     * @param count The number of objects to load to display one page of
     * objects. If this is -1 all objects should be loaded.
     * @param entryList The target list which needs to be filled with the loaded
     * objects.
     * @return Returns the number of <u>total</u> objects available.
     */
    protected abstract int load(int start, int count, List<T> entryList);

}
