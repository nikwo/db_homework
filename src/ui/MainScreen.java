package ui;

import cfg.Config;
import db.DatabaseController;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import mediaplayer.MP3Player;
import parser.MediaDataParser;
import parser.MetaData;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class MainScreen extends Group {
    private Group screen;
    private GridPane grid;
    private FlowPane flow;
    private Label program_name;
    private Label track_name;
    private Stage stage;
    private List<File> file_list;
    private Config cfg;
    private TreeItem<String> category;
    private ListView<String> tracksListView;
    private MP3Player player;
    public MainScreen(Stage s, Config c){
        screen = new Group();
        grid = new GridPane();
        flow = new FlowPane();
        program_name = new Label("LiteMP3");
        track_name = new Label();
        stage = s;
        file_list = new ArrayList<>();
        cfg = c;
        category = new TreeItem<>();
        tracksListView  = new ListView<>();
        player = null;
    }
    public Group get_screen(){
        grid.setBackground(new Background(new BackgroundFill(Color.rgb(77, 69, 99), CornerRadii.EMPTY, Insets.EMPTY)));
        grid.setVgap(10);
        grid.setHgap(10);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setHgrow(Priority.ALWAYS);
        col0.setPercentWidth(25);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setPercentWidth(25);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setPercentWidth(25);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        col3.setPercentWidth(25);

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        col4.setPercentWidth(25);
        grid.setMaxWidth(400);

        grid.getColumnConstraints().addAll(col0, col1, col2, col3, col4);
        RowConstraints row0 = new RowConstraints();
        row0.setVgrow(Priority.NEVER);
        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.ALWAYS);
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.NEVER);
        grid.getRowConstraints().addAll(row0, row1, row2);

        program_name.setBackground(new Background(new BackgroundFill(Color.rgb(36, 30, 57), CornerRadii.EMPTY, Insets.EMPTY)));
        program_name.setTextFill(Color.web("#f57c00"));
        program_name.setFont(new Font("Georgia", 24));
        program_name.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(program_name, 0.0);
        AnchorPane.setLeftAnchor(program_name, 0.0);
        AnchorPane.setRightAnchor(program_name, 0.0);
        AnchorPane.setBottomAnchor(program_name, 0.0);

        track_name.setTextFill(Color.web("#f5c493"));
        track_name.setFont(new Font("Georgia", 12));
        track_name.setAlignment(Pos.TOP_CENTER);
        track_name.setText("Track name");
        track_name.setTextAlignment(TextAlignment.JUSTIFY);
        track_name.setMinHeight(40);
        track_name.setMaxWidth(Double.MAX_VALUE);

        Region track_image = new Region();
        track_image.setStyle("-fx-background-image: url('/ui/icons/no_picture.png'); "+
                "-fx-background-size: cover; ");

        Button menu_button = new Button("");
        menu_button.setBackground(new Background(new BackgroundFill(Color.rgb(108, 79, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        menu_button.setStyle("-fx-background-image: url('/ui/icons/menu_icon.png'); "+
                "-fx-background-size: cover; " + "-fx-background-color:transparent;");
        menu_button.setMinSize(80, 80);
        menu_button.setOnAction(actionEvent -> {
            DatabaseController db = new DatabaseController();
            ObservableList<String> t = FXCollections.observableArrayList();
            try {
                t = db.playlists();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Choose current playlist", t);
            choiceDialog.setContentText("playlist");
            Optional<String> result = choiceDialog.showAndWait();
            String entered = "";
            if(result.isPresent()){
                entered = result.get();
            }
            if(!entered.equals("")){
                cfg.set_current_playlist(entered);
                player.stopM();
                try {
                    set_current_playlist();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button prev_button = new Button("");
        prev_button.setBackground(new Background(new BackgroundFill(Color.rgb(108, 79, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        prev_button.setStyle("-fx-background-image: url('/ui/icons/prev_icon.png'); "+
                "-fx-background-size: cover; " + "-fx-background-color:transparent;");
        prev_button.setMinSize(80,80);

        Button next_button = new Button("");
        next_button.setBackground(new Background(new BackgroundFill(Color.rgb(108, 79, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        next_button.setStyle("-fx-background-image: url('/ui/icons/next_icon.png'); "+
                "-fx-background-size: cover; " + "-fx-background-color:transparent;");
        next_button.setMinSize(80,80);

        Button play_button = new Button("");
        play_button.setBackground(new Background(new BackgroundFill(Color.rgb(108, 79, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        play_button.setStyle("-fx-background-image: url('/ui/icons/play_icon.png'); "+
                "-fx-background-size: cover; " + "-fx-background-color:transparent;");
        play_button.setMinSize(80,80);

        Button cycle_button = new Button("");
        cycle_button.setBackground(new Background(new BackgroundFill(Color.rgb(108, 79, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        cycle_button.setStyle("-fx-background-image: url('/ui/icons/cycle_icon.png'); "+
                "-fx-background-size: cover; " + "-fx-background-color:transparent;");
        cycle_button.setMinSize(80,80);

        Button random_button = new Button("");
        random_button.setBackground(new Background(new BackgroundFill(Color.rgb(108, 79, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        random_button.setStyle("-fx-background-image: url('/ui/icons/random_icon.png'); "+
                "-fx-background-size: cover; " + "-fx-background-color:transparent;" + "-fx-background-position: center");
        random_button.setMinSize(80,80);

        track_image.setMaxWidth(440);
        track_image.setMaxHeight(580);

        grid.add(menu_button, 0, 0);
        grid.add(track_name, 1, 0, 3, 1);
        grid.add(track_image, 0, 1, 5, 1);
        grid.add(next_button, 3, 2);
        grid.add(play_button, 2, 2);
        grid.add(prev_button, 1, 2);
        grid.add(cycle_button, 0, 2);
        grid.add(random_button, 4, 2);

        flow.setLayoutY(0);
        flow.setLayoutX(440);
        flow.setBackground(new Background(new BackgroundFill(Color.rgb(36, 30, 57), CornerRadii.EMPTY, Insets.EMPTY)));
        flow.setMinWidth(600);
        flow.setMinHeight(670);
        flow.setOrientation(Orientation.VERTICAL);
        flow.setVgap(10);

        FlowPane bottom_playlist_menu = new FlowPane();
        bottom_playlist_menu.setOrientation(Orientation.HORIZONTAL);
        bottom_playlist_menu.setLayoutX(440);
        bottom_playlist_menu.setLayoutY(670);
        bottom_playlist_menu.setHgap(10);
        bottom_playlist_menu.setMinWidth(875);
        bottom_playlist_menu.setMinHeight(200);
        bottom_playlist_menu.setBackground(new Background(new BackgroundFill(Color.rgb(36, 30, 57), CornerRadii.EMPTY, Insets.EMPTY)));
        bottom_playlist_menu.setAlignment(Pos.TOP_CENTER);

        Button sort_button = new Button("");
        sort_button.setMinSize(80,80);
        sort_button.setStyle("-fx-background-image: url('/ui/icons/sort_icon.png'); " +
                "-fx-background-size: cover;" + "-fx-background-color:transparent;");

        Button add_button = new Button("");
        add_button.setStyle("-fx-background-image: url('/ui/icons/add.png'); " +
                "-fx-background-size: cover;" + "-fx-background-color:transparent;");
        add_button.setMinSize(80,80);
        flow.setHgap(10);


        tracksListView.setStyle("-fx-control-inner-background: \"#241E39\";" + "-fx-font-size: 18px;"
                + "-fx-font-family: Consolas;" + "-fx-background-color: black;");
        tracksListView.getStylesheets().add(getClass().getResource("listview.css").toExternalForm());

        add_button.setOnAction(actionEvent -> {
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(add_button.opacityProperty(), 0.1)),
                    new KeyFrame(new Duration(500),
                            new KeyValue(add_button.opacityProperty(), 1)));
            timeline.play();
            DirectoryChooser dir_choice = new DirectoryChooser();
            File dir = dir_choice.showDialog(stage);
            if (dir != null) {
                MediaDataParser parser = new MediaDataParser(dir.getAbsolutePath());
                parser.parse();
                Dialog dialog = new TextInputDialog("playlist");
                dialog.setTitle("set playlist name");
                dialog.setHeaderText("Enter name, or use default playlist name.");

                Optional<String> result = dialog.showAndWait();
                String entered = "none.";

                if (result.isPresent()) {

                    entered = result.get();
                }
                try {
                    insert_into_db(parser.getFile_list(), entered);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    insert_tracks_to_view(entered, tracksListView);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                cfg.set_current_playlist(entered);
                try {
                    cfg.write_cfg_state();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sort_button.setOnAction(actionEvent -> {
            configure_animation(sort_button);
            ObservableList<String> categories = FXCollections.observableArrayList("genre", "album", "group");
            ChoiceDialog<String> dialog = new ChoiceDialog<>("order by ...", categories);
            Optional<String> result = dialog.showAndWait();
            String entered = "none.";
            if (result.isPresent()) {
                entered = result.get();
            }
            if(!entered.equals("")){
                DatabaseController db = new DatabaseController();
                try {
                    List<MetaData> tracks = db.get_ordered_tracks(cfg.get_current_playlist(), entered);
                    ObservableList<String> t = FXCollections.observableArrayList();
                    for(MetaData track : tracks){
                        t.add(track.get_group_name()+" - "+track.get_track_name());
                    }
                    tracksListView.setItems(t);
                    player = new MP3Player(tracks);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        play_button.setOnAction(actionEvent -> {
            configure_animation(play_button);
            if(player.get_state() != MediaPlayer.Status.PLAYING) {
                if(player.get_state() == MediaPlayer.Status.PAUSED){
                    if(player.get_ind() == tracksListView.getSelectionModel().getSelectedIndex()) {
                        player.resumeM();
                        play_button.setStyle("-fx-background-image: url('/ui/icons/pause_icon.png'); " +
                                "-fx-background-size: cover;" + "-fx-background-color:transparent;");
                    }
                    else
                    {
                        int ind = tracksListView.getSelectionModel().getSelectedIndex();
                        if (ind < 0)
                            ind = 0;
                        player.play(ind, tracksListView, track_image, track_name);
                        play_button.setStyle("-fx-background-image: url('/ui/icons/pause_icon.png'); " +
                                "-fx-background-size: cover;" + "-fx-background-color:transparent;");
                    }
                }
                else {
                    int ind = tracksListView.getSelectionModel().getSelectedIndex();
                    if (ind < 0) {
                        ind = 0;
                        tracksListView.getSelectionModel().clearAndSelect(0);
                    }
                    player.play(ind, tracksListView, track_image, track_name);
                    play_button.setStyle("-fx-background-image: url('/ui/icons/pause_icon.png'); " +
                            "-fx-background-size: cover;" + "-fx-background-color:transparent;");
                }
            }
            else{
                play_button.setStyle("-fx-background-image: url('/ui/icons/play_icon.png'); " +
                        "-fx-background-size: cover;" + "-fx-background-color:transparent;");
                player.pause();
            }
        });

        next_button.setOnAction(actionEvent -> {
            configure_animation(next_button);
            if(player.get_state() == MediaPlayer.Status.PLAYING){
                player.next(tracksListView, track_image, track_name);
            }
        });

        prev_button.setOnAction(actionEvent -> {
            configure_animation(prev_button);
            if(player.get_state() == MediaPlayer.Status.PLAYING){
                player.prev(tracksListView, track_image, track_name);
            }
        });

        random_button.setOnAction(actionEvent -> {
            if(!player.get_rnd_flag()) {
                random_button.setOpacity(0.5);
                player.set_rnd_flag(true);
            }
            else{
                random_button.setOpacity(1);
                player.set_rnd_flag(false);
            }
        });

        cycle_button.setOnAction(actionEvent -> {
            if(!player.get_cycle_flag()) {
                cycle_button.setOpacity(0.5);
                player.set_cycle_flag(true);
            }
            else{
                cycle_button.setOpacity(1);
                player.set_cycle_flag(false);
            }
        });

        tracksListView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                int current = tracksListView.getSelectionModel().getSelectedIndex();
                player.play(current, tracksListView, track_image, track_name);
                play_button.setStyle("-fx-background-image: url('/ui/icons/pause_icon.png'); " +
                        "-fx-background-size: cover;" + "-fx-background-color:transparent;");
            }
        });

        tracksListView.setMinWidth(875);
        tracksListView.setMinHeight(670);
        bottom_playlist_menu.getChildren().add(add_button);
        bottom_playlist_menu.getChildren().add(sort_button);
        tracksListView.getSelectionModel().select(0);

        flow.getChildren().add(tracksListView);

        screen.getChildren().add(grid);
        screen.getChildren().add(flow);
        screen.getChildren().add(bottom_playlist_menu);

        return screen;
    }

    public void bind_grid_resize(Scene scene){
        grid.prefWidthProperty().bind(scene.widthProperty());
        grid.setAlignment(Pos.TOP_CENTER);
        grid.prefHeightProperty().bind(scene.heightProperty());
    }

    private void insert_tracks_to_view(String playlist, ListView<String> tracksListView) throws SQLException {
        DatabaseController db = new DatabaseController();
        List<MetaData> tracks = db.get_tracks(playlist);
        ObservableList<String> t = FXCollections.observableArrayList();
        for(MetaData track : tracks){
            t.add(track.get_group_name()+" - "+track.get_track_name());
        }
        tracksListView.setItems(t);
        player = new MP3Player(tracks);
    }

    public void set_stage(Stage s){
        stage = s;
    }

    private void insert_into_db(List<File> fl, String playlist) throws SQLException {
        file_list.addAll(fl);
        List<MetaData> md = new ArrayList<>();
        for(File f : file_list){
            md.add(new MetaData(f));
        }
        DatabaseController db = new DatabaseController();
        db.move_data_to_db(md, playlist);
    }

    public void set_current_playlist() throws SQLException, IOException {
        String playlist = cfg.get_current_playlist();
        if (!playlist.equals("NULL")) {
            insert_tracks_to_view(playlist, tracksListView);
            cfg.set_current_playlist(playlist);
            cfg.write_cfg_state();
        }
    }

    private void configure_animation(Button btn){
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(btn.opacityProperty(), 0.1)),
                new KeyFrame(new Duration(500),
                        new KeyValue(btn  .opacityProperty(), 1)));
        timeline.play();
    }

}
