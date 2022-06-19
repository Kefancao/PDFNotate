package com.example.a2

import javafx.scene.input.KeyCode

interface IView {
    fun updateView();
    fun save() : String;
    fun load(shapes : List<String>);
    fun keyEventPass(key: KeyCode){}
    fun copySelected(){}
    fun pasteSelected(){}
}
