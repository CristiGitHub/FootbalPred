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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {
    private Button button1, button2, button3;
    private TextView textViewNume11, textViewData11, textViewLiga11;
    private TextView textViewNume21, textViewNume12, textViewNume22, textViewData22, textViewLiga22;
    private TextView textViewNume13, textViewData13, textViewLiga13, textViewNume23;

    private static final String API_URL = "https://api.football-data.org/v4/matches?date=TOMORROW";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.matches);

        // Inițializare TextView-uri
        textViewNume11 = findViewById(R.id.textView_nume11);
        textViewData11 = findViewById(R.id.textView_data11);
        textViewLiga11 = findViewById(R.id.textView_liga11);
        textViewNume21 = findViewById(R.id.textView_nume21);
        textViewNume12 = findViewById(R.id.textView_nume12);
        textViewNume22 = findViewById(R.id.textView_nume22);
        textViewData22 = findViewById(R.id.textView_data22);
        textViewLiga22 = findViewById(R.id.textView_liga22);
        textViewNume13 = findViewById(R.id.textView_nume13);
        textViewData13 = findViewById(R.id.textView_data13);
        textViewLiga13 = findViewById(R.id.textView_liga13);
        textViewNume23 = findViewById(R.id.textView_nume23);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        // Setează listener pentru fiecare buton și trimite datele corecte
        button1.setOnClickListener(view -> openPopup(textViewNume11, textViewNume21, textViewLiga11));
        button2.setOnClickListener(view -> openPopup(textViewNume12, textViewNume22, textViewLiga22));
        button3.setOnClickListener(view -> openPopup(textViewNume13, textViewNume23, textViewLiga13));

        fetchDataFromAPI();
    }

    private void openPopup(TextView homeTextView, TextView awayTextView, TextView leagueTextView) {
        String homeTeam = homeTextView.getText().toString();
        String awayTeam = awayTextView.getText().toString();
        String championship = leagueTextView.getText().toString();

        Intent intent = new Intent(SecondActivity.this, PopupActivity.class);
        intent.putExtra("homeTeam", homeTeam);
        intent.putExtra("awayTeam", awayTeam);
        intent.putExtra("championship", championship);
        startActivity(intent);
    }

    private void fetchDataFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray matchesArray = response.getJSONArray("matches");
                            List<Match> matches = parseMatches(matchesArray);

                            if (!matches.isEmpty()) {
                                Match match1 = matches.get(0);
                                textViewNume11.setText(match1.getHomeTeam());
                                textViewData11.setText(match1.getUtcDate());
                                textViewLiga11.setText(match1.getCompetition());
                                textViewNume21.setText(match1.getAwayTeam());
                            }
                            if (matches.size() > 1) {
                                Match match2 = matches.get(1);
                                textViewNume12.setText(match2.getHomeTeam());
                                textViewNume22.setText(match2.getAwayTeam());
                                textViewData22.setText(match2.getUtcDate());
                                textViewLiga22.setText(match2.getCompetition());
                            }
                            if (matches.size() > 2) {
                                Match match3 = matches.get(2);
                                textViewNume13.setText(match3.getHomeTeam());
                                textViewData13.setText(match3.getUtcDate());
                                textViewLiga13.setText(match3.getCompetition());
                                textViewNume23.setText(match3.getAwayTeam());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SecondActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SecondActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Auth-Token", "6fba7c0832ac4faebebe1b7c6674244f");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private List<Match> parseMatches(JSONArray matchesArray) {
        List<Match> matchList = new ArrayList<>();
        try {
            for (int i = 0; i < matchesArray.length(); i++) {
                JSONObject matchJson = matchesArray.getJSONObject(i);
                Match match = new Match(
                        matchJson.getJSONObject("homeTeam").getString("name"),
                        matchJson.getJSONObject("awayTeam").getString("name"),
                        matchJson.getJSONObject("competition").getString("name"),
                        matchJson.getString("utcDate")
                );
                matchList.add(match);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return matchList;
    }

    class Match {
        private String homeTeam;
        private String awayTeam;
        private String competition;
        private String utcDate;

        public Match(String homeTeam, String awayTeam, String competition, String utcDate) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.competition = competition;
            this.utcDate = utcDate;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        public String getCompetition() {
            return competition;
        }

        public String getUtcDate() {
            return utcDate;
        }
    }
}
