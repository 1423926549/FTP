import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileChoice extends Application {
    public void start(Stage stage) {
        ImageView imgView = new ImageView();
        imgView.setFitWidth(20);
        imgView.setFitHeight(20);
        Menu file = new Menu("File");
        MenuItem item = new MenuItem("Open Multiple Files", imgView);
        file.getItems().addAll(item);
        //创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Multiple Files");
//        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"));
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"));
        //在菜单项上添加动作
        item.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                //打开对话框
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                for (File t : files) {
                    System.out.println("你选择的文件路径为：" + t.getPath());
                }
            }});
        //创建菜单栏并向其中添加菜单。
        MenuBar menuBar = new MenuBar(file);
        Group root = new Group(menuBar);
        Scene scene = new Scene(root, 595, 355, Color.BEIGE);
        stage.setTitle("File Chooser Example");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String args[]){
        launch(args);
    }
}