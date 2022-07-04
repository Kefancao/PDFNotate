package com.example.a3

import javafx.animation.AnimationTimer
import javafx.scene.input.KeyCode
import javafx.scene.layout.Pane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlin.collections.ArrayList
import kotlin.math.abs

class GameView(private val model: SpaceInvader, private var level: Int = 1): Pane(), IView {
    // Change 'bulletRate' to 0 if you'd like the ability to spam bullets.
    var bulletRate = 30;
    val classLoader = Thread.currentThread().contextClassLoader
    val alienList = ArrayList<Enemy>();
    val ship = Ship(750.0, 1070.0);
    var bulletList = ArrayList<PlayerBullet>();
    var alienBullets = ArrayList<AlienBullet>();
    // Starting position for the group of aliens
    val offsetY = 10.0
    val offsetX = 450.0
//     direction also encodes the speed
    var direction = 2.0 + 2 * level;
    // Ship speed and direction
    var shipDir = 0;

    // Decrements if positive until it goes to 0, cannot fire bullets until this becomes 0.
    var timeSinceLastBullet = 0;
    // Clock for the player to freely move around once it's hit.
    var moverClock = 300;

    var gameStarted = false;

    fun populateEnemy(){
        // Generates about 50 aliens.
        for (i in 1..10){
            for (j in 1..5){
                alienList.add(Enemy(i + 0.0, j + 0.0, offsetX, offsetY, type = j.mod(3) + 1))
            }
        }
        updateView()
    }

    override fun updateView() {
        children.clear();
        children.add(ship)
        for (alien in alienList){
            if (alien.isAlive){
                children.add(alien);
            }
        }
        for (bullet in bulletList){
            children.add(bullet);
        }
        for (ab in alienBullets){
            children.add(ab);
        }
    }

    val timer = object : AnimationTimer(){
        override fun handle(now: Long){
            // Aliens have been exhausted, either move on to next level or finish game.
            if (alienList.isEmpty()){
                nextLevel();
                return;
            }
            if (ship.x + shipDir + ship.fitWidth < 1600.0 && ship.x + shipDir > 0.0){
                ship.x += shipDir
            }

            if (moverClock > 0){
                moverClock -= 1
                ship.opacity = moverClock.mod(20)/19.0
                if (moverClock == 0){
                    ship.opacity = 1.0
                }
                return;
            }

            var altered = false
            if (timeSinceLastBullet > 0){
                timeSinceLastBullet -= 1;
            }
            // Created a temporary variable in case, so it doesn't affect the direction of the aliens being updated now
            var fire = (0..69).random() == 69;
            var idx = (0 until alienList.size).random()
            if (fire && alienBullets.size < 10){
                val abullet = AlienBullet(alienList[idx].x, alienList[idx].y, speed = abs(direction), type = alienList[idx].type)
                alienBullets.add(abullet)
                children.add(abullet)
            }
            // Move the aliens
            for (alien in alienList){
                alien.x += direction;
            }

            // Check for edge hitting
            for (alien in alienList){
                // If the aliens go out of bounds in either direction and the direction has not yet been changed.
                if (((alien.x + alien.fitWidth > 1590) || (alien.x < 10)) && !altered){
                    direction *= -1;
                    altered = true;
                    val edgebullet = AlienBullet(alienList[idx].x, alienList[idx].y, type = alienList[idx].type)
                    alienBullets.add(edgebullet)
                    children.add(edgebullet)
                    break;
                }
                if (alien.boundsInParent.intersects(ship.boundsInParent)){
                    MediaPlayer(Media(classLoader.getResource("explosion.wav")?.toString())).play()
                    model.lives -= 1;
                    model.updateLives()
                    model.checkGameOver();
                    moverClock = 300;
                }
            }
            // If we hit the edge, then make the aliens go down a row.
            if (altered){
                for (alien in alienList){
                    alien.y += alien.fitHeight;
                    if (alien.y > 1100.0){
                        model.lives = 0
                        model.endGame();
                    }
                }

            }
            // Check to remove bullet from bullet list if they go out of
            //   sight. Add to a list to keep track of indices.
            val toRemove = ArrayList<Int>()
            for (bullet in bulletList){
                bullet.update()
                for (enemy in alienList){
                    if (bullet.boundsInParent.intersects(enemy.boundsInParent)){
                        bullet.y = -20.0;
                        MediaPlayer(Media(classLoader.getResource("invaderkilled.wav")?.toString())).play()
                        // Changing by a small amount since we don't want the game to become impossible.
                        //  This will bound the alien speed by level hardness + 1 at most.
                        direction += 0.02
                        children.remove(enemy);
                        alienList.remove(enemy);
                        model.score += level
                        model.updateScore();
                        break;
                    }
                }
                // Bullet to Alien Bullet collision checker
                for (ab in alienBullets){
                    // If there is a match, then remove the bullet from the bullet list
                    //   and delete the alien bullet.
                    if (bullet.boundsInParent.intersects(ab.boundsInParent)){
                        bullet.y = -20.0;
                        MediaPlayer(Media(classLoader.getResource("explosion.wav")?.toString())).play()
                        children.remove(ab);
                        alienBullets.remove(ab);
                        break;
                    }
                }

            }
            for (ab in alienBullets){
                ab.y += 7
            }

            for (ab in alienBullets){
                // Collision between the ship and alien Bullet.
                if (ab.boundsInParent.intersects(ship.boundsInParent)){
                    children.remove(ship);
                    alienBullets.remove(ab);
                    children.remove(ab);
                    MediaPlayer(Media(classLoader.getResource("explosion.wav")?.toString())).play();
                    model.lives -= 1;
                    moverClock = 300;
                    model.updateLives();
                    model.checkGameOver();
                    break;
                }
            }
            alienBullets = alienBullets.filter { it.y < 1200.0 } as ArrayList<AlienBullet>
            bulletList = bulletList.filter { it.y > 0.0 } as ArrayList<PlayerBullet>
            updateView()
        }
    }

    init{
        style = "-fx-background-color: #141414"
        populateEnemy()

    }

    fun activate(){
        timer.start()
    }

    fun nextLevel(){
        timer.stop();
        model.nextLevel();

    }

    fun releaseKey(code: KeyCode){
        if (code == KeyCode.LEFT || code == KeyCode.RIGHT || code == KeyCode.A || code == KeyCode.D){
            shipDir = 0;
        }
    }
    fun handleKey(code: KeyCode){
        if (!gameStarted){
            gameStarted = true;
            activate()
            return
        }
        if (code == KeyCode.SPACE && timeSinceLastBullet == 0){
            if (gameStarted){
                moverClock = 0
                ship.opacity = 1.0
                MediaPlayer(Media(classLoader.getResource("shoot.wav")?.toString())).play()
                bulletList.add(PlayerBullet(ship.x + ship.fitWidth/2, ship.y))
                timeSinceLastBullet = bulletRate;
                updateView();
            }
        } else if (code == KeyCode.A || code == KeyCode.LEFT){
            shipDir = -4;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT){
            shipDir = 4;
        }
        ship.handleKey(code);
    }

}