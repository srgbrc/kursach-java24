import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Date;

public class FileSystemAnalyzer extends JFrame {
    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;

    public FileSystemAnalyzer() {
        setTitle("File System Analyzer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rootNode = new DefaultMutableTreeNode("File System");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);

        JScrollPane scrollPane = new JScrollPane(tree);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JButton analyzeButton = new JButton("Analyze");
        analyzeButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                Path startPath = fileChooser.getSelectedFile().toPath();
                analyzePath(startPath, rootNode);
                treeModel.reload();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(analyzeButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void analyzePath(Path path, DefaultMutableTreeNode parentNode) {
        try {
            if (Files.isDirectory(path)) {
                analyzeDirectory(path, parentNode);
            } else {
                analyzeFile(path, parentNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyzeDirectory(Path path, DefaultMutableTreeNode parentNode) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getFileName().toString());
                parentNode.add(node);
                analyzePath(entry, node);
            }
        }
    }

    private void analyzeFile(Path filePath, DefaultMutableTreeNode parentNode) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        FileOwnerAttributeView ownerView = Files.getFileAttributeView(filePath, FileOwnerAttributeView.class);
        UserPrincipal owner = ownerView.getOwner();

        String details = String.format("Size: %d bytes, Created: %s, Owner: %s",
                attrs.size(), new Date(attrs.creationTime().toMillis()), owner.getName());
        parentNode.add(new DefaultMutableTreeNode(details));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileSystemAnalyzer().setVisible(true));
    }
}