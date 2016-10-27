package org.silnith.files;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class SelectFileSizeListener implements ListSelectionListener {
    
    private final JList<Long> displayList;
    
    private final JPanel displayPanel;
    
    private final Map<Long, Set<Path>> fileSizeToPathsMap;
    
    public SelectFileSizeListener(final Map<Long, Set<Path>> fileSizeToPathsMap, final JList<Long> displayList,
            final JPanel displayPanel) {
        this.fileSizeToPathsMap = fileSizeToPathsMap;
        this.displayList = displayList;
        this.displayPanel = displayPanel;
    }
    
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        assert EventQueue.isDispatchThread();
        
        if (e.getValueIsAdjusting()) {
            return;
        }
        final JPanel choicePanel = new JPanel();
        final JPanel progressPanel = new JPanel(new GridLayout(0, 1));
        
        displayPanel.removeAll();
        displayPanel.add(choicePanel, BorderLayout.CENTER);
        displayPanel.add(progressPanel, BorderLayout.PAGE_START);
        displayPanel.revalidate();
        displayPanel.repaint();
        
        final Long fileSize = displayList.getSelectedValue();
        
        if (fileSize == null) {
            return;
        }
        
        final SwingWorker<Collection<Set<Path>>, ?> worker =
                new CheckFileSetWorker(fileSizeToPathsMap, fileSize, displayList, choicePanel, progressPanel);
        worker.execute();
    }
    
}
