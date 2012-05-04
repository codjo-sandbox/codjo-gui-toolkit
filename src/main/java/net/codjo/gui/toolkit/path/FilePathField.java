/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.gui.toolkit.path;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.codjo.gui.toolkit.fileChooser.FileChooserManager;
import net.codjo.gui.toolkit.i18n.InternationalizationUtil;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.gui.InternationalizableContainer;
import net.codjo.i18n.gui.TranslationNotifier;
import org.apache.log4j.Logger;
/**
 * Panel permettant de selectionner un fichier.
 */
public class FilePathField extends JPanel implements InternationalizableContainer {
    private static final Logger LOG = Logger.getLogger(FilePathField.class);
    private static final ImageIcon OPEN_ICON = new ImageIcon(FilePathField.class.getResource("folderOpen.png"));
    private static final ImageIcon SEARCH_ICON = new ImageIcon(FilePathField.class.getResource("search.png"));
    private JLabel fileLabel = new JLabel();

    private JTextField fileNameField = new JTextField();
    private JButton openFileButton = new JButton();
    private JButton selectButton = new JButton();
    private FileChooserManager fileChooserManager;

    private String defaultFolder = "";
    private boolean onlyInDefaultFolder;
    private Component parent;

    private String fileChooserTitle = "Selection du fichier";
    private String cantDisplayFileMessage = "Impossible d'afficher le fichier";
    private String inconsistentMessage = "Fichier non conforme !";
    private String badFileLocationMessage = "Le fichier doit se trouver dans le repertoire";


    public FilePathField() {
        this(null);
    }


    public FilePathField(Component parent) {
        this(parent, null, null);
    }


    public FilePathField(Component parent,
                         TranslationManager translationManager,
                         TranslationNotifier translationNotifier) {
        this.parent = parent;

        initGui();
        setTranslationBackpack(translationManager, translationNotifier);
    }


    public void setTranslationBackpack(TranslationManager translationManager, TranslationNotifier translationNotifier) {
        if (translationManager != null && translationNotifier != null) {
            InternationalizationUtil.registerBundlesIfNeeded(translationManager);
            translationNotifier.addInternationalizableContainer(this);

            fileChooserTitle = translationManager.translate("FilePathField.fileChooserTitle",
                                                            translationNotifier.getLanguage());
            fileChooserManager.setTitle(fileChooserTitle);

            cantDisplayFileMessage = translationManager.translate("FilePathField.cantDisplayFileMessage",
                                                                  translationNotifier.getLanguage());
            inconsistentMessage = translationManager.translate("FilePathField.inconsistentMessage",
                                                               translationNotifier.getLanguage());
            badFileLocationMessage = translationManager.translate("FilePathField.badFileLocationMessage",
                                                                  translationNotifier.getLanguage());
        }
    }


    public void addInternationalizableComponents(TranslationNotifier translationNotifier) {
        translationNotifier.addInternationalizableComponent(selectButton, null, "FilePathField.selectButton.tooltip");
        translationNotifier.addInternationalizableComponent(openFileButton,
                                                            null,
                                                            "FilePathField.openFileButton.tooltip");
    }


    private void initGui() {
        jbInit();
        fileNameField.getDocument().addDocumentListener(new FileNameListener());
        initFileChooserManager();
    }


    @Override
    public void setBackground(Color fg) {
        if (fileNameField == null) {
            super.setBackground(fg);
            return;
        }
        fileNameField.setBackground(fg);
    }


    public void setDefaultFolder(String defaultFolder) {
        this.defaultFolder = defaultFolder;
    }


    @Override
    public void setForeground(Color fg) {
        if (fileNameField == null) {
            super.setForeground(fg);
            return;
        }
        fileNameField.setForeground(fg);
    }


    public void setLabel(String label) {
        fileLabel.setText(label);
    }


    public void setLabelSize(int labelSize) {
        fileLabel.setPreferredSize(new Dimension(labelSize, fileLabel.getPreferredSize().height));
        this.invalidate();
    }


    public String getDefaultFolder() {
        return defaultFolder;
    }


    public File getFile() {
        if ("".equals(fileNameField.getText().trim())) {
            return null;
        }
        if (isOnlyInDefaultFolder()) {
            return new File(getDefaultFolder(), fileNameField.getText().trim());
        }
        else {
            return new File(fileNameField.getText().trim());
        }
    }


    @Override
    public void setName(String name) {
        fileNameField.setName(name + "." + fileNameField.getName());
        openFileButton.setName(name + "." + openFileButton.getName());
        selectButton.setName(name + "." + selectButton.getName());
        super.setName(name);
    }


    public void setFile(File newFile) {
        if (newFile == null) {
            fileNameField.setText("");
        }
        else {
            fileNameField.setText(newFile.getAbsolutePath());
        }
    }


    public JTextField getFileNameField() {
        return fileNameField;
    }


    public String getLabel() {
        return fileLabel.getText();
    }


    public int getLabelSize() {
        return fileLabel.getPreferredSize().width;
    }


    public boolean isOnlyInDefaultFolder() {
        return onlyInDefaultFolder;
    }


    public void setOnlyInDefaultFolder(boolean onlyInDefaultFolder) {
        this.onlyInDefaultFolder = onlyInDefaultFolder;
    }


    public boolean isWithAccessories() {
        return fileChooserManager.isWithAccessories();
    }


    public void setWithAccessories(boolean withAccessories) {
        fileChooserManager.setWithAccessories(withAccessories);
    }


    public void addExtensionFileFilter(String label, String extension) {
        fileChooserManager.addExtensionFileFilter(label, extension);
    }


    void openFile() {
        try {
            String[] args = {"cmd.exe", "/X", "/C", getFile().getAbsolutePath()};
            Process proc = Runtime.getRuntime().exec(args);
            Thread.sleep(1000);
            proc.destroy();
        }
        catch (Exception ex) {
            LOG.error(cantDisplayFileMessage, ex);
            ErrorDialog.show(this, cantDisplayFileMessage, ex);
        }
    }


    void selectFile() {
        String oldFile = fileNameField.getText();

        fileChooserManager.setDefaultFile(determineRootFolder());
        File file = fileChooserManager.showChooserForOpen(parent);

        String newFile = file == null ? "" : file.getAbsolutePath();
        if (file != null && isOnlyInDefaultFolder()) {
            // on v�rifie que le fichier se trouve dans le bon r�pertoire
            if (!isInDefaultFolder(newFile)) {
                ErrorDialog.show(this, inconsistentMessage,
                                 badFileLocationMessage + " " + getDefaultFolder());
                return;
            }
            newFile = newFile.substring(getDefaultFolder().length());
        }
        try {
            fireVetoableChange("fileName", toXml(oldFile), toXml(newFile));
            fileNameField.setText(newFile);
            firePropertyChange("fileName", oldFile, newFile);
        }
        catch (PropertyVetoException ex) {
            ErrorDialog.show(this, inconsistentMessage, ex.getMessage());
        }
    }


    private void initFileChooserManager() {
        fileChooserManager = new FileChooserManager();
        fileChooserManager.setTitle(fileChooserTitle);
        fileChooserManager.setSelectionMode(JFileChooser.FILES_ONLY);
    }


    private boolean isInDefaultFolder(String fileName) {
        return fileName.startsWith(getDefaultFolder());
    }


    private File determineRootFolder() {
        if (fileNameField.getText().length() > 0) {
            return getFile();
        }
        return new File(defaultFolder);
    }


    private void jbInit() {
        selectButton.setName("selectFileButton");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent event) {
                selectFile();
            }
        });
        selectButton.setBorder(BorderFactory.createEmptyBorder());
        selectButton.setPreferredSize(new Dimension(40, 21));
        selectButton.setMinimumSize(new Dimension(40, 21));
        selectButton.setIcon(SEARCH_ICON);

        openFileButton.setEnabled(false);
        openFileButton.setName("openFileButton");
        openFileButton.setBorder(BorderFactory.createEmptyBorder());
        openFileButton.setPreferredSize(new Dimension(40, 21));
        openFileButton.setMinimumSize(new Dimension(40, 21));
        openFileButton.setIcon(OPEN_ICON);
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openFile();
            }
        });
        this.setLayout(new GridBagLayout());
        fileLabel.setLabelFor(fileNameField);
        fileNameField.setEditable(false);
        fileNameField.setEnabled(true);
        fileNameField.setName("fileName");
        this.add(fileLabel,
                 new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                                        GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 0), 0, 0));
        this.add(fileNameField,
                 new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                        GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
        this.add(selectButton,
                 new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                        GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
        this.add(openFileButton,
                 new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    }


    private String toXml(String val) {
        return "".equals(val) ? "null" : val;
    }


    private class FileNameListener implements DocumentListener {
        public void changedUpdate(DocumentEvent evt) {
            if (isActivable()) {
                firePropertyChange("fileName", Boolean.TRUE, Boolean.FALSE);
            }
            openFileButton.setEnabled(isActivable());
        }


        public void insertUpdate(DocumentEvent evt) {
            if (isActivable()) {
                firePropertyChange("fileName", Boolean.TRUE, Boolean.FALSE);
            }
            openFileButton.setEnabled(isActivable());
        }


        public void removeUpdate(DocumentEvent evt) {
            if (isActivable()) {
                firePropertyChange("fileName", Boolean.TRUE, Boolean.FALSE);
            }
            openFileButton.setEnabled(isActivable());
        }


        private boolean isActivable() {
            File file = getFile();
            return file != null && file.exists() && file.isFile();
        }
    }
}
