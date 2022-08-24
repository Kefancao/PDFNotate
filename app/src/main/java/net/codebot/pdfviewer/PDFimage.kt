package net.codebot.pdfviewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.ImageView
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.serialization.encodeToString
import kotlin.math.max
import kotlin.math.min

@Serializable
data class FloatPoint(val x: Float, val y: Float)


class UndoAction(var type: Int, var path: ArrayList<myPath?>? = null, var amountToRemove: Int = 1){}

@Serializable
data class myPath(val points: ArrayList<FloatPoint> = ArrayList<FloatPoint>(), val width: Float = 10.0F, val type: Int = 1): Path(){
    init{
        if (points.size > 0){
            super.moveTo(points[0].x, points[0].y)
        }
        for (i in 1..points.size - 2){
            super.lineTo(points[i].x, points[i].y)
        }
    }

    fun initLineTo(x: Float, y: Float){
        super.lineTo(x, y)
    }

    fun initMoveTo(x: Float, y: Float){
        super.moveTo(x, y);
    }

    override fun lineTo(x: Float, y: Float) {
        points.add(FloatPoint(x, y))
        super.lineTo(x, y)
    }

    override fun moveTo(x: Float, y: Float) {
        points.add(FloatPoint(x, y))
        super.moveTo(x, y)
    }
}

@SuppressLint("AppCompatCustomView")
class PDFimage  // constructor
    (context: Context?) : ImageView(context) {
    val LOGNAME = "pdf_image"

    // The following code is referenced from the cs349-public-repo/Android/PanZoom demo!
    // Scaling
    // we save a lot of points because they need to be processed
    // during touch events e.g. ACTION_MOVE
    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    var old_x1 = 0f
    var old_y1 = 0f
    var old_x2 = 0f
    var old_y2 = 0f
    var mid_x = -1f
    var mid_y = -1f
    var old_mid_x = -1f
    var old_mid_y = -1f
    var p1_id = 0
    var p1_index = 0
    var p2_id = 0
    var p2_index = 0

    var currPage = 0

    // --------- END-----------

    var pagesUndo = ArrayList<ArrayList<UndoAction>>()
    var pagesRedo = ArrayList<ArrayList<UndoAction>>()
    init{
        for (i in 1..28){
            pagesUndo.add(ArrayList<UndoAction>())
            pagesRedo.add(ArrayList<UndoAction>())
        }
    }


    var undoStack = pagesUndo[currPage]
    var redoStack = pagesRedo[currPage]
    // store cumulative transformations
    // the inverse matrix is used to align points with the transformations - see below
    var currentMatrix = Matrix()
    var inverse = Matrix()


    // the highlighter button

    // drawing path
    var path: myPath? = null
    var strokes = ArrayList<myPath?>()

    // Matrix to apply to the paths
    val transform: Double = 2.0
    // eraser mode
    var isErase = false
    var currType = 1

    var eraseArray = ArrayList<myPath?>()


    val undo = {
        if (undoStack.size > 0){
            // 1 for when the last action was a drawing.
            if (undoStack[undoStack.size - 1].type == 1){

                // Might be an issue here with the type. Check!!!
                var temp = ArrayList<myPath?>()
                // Goes through the number of drawing that was made in the last Action. This
                //  is done in case the last action was to redo an erase operation
                while (undoStack[undoStack.size - 1].amountToRemove > 0){
                    undoStack[undoStack.size - 1].amountToRemove -= 1
                    temp.add(strokes.removeAt(strokes.size - 1))
                }
                redoStack.add(UndoAction(2, temp))
                undoStack.removeAt(undoStack.size - 1)
            } else {
                Log.d(LOGNAME, "HERE Add action is reached")
                // For when the last action was erase, we delete the action containing the path
                //  and add it to the current items
                val toRemoveItems = undoStack.removeAt(undoStack.size - 1).path!!
                Log.d(LOGNAME, "There are a total of ${toRemoveItems.size} strokes being added back!")
                for (each in toRemoveItems){
                    strokes.add(each)
                }
                // To redo this, we would remove this from the strokes, so we pass it action 1.
                redoStack.add(UndoAction(1, amountToRemove = toRemoveItems.size))
            }
        }
    }

    val resetState = {
        currentMatrix = Matrix()
        inverse = Matrix()
    }

    val redo = {
        if (redoStack.size > 0){
            // When it's a drawing
            if (redoStack[redoStack.size - 1].type == 1){
                var temp = ArrayList<myPath?>()
                while (redoStack[redoStack.size - 1].amountToRemove > 0){
                    temp.add(strokes.removeAt(strokes.size - 1))
                    redoStack[redoStack.size - 1].amountToRemove -= 1
                }
                undoStack.add(UndoAction(2, temp))
                redoStack.removeAt(redoStack.size - 1)
            } else {
                // when it's an erase operation
                val toRemoveItems = redoStack.removeAt(redoStack.size - 1).path!!
                strokes.addAll(toRemoveItems)
                undoStack.add(UndoAction(1, amountToRemove = toRemoveItems.size))
            }
        }
    }

    // image to display
    var bitmap: Bitmap? = null
//    var paint = Paint(Color.BLUE)
    var solidPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10.0F
        alpha = 255
    }

    // Distance for finger entry
    var initDistance = 0.0;

    val saveCurrPage = {
        val filename = "str${currPage}.txt"
        val fileContents = Json.encodeToString(strokes);
        context!!.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
        strokes.clear()
    }

    val loadCurrPage = {
        // Temporary solution for bug.
//            context!!.deleteFile("str0.txt")
//            context!!.deleteFile("str1.txt")
//            context!!.deleteFile("str2.txt")
//            context!!.deleteFile("str3.txt")
//            context!!.deleteFile("str4.txt")
        context!!.openFileInput("str${currPage}.txt").bufferedReader().useLines { lines ->
            val content = lines.fold("") { some, text ->
                "$some\n$text"
            }
            strokes = Json.decodeFromString<ArrayList<myPath?>>(content)
            Log.d(LOGNAME, "${strokes.size}")
        }
        undoStack = pagesUndo[currPage]
        redoStack = pagesRedo[currPage]
        resetState();
    }

    var highlightPaint = Paint().apply {
        isAntiAlias = true
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 20.0F
        alpha = 90
    }


    val enableEraser = {
        isErase = true;
    }

    val swapPen = {
        isErase = false
        currType = 1
    }

    val swapHighlight = {
        isErase = false;
        currType = 0
    }

    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var inverted = floatArrayOf()
        when(event.pointerCount){
            1 -> {
                p1_id = event.getPointerId(0)
                p1_index = event.findPointerIndex(p1_id)

                // invert using the current matrix to account for pan/scale
                // inverts in-place and returns boolean
                inverse = Matrix()
                currentMatrix.invert(inverse)

                // mapPoints returns values in-place
                inverted = floatArrayOf(event.getX(p1_index), event.getY(p1_index))
                inverse.mapPoints(inverted)
                x1 = inverted[0]
                y1 = inverted[1]

                if (!isErase){
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            Log.d(LOGNAME, "Action down")
                            path = myPath(type=currType)
                            path!!.moveTo(x1, y1)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            Log.d(LOGNAME, "Action move")
                            path!!.lineTo(x1, y1)
                        }
                        MotionEvent.ACTION_UP -> {
                            Log.d(LOGNAME, "Action up")
                            strokes.add(path)
                            var temp = ArrayList<myPath?>()
                            temp.add(path)
                            undoStack.add(UndoAction(1, temp))
                            redoStack.clear()
                            Log.d(LOGNAME, strokes.size.toString());
                            path = myPath()
                        }
                    }
                }
                else {
                    val toRemove = ArrayList<Int>()
                    for (stroke in strokes) {
                        val rect = RectF()
                        stroke!!.computeBounds(rect, true);
                        if (rect.contains(x1, y1)) {
                            toRemove.add(strokes.indexOf(stroke))
                            break
                        }
                    }
                    for (i in toRemove){
                        eraseArray.add(strokes.removeAt(i));
                    }
                    if (event.action == MotionEvent.ACTION_UP && eraseArray.size > 0){
                        Log.d(LOGNAME, "THERE ARE ${eraseArray.size} many strokes being added to eraseArray")
                        val temp = ArrayList<myPath?>(eraseArray)
                        undoStack.add(UndoAction(2, temp))
                        eraseArray.clear()
                        redoStack.clear()
                    }
                }
            }
            2 ->{
                // The following code is referenced from the cs349-public-repo/Android/PanZoom demo!
                // point 1
                p1_id = event.getPointerId(0)
                p1_index = event.findPointerIndex(p1_id)

                // mapPoints returns values in-place
                inverted = floatArrayOf(event.getX(p1_index), event.getY(p1_index))
                inverse.mapPoints(inverted)

                // first pass, initialize the old == current value
                if (old_x1 < 0 || old_y1 < 0) {
                    x1 = inverted.get(0)
                    old_x1 = x1
                    y1 = inverted.get(1)
                    old_y1 = y1
                } else {
                    old_x1 = x1
                    old_y1 = y1
                    x1 = inverted.get(0)
                    y1 = inverted.get(1)
                }

                // point 2
                p2_id = event.getPointerId(1)
                p2_index = event.findPointerIndex(p2_id)

                // mapPoints returns values in-place
                inverted = floatArrayOf(event.getX(p2_index), event.getY(p2_index))
                inverse.mapPoints(inverted)

                // first pass, initialize the old == current value
                if (old_x2 < 0 || old_y2 < 0) {
                    x2 = inverted.get(0)
                    old_x2 = x2
                    y2 = inverted.get(1)
                    old_y2 = y2
                } else {
                    old_x2 = x2
                    old_y2 = y2
                    x2 = inverted.get(0)
                    y2 = inverted.get(1)
                }

                // midpoint
                mid_x = (x1 + x2) / 2
                mid_y = (y1 + y2) / 2
                old_mid_x = (old_x1 + old_x2) / 2
                old_mid_y = (old_y1 + old_y2) / 2

                // distance
                val d_old =
                    Math.sqrt(Math.pow((old_x1 - old_x2).toDouble(), 2.0) + Math.pow((old_y1 - old_y2).toDouble(), 2.0))
                        .toFloat()
                val d = Math.sqrt(Math.pow((x1 - x2).toDouble(), 2.0) + Math.pow((y1 - y2).toDouble(), 2.0))
                    .toFloat()

                // pan and zoom during MOVE event
                if (event.action == MotionEvent.ACTION_MOVE) {
                    Log.d(LOGNAME, "Multitouch move")
                    // pan == translate of midpoint
                    val dx = mid_x - old_mid_x
                    val dy = mid_y - old_mid_y
                    currentMatrix.preTranslate(dx, dy)
                    Log.d(LOGNAME, "translate: $dx,$dy")

                    // zoom == change of spread between p1 and p2
                    var scale = d / d_old
                    scale = Math.max(0f, scale)
                    currentMatrix.preScale(scale, scale, mid_x, mid_y)
                    Log.d(LOGNAME, "scale: $scale")

                    // reset on up
                } else if (event.action == MotionEvent.ACTION_UP) {
                    old_x1 = -1f
                    old_y1 = -1f
                    old_x2 = -1f
                    old_y2 = -1f
                    old_mid_x = -1f
                    old_mid_y = -1f
                }

            }
        }

        return true
    }

    // set image as background
    fun setImage(bitmap: Bitmap?) {
        this.bitmap = bitmap
    }

    // set brush characteristics
    // e.g. color, thickness, alpha

    override fun onDraw(canvas: Canvas) {
        canvas.concat(currentMatrix)
        // draw background
        if (bitmap != null) {
            setImageBitmap(bitmap)
        }
        // draw lines over it
        for (stroke in strokes) {
            if (stroke!!.type == 1){
                canvas.drawPath(stroke!!, solidPaint)
            } else{
                canvas.drawPath(stroke!!, highlightPaint)
            }
        }
        if (currType == 1){
            if (path != null){
                canvas.drawPath(path!!, solidPaint)
            }
        } else{
            if (path!= null){
                canvas.drawPath(path!!, highlightPaint)
            }
        }
        super.onDraw(canvas)
    }


}