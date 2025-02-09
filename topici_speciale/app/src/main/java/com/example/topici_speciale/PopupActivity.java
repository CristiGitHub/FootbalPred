package com.example.topici_speciale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PopupActivity extends AppCompatActivity {

    private Button checkButton;
    private TextView textView;
    private static final String API_URL = "http://10.0.2.2:8000/predict/?home_team=t_h&away_team=t_a&championship=Campionat&h_bet=odd_h&x_bet=odd_x&a_bet=odd_a&";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);
        checkButton = findViewById(R.id.btnClosePopup);
        textView = findViewById(R.id.textPopup);
        View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PopupActivity.this, SecondActivity.class);
                    startActivity(intent);
                }
            };

        checkButton.setOnClickListener(listener);

        Intent intent = getIntent();
        String homeTeam = intent.getStringExtra("homeTeam");
        String awayTeam = intent.getStringExtra("awayTeam");
        String championship = intent.getStringExtra("championship");

        // Apelăm API-ul cu datele echipelor
        fetchDataFromAPI(homeTeam, awayTeam, championship);
        }

        private void fetchDataFromAPI(String home, String away, String campionat) {

            RequestQueue queue = Volley.newRequestQueue(this);
            double[] odds = generateOdds(home, away);
            String url = API_URL.replace("odd_h", String.valueOf(odds[0])).replace("odd_x", String.valueOf(odds[1])).replace("odd_a", String.valueOf(odds[2])).replace("t_h", home).replace("t_a", away).replace("Campionat", campionat);
//            System.out.println("Home Odds: " + odds[0] + ", Draw Odds: " + odds[1] + ", Away Odds: " + odds[2]);
//            textView.setText("test");
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            textView.setText(response); // Display the response
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(PopupActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(stringRequest);
        }
    public static int hash(String input) {
        return new BigInteger(input.getBytes(StandardCharsets.UTF_8)).mod(BigInteger.valueOf(100000)).intValue();
    }

    // Generate odds based on hashed team names
    public static double[] generateOdds(String homeTeam, String awayTeam) {
        int seed = hash(homeTeam + awayTeam);
        Random random = new Random(seed);

        // Generate base probabilities
        double homeWinProb = 0.3 + (random.nextDouble() * 0.4);  // 30% - 70%
        double drawProb = 0.2 + (random.nextDouble() * 0.3);     // 20% - 50%
        double awayWinProb = 1.0 - (homeWinProb + drawProb);     // Ensure sum = 1

        // Convert probabilities to decimal odds
        double homeOdds = 1 / homeWinProb * 1.05;  // Adjust for bookmaker margin
        double drawOdds = 1 / drawProb * 1.05;
        double awayOdds = 1 / awayWinProb * 1.05;

        return new double[]{round(homeOdds, 2), round(drawOdds, 2), round(awayOdds, 2)};
    }

    // Round to 2 decimal places
    public static double round(double value, int places) {
        return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
    }



}