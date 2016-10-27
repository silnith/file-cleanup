package org.silnith.files;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingWorker;


public class SelectDirectoriesAction extends AbstractAction {
    
    private final JPanel displayPanel;
    
    private final JList<Long> displayList;
    
    public SelectDirectoriesAction(final String name, final JList<Long> displayList, JPanel displayPanel) {
        super(name);
        this.displayList = displayList;
        this.displayPanel = displayPanel;
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        assert EventQueue.isDispatchThread();
        
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(null);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        
        final int selection = fileChooser.showOpenDialog(null);
        switch (selection) {
        case JFileChooser.APPROVE_OPTION: break;
        case JFileChooser.CANCEL_OPTION: return;
        case JFileChooser.ERROR_OPTION: return;
        default: return;
        }
        final File[] selectedFiles = fileChooser.getSelectedFiles();
        final List<File> directories = Collections.unmodifiableList(Arrays.asList(selectedFiles));
        
        final SwingWorker<Map<Long, Set<Path>>, ?> worker =
                new EnumerateFileTreeWorker(directories, displayList, displayPanel);
        worker.execute();
    }
    
}
