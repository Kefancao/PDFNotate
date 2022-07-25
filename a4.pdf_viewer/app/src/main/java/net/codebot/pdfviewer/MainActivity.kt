package net.codebot.pdfviewer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min


// onStop calls closeRenderer, which calls the page.close() method causing an error.
// TO DO
// - Get clarification on the above matter ^^
// - Allow the undo/redo stack to work when switching between pages.
//     - could do this with an array containing the undo/redo stacks for each page.
// - Get orientation right
// - Fix panning/zooming limit.

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provide this code.
class MainActivity : AppCompatActivity() {
    val LOGNAME = "pdf_viewer"
    val FILENAME = "shannon1948.pdf"
    val FILERESID = R.raw.shannon1948

//    var scaleFactor = 1.0f

    // manage the pages of the PDF, see below
    lateinit var pdfRenderer: PdfRenderer
    lateinit var parcelFileDescriptor: ParcelFileDescriptor

    // Scale Gesture Detection
    lateinit var scaleGestureDetector: ScaleGestureDetector

    var currentPage: PdfRenderer.Page? = null

    // custom ImageView class that captures strokes and draws them over the image
    lateinit var pageImage: PDFimage

    var currPageNum = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layout = findViewById<LinearLayout>(R.id.pdfLayout)
        layout.isEnabled = true

        pageImage = PDFimage(this)
        layout.addView(pageImage)
        pageImage.minimumWidth = 1000
        pageImage.minimumHeight = 2000

        // Highlighter switch
        highlight.setOnClickListener {
            pageImage.swapHighlight();
        }

        solidPen.setOnClickListener {
            pageImage.swapPen();
        }

        // Next and Previous Page Action
        nextPage.setOnClickListener {
            try{
                pageImage.currPage = currPageNum;
                pageImage.saveCurrPage();
                currPageNum += 1
                openRenderer(this)
                showPage(currPageNum)
                pageNumber.text = "${currPageNum + 1}/28"
                pageImage.currPage = currPageNum;
                pageImage.loadCurrPage();
                closeRenderer()
            } catch (exception: IOException){
                Log.d(LOGNAME, "Error opening PDF")
            }
        }

        undo.setOnClickListener{
            pageImage.undo()
        }

        redo.setOnClickListener {
            pageImage.redo()
        }

        previousPage.setOnClickListener {
            try{
                pageImage.currPage = currPageNum
                pageImage.saveCurrPage();
                if (currPageNum != 0){
                    currPageNum -= 1;
                }
                pageNumber.text = "${currPageNum + 1}/28"
                openRenderer(this)
                showPage(currPageNum)
                pageImage.currPage = currPageNum
                pageImage.loadCurrPage();
                closeRenderer()
            } catch (exception: IOException){
                Log.d(LOGNAME, "Error opening PDF")
            }
        }

        eraser.setOnClickListener {
            pageImage.enableEraser();
        }

        pageNumber.text = "${currPageNum + 1}28"

        // open page 0 of the PDF
        // it will be displayed as an image in the pageImage (above)
        try {
            openRenderer(this)
            showPage(currPageNum)
            // Loads the current page
            pageImage.currPage = currPageNum
            pageImage.loadCurrPage();
            closeRenderer()
        } catch (exception: IOException) {
            Log.d(LOGNAME, "Error opening PDF")
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            pageImage.saveCurrPage()
//            closeRenderer()
        } catch (ex: IOException) {
            Log.d(LOGNAME, "Unable to close PDF renderer")
        }
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context) {
        // In this sample, we read a PDF from the assets directory.
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            val asset = this.resources.openRawResource(FILERESID)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size: Int
            while (asset.read(buffer).also { size = it } != -1) {
                output.write(buffer, 0, size)
            }
            asset.close()
            output.close()
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
    }

    // do this before you quit!
    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage?.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }

    private fun showPage(index: Int) {
        Log.d(LOGNAME, "${pdfRenderer.pageCount} In total ")
        if (pdfRenderer.pageCount <= index) {
            return
        }
        // Close the current page before opening another one.
//        currentPage?.close()

        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index)

        if (currentPage != null) {
            // Important: the destination bitmap must be ARGB (not RGB).
            val bitmap = Bitmap.createBitmap(currentPage!!.getWidth(), currentPage!!.getHeight(), Bitmap.Config.ARGB_8888)

            // Here, we render the page onto the Bitmap.
            // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
            currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Display the page

            pageImage.setImage(bitmap);
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pageImage
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(baseContext, "Portrait Mode", Toast.LENGTH_SHORT).show()
        }
    }

}