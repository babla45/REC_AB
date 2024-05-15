package com.example.rec_ab;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class multilineTextView extends AppCompatActivity {
    private TextView textView;
    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_multiline_text_view);


        textView = findViewById(R.id.textViewId);
        editText = findViewById(R.id.editTextId);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from the EditText
                String newText = editText.getText().toString();

                // Get the current text from the TextView
                String currentText = textView.getText().toString();

                // Append the new text with a newline character
                String updatedText = currentText + "\n" + newText;

                // Split the updated text into lines
                String[] lines = updatedText.split("\n");

                // Only keep the last 5 lines
                if (lines.length > 5) {
                    lines = java.util.Arrays.copyOfRange(lines, lines.length - 5, lines.length);
                }

                // Join the lines back into a single string
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }

                // Set the updated text back to the TextView
                textView.setText(sb.toString());

                // Clear the EditText
                editText.setText("");

                Toast.makeText(multilineTextView.this, "Button clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
