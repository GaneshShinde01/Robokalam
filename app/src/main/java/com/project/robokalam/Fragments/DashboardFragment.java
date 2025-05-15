package com.project.robokalam.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.project.robokalam.Database.SQLiteHelper;
import com.project.robokalam.Model.PortfolioModel;
import com.project.robokalam.R;
import com.project.robokalam.databinding.FragmentDashboardBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SQLiteHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        Context context = getContext();
        if (context == null) {
            Toast.makeText(requireActivity(), "Context not available", Toast.LENGTH_SHORT).show();
            return view;
        }

        dbHelper = new SQLiteHelper(context);
        prefs = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        String email = prefs.getString("email", null);

        // Welcome message and button visibility setup
        if (email == null || email.isEmpty()) {
            binding.welCome.setText("Welcome!");
            binding.addPortfolioBtn.setVisibility(View.GONE);
        } else {
            String username = email.split("@")[0];
            binding.welCome.setText(String.format("Welcome, %s!", username));

            // Show Add Portfolio only for first-time users (tracked per email)
            boolean isFirstTime = prefs.getBoolean("isFirstTime_" + email, true);
            binding.addPortfolioBtn.setVisibility(isFirstTime ? View.VISIBLE : View.GONE);
        }

        // Fetch quote of the day asynchronously
        fetchQuote();

        binding.addPortfolioBtn.setOnClickListener(v -> showPortfolioDialog());

        return view;
    }

    private void showPortfolioDialog() {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_portfolio, null);

        // Custom dialog title styling
        TextView customTitle = new TextView(getContext());
        customTitle.setText("Add Portfolio");
        customTitle.setPadding(40, 40, 40, 40);
        customTitle.setTextSize(22);
        customTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.text));
        customTitle.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_title_bg));
        customTitle.setTypeface(null, Typeface.BOLD);

        // Get EditTexts from dialog layout
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtCollege = dialogView.findViewById(R.id.edtCollege);
        EditText edtSkill1 = dialogView.findViewById(R.id.edtSkill1);
        EditText edtSkill2 = dialogView.findViewById(R.id.edtSkill2);
        EditText edtSkill3 = dialogView.findViewById(R.id.edtSkill3);
        EditText edtProjectTitle = dialogView.findViewById(R.id.edtProjectTitle);
        EditText edtProjectDesc = dialogView.findViewById(R.id.edtProjectDesc);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCustomTitle(customTitle)
                .setView(dialogView)
                .setPositiveButton("Save", null)  // Override later to prevent auto dismiss
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        // Style buttons
        saveButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_save_bg));
        cancelButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_cancel_bg));
        saveButton.setTextColor(Color.GREEN);
        cancelButton.setTextColor(Color.RED);

        saveButton.setOnClickListener(v -> {
            String email = prefs.getString("email", null);
            if (email == null) {
                Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get user inputs with trimming
            String name = edtName.getText().toString().trim();
            String college = edtCollege.getText().toString().trim();
            String skill1 = edtSkill1.getText().toString().trim();
            String skill2 = edtSkill2.getText().toString().trim();
            String skill3 = edtSkill3.getText().toString().trim();
            String projectTitle = edtProjectTitle.getText().toString().trim();
            String projectDesc = edtProjectDesc.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty() || college.isEmpty() || skill1.isEmpty() || skill2.isEmpty() || skill3.isEmpty()
                    || projectTitle.isEmpty() || projectDesc.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String skills = skill1 + ", " + skill2 + ", " + skill3;

            PortfolioModel model = new PortfolioModel(email, name, college, skills, projectTitle, projectDesc);
            boolean inserted = false;

            try {
                inserted = dbHelper.insertPortfolio(model);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (inserted) {
                Toast.makeText(getContext(), "Portfolio Saved", Toast.LENGTH_SHORT).show();
                binding.welCome.setText("Welcome, " + name + "!");
                dialog.dismiss();

                // Update SharedPreferences so button won't show again for this user
                prefs.edit().putBoolean("isFirstTime_" + email, false).apply();

                // Hide the Add Portfolio button immediately
                binding.addPortfolioBtn.setVisibility(View.GONE);
            } else {
                Toast.makeText(getContext(), "Failed to save portfolio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchQuote() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("https://zenquotes.io/api/today");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONArray jsonArray = new JSONArray(result.toString());
                JSONObject quoteObj = jsonArray.getJSONObject(0);
                String quote = quoteObj.getString("q");
                String author = quoteObj.getString("a");
                String finalQuote = "\"" + quote + "\"\n\n~ " + author;

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> binding.quoteText.setText(finalQuote));
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch quote", Toast.LENGTH_SHORT).show()
                    );
                }
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // prevent memory leaks
    }
}
