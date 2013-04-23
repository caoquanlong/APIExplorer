package apiexplorer.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author quanlong
 */
public class TreeItemEx extends TreeItem{
    
    private String path;
    private boolean isDir;

    public TreeItemEx() {
        super();
    }

    public TreeItemEx(Object t) {
        super(t);
    }

    public TreeItemEx(Object t, String path, boolean isDir) {
        super(t);
        this.path = path;
        this.isDir = isDir;
    }
    
    public TreeItem getChildren(String path) {
        ObservableList<TreeItemEx> children = this.getChildren();
        for (TreeItemEx item : children) {
            if (item.getPath().equals(path)) {
                return item;
            }
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setIsDir(boolean isDir) {
        this.isDir = isDir;
    }
}
