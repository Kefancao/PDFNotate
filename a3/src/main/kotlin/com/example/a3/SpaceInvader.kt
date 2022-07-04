package com.example.a3

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font

class SpaceInvader : BorderPane(){
    val welcomePage = WelcomeView(this);
    var gamePage = GameView(this)
    var playing = false
    var gameOver = false;
    var score = 0
    var level = 1
    var lives = 3
    val scorel = Label("Score: 0")
    val levell = Label("Level: 1")
    val livesl = Label("Lives: 3")
    val topInfo = HBox()

    init{
        scorel.font = Font.font(30.0)
        scorel.textFill = Color.WHITE
        levell.font = Font.font(30.0)
        levell.textFill = Color.WHITE
        livesl.font = Font.font(30.0)
        livesl.textFill = Color.WHITE

        topInfo.children.addAll(scorel, levell, livesl)
        topInfo.padding = Insets(15.0)
        topInfo.spacing = 50.0;
        topInfo.style = "-fx-background-color: #141414"
        center = welcomePage
    }

    fun updateScore(){
        scorel.text = "Score: $score"
    }

    fun updateLives(){
        livesl.text = "Lives: $lives"
    }

    fun updateLevel(){
        levell.text = "Level: $level"
    }

    val endGame = {
        center = EndView(score, lives > 0);
        gameOver = true;
        gamePage = GameView(this, 1)
        top = null
    }

    val nextLevel = {
        level += 1;
        updateLevel()
        if (level > 3){
            endGame();
        } else{
            gamePage = GameView(this, level);
            center = gamePage
        }
    }

    val checkGameOver = {
        if (lives == 0){
            endGame()
        }
    }

    fun keyPress(code : KeyCode){
        if (gameOver){
            if (code == KeyCode.R){
                gameOver = false
                lives = 3
                score = 0
                level = 1
                playing = false
                gamePage = GameView(this, level)
                center = welcomePage
            } else if (code == KeyCode.Q){
                Platform.exit()
            }
            return
        }
        if (playing){
            gamePage.handleKey(code);
            return
        } else if (code == KeyCode.ENTER && !playing){
            playing = true
            center = gamePage;
            top = topInfo
        } else if (code == KeyCode.Q && !playing){
            Platform.exit()
        }
        else if (code == KeyCode.DIGIT1 && !playing){
            playing = true
            center = gamePage;
            top = topInfo
        } else if (code == KeyCode.DIGIT2 && !playing){
            playing = true
            level = 2;
            gamePage = GameView(this, level)
            center = gamePage
            top = topInfo
        } else if (code == KeyCode.DIGIT3 && !playing){
            playing = true;
            level = 3;
            gamePage = GameView(this, level);
            center = gamePage
            top = topInfo
        }
        updateLevel();
        updateScore();
        updateLives();
    }

    fun keyRelease(code: KeyCode){
        if (playing){
            gamePage.releaseKey(code);
        }
    }

    init{

    }

}