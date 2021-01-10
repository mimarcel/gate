package com.example.gate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Gate {
    static final String gatePreferences = "gate";
    SharedPreferences sp;

    public String SETUP_ERROR;
    public Individual GATE_OWNER_INDIVIDUAL;
    public Individual GATE_INDIVIDUAL;
    public List<Individual> GATE_ALLOWED_INDIVIDUALS;

    public Gate(Context context) {
        sp = context.getSharedPreferences(gatePreferences, 0);
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            Map<String, Individual> individuals = getIndividuals(properties);
            GATE_OWNER_INDIVIDUAL = individuals.get(properties.getProperty("gate_owner_individual"));
            GATE_INDIVIDUAL = individuals.get(properties.getProperty("gate_individual"));
            String[] gateAllowedIndividuals = properties.getProperty("gate_allowed_individuals").split(",");
            GATE_ALLOWED_INDIVIDUALS = new ArrayList<>();
            for (String gateAllowedIndividual : gateAllowedIndividuals) {
                GATE_ALLOWED_INDIVIDUALS.add(individuals.get(gateAllowedIndividual));
            }
        } catch (Resources.NotFoundException e) {
            SETUP_ERROR = "Unable to find the config file";
            Log.e("Gate", SETUP_ERROR, e);
        } catch (IOException e) {
            SETUP_ERROR = "Failed to open config file";
            Log.e("Gate", SETUP_ERROR, e);
        } catch (Exception e) {
            SETUP_ERROR = "Unexpected error occurred";
            Log.e("Gate", SETUP_ERROR, e);
        }

        if (GATE_OWNER_INDIVIDUAL == null) {
            SETUP_ERROR = "gate_owner_individual configuration not set";
        }
        if (GATE_INDIVIDUAL == null) {
            SETUP_ERROR = "gate_individual configuration not set";
        }
    }

    protected Map<String, Individual> getIndividuals(Properties properties) {
        String individualsString = properties.getProperty("individuals");
        HashMap<String, Individual> individuals = new HashMap<>();
        for (String individualString : individualsString.split(";")) {
            String[] tokens = individualString.split(",");
            individuals.put(tokens[0], new Individual(tokens[0], tokens[1], tokens[2]));
        }

        return individuals;
    }

    public boolean isAutoCallEnabled() {
        String str = sp.getString("auto_call_enabled", "false");

        return str.equals("true");
    }

    @SuppressLint("ApplySharedPref")
    public void enableAutoCall(boolean enable) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("auto_call_enabled", enable ? "true" : "false");
        editor.commit();
    }

    public static Individual FindGuyByPhone(List<Individual> individuals, String phone) {
        if (individuals == null) {
            return null;
        }
        for (int i = 0; i < individuals.size(); i++) {
            Individual individual = individuals.get(i);
            if (individual.Phone.equals(phone)) {
                return individual;
            }
        }

        return null;
    }
}
