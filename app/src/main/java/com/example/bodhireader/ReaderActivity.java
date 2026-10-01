package com.example.bodhireader;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ReaderActivity extends AppCompatActivity {
    private ImageView pdfImageView;
    private TextView tvPageNum;
    private Button btnPrevious, btnNext;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor fileDescriptor;
    private int pageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on while reading
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_reader);

        pdfImageView = findViewById(R.id.pdfImageView);
        tvPageNum = findViewById(R.id.tvPageNum);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        String bookName = getIntent().getStringExtra("BOOK_NAME");
        if (bookName != null) {
            setTitle(bookName.replace(".pdf", ""));
            openPdfFromAssets(bookName);
        }

        btnPrevious.setOnClickListener(v -> {
            if (pageIndex > 0) {
                pageIndex--;
                showPage(pageIndex);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (pdfRenderer != null && pageIndex < pdfRenderer.getPageCount() - 1) {
                pageIndex++;
                showPage(pageIndex);
            }
        });
    }

    private void openPdfFromAssets(String fileName) {
        try {
            File file = new File(getCacheDir(), fileName);
            if (!file.exists()) {
                InputStream asset = getAssets().open("books/" + fileName);
                FileOutputStream output = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            }

            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            if (fileDescriptor != null) {
                pdfRenderer = new PdfRenderer(fileDescriptor);
                showPage(pageIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) return;

        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(
                currentPage.getWidth() * 2,
                currentPage.getHeight() * 2,
                Bitmap.Config.ARGB_8888
        );

        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pdfImageView.setImageBitmap(bitmap);

        tvPageNum.setText((index + 1) + " / " + pdfRenderer.getPageCount());
        btnPrevious.setEnabled(index > 0);
        btnNext.setEnabled(index < pdfRenderer.getPageCount() - 1);
    }

    @Override
    protected void onDestroy() {
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (fileDescriptor != null) fileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}