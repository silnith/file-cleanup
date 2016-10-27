package org.silnith.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;


public class FileSizeModel extends AbstractListModel<Long> {
    
    private final List<Long> sizes;
    
    public FileSizeModel(final Collection<Long> sizes) {
        super();
        this.sizes = new ArrayList<>(new TreeSet<>(sizes));
        Collections.reverse(this.sizes);
    }
    
    @Override
    public int getSize() {
        return sizes.size();
    }
    
    @Override
    public Long getElementAt(final int index) {
        return sizes.get(index);
    }
    
}
