package org.silnith.files;

import java.awt.EventQueue;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionListener;


public class EnumerateFileTreeWorker extends SwingWorker<Map<Long, Set<Path>>, Integer> {
    
    private final Collection<File> directories;
    
    private final JList<Long> displayList;
    
    private final JPanel displayPanel;
    
    public EnumerateFileTreeWorker(final Collection<File> directories,
            final JList<Long> displayList, final JPanel displayPanel) {
        this.directories = directories;
        this.displayList = displayList;
        this.displayPanel = displayPanel;
    }
    
    @Override
    protected Map<Long, Set<Path>> doInBackground() throws Exception {
        assert !EventQueue.isDispatchThread();
        
        if (directories == null) {
            return null;
        }
        
        final ConcurrentMap<Long, Set<Path>> accumulator = new ConcurrentHashMap<>();
        for (final File directory : directories) {
            final FileTreeWalker fileTreeWalker = new FileTreeWalker(directory, accumulator);
            fileTreeWalker.call();
        }
        final Iterator<Map.Entry<Long, Set<Path>>> iterator = accumulator.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Long, Set<Path>> next = iterator.next();
            final Set<Path> value = next.getValue();
            if (value.size() < 2) {
                iterator.remove();
            }
        }
        
        return accumulator;
    }
    
    @Override
    protected void done() {
        assert EventQueue.isDispatchThread();
        
        try {
            final Map<Long, Set<Path>> map = get();
            
            final Set<Long> fileSizes = map.keySet();
            
            final ListSelectionListener[] listSelectionListeners = displayList.getListSelectionListeners();
            for (final ListSelectionListener listener : listSelectionListeners) {
                displayList.removeListSelectionListener(listener);
            }
            
            displayList.setModel(new FileSizeModel(fileSizes));
            displayList.addListSelectionListener(new SelectFileSizeListener(map, displayList, displayPanel));
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
    
}
