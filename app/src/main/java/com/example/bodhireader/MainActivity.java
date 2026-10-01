package com.example.bodhireader;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List books = getAssetsPdfList();
        BookAdapter adapter = new BookAdapter(books, fileName -> {
            Intent intent = new Intent(MainActivity.this, ReaderActivity.class);
            intent.putExtra("BOOK_NAME", fileName);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private List getAssetsPdfList() {
        try {
            String[] files = getAssets().list("books");
            if (files != null) {
                return Arrays.asList(files);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}