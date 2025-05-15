package com.project.robokalam.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.robokalam.Database.SQLiteHelper;
import com.project.robokalam.Model.PortfolioModel;
import com.project.robokalam.R;
import com.project.robokalam.databinding.FragmentPortfolioBinding;

import java.util.List;

public class PortfolioFragment extends Fragment {

    private FragmentPortfolioBinding binding;
    SQLiteHelper dbHelper;

    public PortfolioFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_portfolio, container, false);
        binding = FragmentPortfolioBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        dbHelper = new SQLiteHelper(getContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String loggedInEmail = prefs.getString("email", null);

            if(loggedInEmail != null) {


                PortfolioModel portfolio = dbHelper.getPortfolioByEmail(loggedInEmail);

                if (portfolio != null) {
                    binding.txtName.setText("Name: " + portfolio.getName());
                    binding.txtCollege.setText("College: " + portfolio.getCollege());
                    binding.txtSkills.setText("Skills: " + portfolio.getSkills());
                    binding.txtProjectTitle.setText("Project Title: " + portfolio.getProjectTitle());
                    binding.txtProjectDesc.setText("Project Description: " + portfolio.getProjectDescription());
                } else {
                    // No portfolio data found, you can clear fields or show a message
                    binding.txtName.setText("Name: N/A");
                    binding.txtCollege.setText("College: N/A");
                    binding.txtSkills.setText("Skills: N/A");
                    binding.txtProjectTitle.setText("Project Title: N/A");
                    binding.txtProjectDesc.setText("Project Description: N/A");
                }

            }



        return view;

    }
}