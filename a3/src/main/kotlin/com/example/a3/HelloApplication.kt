package com.example.a3

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage


class HelloApplication : Application() {
    override fun start(stage: Stage) {

        val model = Model();
//        val welcomePage = WelcomeView(model);
//        val gamePage = GameView(model)
//        val levels = ArrayList<GameView>()
//        val GameBorder = BorderPane();
//        var currLevel = 1;
//        GameBorder.center = gamePage
//
//
        val rootGame = Scene(model, 1600.0, 1200.0)
//        val rootWelcome = Scene(welcomePage, 1600.0, 1200.0)
//
//        val startGame = {
//            stage.scene = rootGame
//        }
//
//        val endGame = {}
//
//        val nextLevel = {
//            currLevel += 1;
//            if (currLevel == 3){
//                endGame();
//            } else{
//                GameBorder.center = GameView(model, currLevel);
//            }
//        }
//
        rootGame.setOnKeyPressed {
            model.keyPress(it.code)
        }
//
//        rootWelcome.setOnKeyPressed {
//            if (it.code == KeyCode.SPACE){
//                startGame();
//            }
//        }
//
        rootGame.setOnKeyReleased {
            model.keyRelease(it.code);
        }


        stage.title = "Hello!"
        stage.scene = rootGame
        stage.isResizable = false;
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}