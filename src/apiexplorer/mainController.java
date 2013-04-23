package apiexplorer;

import apiexplorer.util.TreeItemEx;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

/**
 *
 * @author quanlong
 */
public class mainController implements Initializable {
    
    private Set<String> searchSet;
    private ObservableList<String> searchList;
    private WebEngine webEngine;
    private WebHistory webHistory;
    
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
        webEngine.load(this.getClass().getResource("/api/index.html").toString());
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        Map<String, TreeItemEx> map = new HashMap<>();
        map.put("/", treeRoot);
        try {
            JarFile zip = new JarFile(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry next = entries.nextElement();
                String path = "/" + next.getName();
                if (path.indexOf("/api/") == -1) {
                    continue;
                }
                if (next.isDirectory()) {
                    String prePath = path.substring(0, path.lastIndexOf("/", path.lastIndexOf("/") - 1) + 1);
                    String tmp = path.substring(0, path.lastIndexOf("/"));
                    String name = tmp.substring(tmp.lastIndexOf("/") + 1);
                    TreeItemEx item = map.get(prePath);
                    TreeItemEx nextItem = new TreeItemEx(name, path, true);
                    item.getChildren().add(nextItem);
                    map.put(path, nextItem);
                } else {
                    String prePath = path.substring(0, path.lastIndexOf("/") + 1);
                    String name = path.substring(path.lastIndexOf("/") + 1);
                    TreeItemEx item = map.get(prePath);
                    if (name.lastIndexOf(".html") != -1) {
                        name = name.substring(0, name.lastIndexOf(".html"));
                    }
                    TreeItemEx nextItem = new TreeItemEx(name, path, false);
                    item.getChildren().add(nextItem);
                    map.put(path, nextItem);
                    if (!path.contains("class-use") && path.contains(".html")) {
                        searchSet.add(path.substring(0, path.lastIndexOf(".html")));
                    }
                }
            }
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(mainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleTreeViewAction(MouseEvent event) throws IOException {
        TreeItemEx item = (TreeItemEx)treeView.getSelectionModel().getSelectedItem();
        if (item.isDir()) {
            item.setExpanded(true);
        } else {
            String path = item.getPath();
            webEngine.load(this.getClass().getResource(path).toString());
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
