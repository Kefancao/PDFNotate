package com.example.a3

import javafx.scene.image.Image
import javafx.scene.image.ImageView

class PlayerBullet (coorX: Double, coorY: Double, private val speed: Double = 7.0)
    : ImageView(){


        init{
            image = Image("player_bullet.png")
            fitHeight = 20.0
            fitWidth = 10.0
            // To adjust for bullet width.
            x = coorX - fitWidth / 2;
            // Just to adjust for the bullet height.
            y = coorY + fitHeight + 5.0;
        }

    // Returns false if it goes off the border
    fun update(): Boolean{
        y -= speed;
        if (y + fitHeight < 0){
            return false;
        }
        return true;
    }



}
