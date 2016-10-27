package org.silnith.files;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;


public class Cleanup {
    
    public static class CreateFrame implements Runnable {
        
        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            } catch (final InstantiationException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            } catch (final UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            
            JDialog.setDefaultLookAndFeelDecorated(true);
            
            final JPanel centerPanel = new JPanel(new BorderLayout());
            
            final JList<Long> displayList = new JList<>();
            displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            final Action selectDirectoriesAction =
                    new SelectDirectoriesAction("Select Directories", displayList, centerPanel);
                    
            final JMenuItem selectDirectoriesMenuItem = new JMenuItem(selectDirectoriesAction);
            
            final JMenu fileMenu = new JMenu("File");
            fileMenu.add(selectDirectoriesMenuItem);
            
            final JMenuBar menuBar = new JMenuBar();
            menuBar.add(fileMenu);
            
            final JScrollPane displayScroll = new JScrollPane(displayList,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    
            final JScrollPane centerScroll = new JScrollPane(centerPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    
            final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, displayScroll, centerScroll);
            
            final JFrame frame = new JFrame("Cleanup");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setLocationByPlatform(true);
            
            frame.setJMenuBar(menuBar);
            
            frame.setContentPane(splitPane);
            
            frame.pack();
            frame.setVisible(true);
        }
        
    }
    
    public static void main(final String[] args) throws IOException, InvocationTargetException, InterruptedException {
        final FileSystem defaultFileSystem = FileSystems.getDefault();
        for (final Path rootDirectory : defaultFileSystem.getRootDirectories()) {
            if (Files.exists(rootDirectory) && Files.isReadable(rootDirectory) && Files.isDirectory(rootDirectory)) {
//                final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootDirectory);
//                Files.walkFileTree(rootDirectory, new MyFileVisitor());
            }
        }
        final Iterable<FileStore> fileStores = defaultFileSystem.getFileStores();
        for (final FileStore fileStore : fileStores) {
            System.out.println(fileStore);
        }
        
        SwingUtilities.invokeAndWait(new CreateFrame());
    }
    
}
