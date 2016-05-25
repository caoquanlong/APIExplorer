package apiexplorer;

import apiexplorer.util.IOUtils;
import apiexplorer.util.TreeItemEx;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipFile;

/**
 *
 * @author quanlong
 */
public class mainController implements Initializable {
    
    private Set<String> searchSet;
    private ObservableList<String> searchList;
    private WebEngine webEngine;
    private WebHistory webHistory;
    private File apiRootDir = new File("./api");
    
    @FXML
    private TreeView treeView;
    @FXML
    private TreeItemEx treeRoot;
    @FXML
    private WebView webView;
    @FXML
    private TextField txtSearch;
    @FXML
    private ListView listSearch;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnForward;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        searchSet = new HashSet<>();
        searchList = FXCollections.observableArrayList();
        listSearch.setItems(searchList);
        webEngine = webView.getEngine();
        webHistory = webEngine.getHistory();
        webHistory.setMaxSize(10);
//        webEngine.load(this.getClass().getResource("/api/index.html").toString());
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        Map<String, TreeItemEx> map = new HashMap<>();
        map.put("/", treeRoot);
        apiRootDir.mkdir();
        initTree(apiRootDir);
    }

    private void initTree(File rootDir) {
        File[] files = rootDir.listFiles();
        treeRoot.getChildren().clear();
        buildTreeItem(treeRoot, files);
    }

    private void buildTreeItem(TreeItemEx parentItem, File[] files) {
        for (File f : files) {
            if (f.isDirectory()) {
                String name = f.getName();
                String path = f.getAbsolutePath();
                TreeItemEx nextItem = new TreeItemEx(name, path, true);
                parentItem.getChildren().add(nextItem);
                File[] f1 = f.listFiles();
                buildTreeItem(nextItem, f1);
            } else {
                String name = f.getName();
                int pos = name.lastIndexOf('.');
                if (pos != -1) {
                    name = name.substring(0, pos);
                }
                searchSet.add(name);
                String path = f.getAbsolutePath();
                TreeItemEx nextItem = new TreeItemEx(name, path, false);
                parentItem.getChildren().add(nextItem);
            }
        }
    }

    @FXML
    private void handlerBtnOpenAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;
        File curJarApiRoot = new File(apiRootDir, file.getName());
        try {
            ZipFile zipFile = new ZipFile(file);
            zipFile.stream().forEach(zipEntity -> {
                FileWriter writer = null;
                try {
                    if (zipEntity.isDirectory()) {
                        File p = new File(curJarApiRoot, zipEntity.getName());
                        p.mkdirs();
                    } else {
                        String name = zipEntity.getName();
                        File f = new File(curJarApiRoot, name);
                        InputStream in = zipFile.getInputStream(zipEntity);
                        String text = IOUtils.toString(in, "UTF-8");
                        writer = new FileWriter(f);
                        writer.write(text);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        initTree(apiRootDir);
    }
    
    @FXML
    private void handleTreeViewAction(MouseEvent event) throws IOException {
        TreeItemEx item = (TreeItemEx)treeView.getSelectionModel().getSelectedItem();
        if (item == null) return;
        if (item.isDir()) {
//            item.setExpanded(true);
        } else {
            String path = item.getPath();
            File file = new File(path);
            String url = file.toURI().toURL().toString();
            webEngine.load(url);
        }
    }
    
    @FXML
    private void handleTxtSearchKeyAction(KeyEvent event) {
        String txt = txtSearch.getText();
        searchList.clear();
        Iterator<String> iterator = searchSet.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.toLowerCase().contains(txt.toLowerCase())) {
                searchList.add(next);
            }
        }
    }
    
    @FXML
    private void handleListViewKeyAction(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String item = (String) listSearch.getSelectionModel().getSelectedItem();
            webEngine.load(this.getClass().getResource(item + ".html").toString());
        }
    }
    
    @FXML
    private void handleListViewMouseAction(MouseEvent event) {
        if (event.getClickCount() > 1) {
            String item = (String) listSearch.getSelectionModel().getSelectedItem();
            webEngine.load(this.getClass().getResource(item + ".html").toString());
        }
    }
    
    @FXML
    private void handleBtnBackAction(ActionEvent event) {
        if (webHistory.getCurrentIndex() - 1 > 0) {
            webHistory.go(-1);
        }
    }
    
    @FXML
    private void handleBtnForwardAction(ActionEvent event) {
        if (webHistory.getCurrentIndex() + 1 < webHistory.getEntries().size()) {
            webHistory.go(1);
        }
    }
}
