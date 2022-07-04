package com.example.a3

import javafx.animation.Interpolator
import javafx.animation.TranslateTransition
import javafx.geometry.Point2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration

val size = 50.0;

class Enemy(coorX: Double, coorY: Double, offX: Double, offY: Double, speed: Double = 7.0, val type: Int)
    : ImageView(){
    var isAlive = true;
    val transition = TranslateTransition(Duration.seconds(speed), this);
    init{
//        println("INITIATED AT ${x},${y}.")
        image = Image("enemy${type}.png")
        x = coorX * (size * 1.2) + offX
        y = coorY * size + offY
        fitHeight = size-10.0
        fitWidth = (size * 1.2) - 10.0
        setOnMouseClicked {
            isAlive = false;
        }
//        transition.interpolator = Interpolator.LINEAR
//        transition.fromX = 0.0
//        transition.toX = 1000.0
//        transition.isAutoReverse = true;
//        transition.cycleCount = TranslateTransition.INDEFINITE
//        transition.play()


    }

}