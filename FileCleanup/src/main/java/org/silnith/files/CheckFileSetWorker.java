package org.silnith.files;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;


public class CheckFileSetWorker extends SwingWorker<Collection<Set<Path>>, JProgressBar> {
    
    private static final class RemoveButtonFromPanelListener implements ActionListener {
        
        private final JButton deleteFileButton;
        
        private final JPanel identicalPanel;
        
        private RemoveButtonFromPanelListener(JButton deleteFileButton, JPanel identicalPanel) {
            this.deleteFileButton = deleteFileButton;
            this.identicalPanel = identicalPanel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            assert EventQueue.isDispatchThread();
            
            identicalPanel.remove(deleteFileButton);
            identicalPanel.revalidate();
            identicalPanel.repaint();
        }
    }

    private final Long fileSize;
    
    private final JPanel choicePanel;
    
    private final JPanel progressPanel;
    
    private final JList<Long> displayList;
    
    private final Map<Long, Set<Path>> fileSizeToPathsMap;
    
    public CheckFileSetWorker(Map<Long, Set<Path>> fileSizeToPathsMap, final Long fileSize, JList<Long> displayList,
            final JPanel choicePanel, final JPanel progressPanel) {
        this.fileSize = fileSize;
        this.choicePanel = choicePanel;
        this.progressPanel = progressPanel;
        this.displayList = displayList;
        this.fileSizeToPathsMap = fileSizeToPathsMap;
    }
    
    @Override
    protected Collection<Set<Path>> doInBackground() throws Exception {
        final int progressBarMax = Long.valueOf(fileSize).intValue();
        
        final Collection<Set<Path>> identicalFileSets = new ArrayList<>();
        
        final List<Path> fileSetCopy = new ArrayList<>(fileSizeToPathsMap.get(fileSize));
        Collections.sort(fileSetCopy, new Comparator<Path>() {

            @Override
            public int compare(Path o1, Path o2) {
                return o1.toString().compareTo(o2.toString());
            }
            
        });
        while ( !fileSetCopy.isEmpty()) {
            final Set<Path> identicalFiles = new HashSet<>();
            
            final Iterator<Path> iterator = fileSetCopy.iterator();
            final Path first = iterator.next();
            System.out.println("first: " + first);
            iterator.remove();
            identicalFiles.add(first);
            while (iterator.hasNext()) {
                final Path next = iterator.next();
                System.out.println("next: " + next);
                final JProgressBar progressBar = new JProgressBar();
                progressBar.setMaximum(progressBarMax);
                publish(progressBar);
                final FileComparisonWorker fileComparisonWorker = new FileComparisonWorker(first, next, progressBar);
                fileComparisonWorker.execute();
                final Boolean identical = fileComparisonWorker.get();
                if (identical) {
                    iterator.remove();
                    identicalFiles.add(next);
                }
            }
            System.out.println(identicalFiles);
            identicalFileSets.add(identicalFiles);
        }
        System.out.println(identicalFileSets);
        return identicalFileSets;
    }
    
    @Override
    protected void process(final List<JProgressBar> chunks) {
        for (final JProgressBar progressBar : chunks) {
            progressPanel.add(progressBar);
        }
        progressPanel.revalidate();
        progressPanel.repaint();
    }
    
    @Override
    protected void done() {
        try {
            final Collection<Set<Path>> identicalFileSets = get();
            for (final Set<Path> identicalFiles : identicalFileSets) {
                final JPanel identicalPanel = new JPanel(new GridLayout(0, 1));
                identicalPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                final String fileSizeMessage = String.format("File Size: %,d", fileSize);
                final JLabel fileSizeLabel = new JLabel(fileSizeMessage);
                identicalPanel.add(fileSizeLabel);
                for (final Path path : identicalFiles) {
                    final Action deleteFileAction = new AbstractAction("Delete " + path) {
                        
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("deleting " + path);
                            try {
                                Files.delete(path);
                                final Set<Path> filesOfSize = fileSizeToPathsMap.get(fileSize);
                                filesOfSize.remove(path);
                                if (filesOfSize.size() < 2) {
                                    fileSizeToPathsMap.remove(fileSize);
                                    displayList.setModel(new FileSizeModel(fileSizeToPathsMap.keySet()));
                                }
                            } catch (final IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        
                    };
                    final JButton deleteFileButton = new JButton(deleteFileAction);
                    deleteFileButton.addActionListener(new RemoveButtonFromPanelListener(deleteFileButton, identicalPanel));
                    identicalPanel.add(deleteFileButton);
                }
                choicePanel.add(identicalPanel);
            }
            choicePanel.revalidate();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
