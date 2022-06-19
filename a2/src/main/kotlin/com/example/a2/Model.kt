package com.example.a2

import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

class Model{
    var views = ArrayList<IView>();
    var tool: SELECT = SELECT.SELECTION
    var lineSty : LINE = LINE.SOLID
    var selectedShape: Shape? = Rectangle();
    var borderColor: Color = Color.BLACK
    var insideFill: Color = Color.WHITE
    var lineWidth: Double = 3.0
    var clipboard = Clipboard.getSystemClipboard()
    var cmdDown = false;

    fun copySelected(){
        for (view in views){
            view.copySelected();
        }
    }

    fun pasteSelected(){
        for (view in views){
            view.pasteSelected();
        }
    }

    fun addView(view: IView){
        views.add(view);
        updateViews();
    }

    fun updateViews(){
        for (view in views){
            view.updateView()
        }

    }

    fun load(shapes: List<String>) {
        for (view in views){
            view.load(shapes);
        }
    }
    fun save(): String{
        var str = ""
        for (view in views){
            str += view.save()
        }
        return str
    }

    fun keyEventPass(key : KeyCode){
        for (view in views){
            view.keyEventPass(key);
        }
    }

}