package org.dbpedia.databus.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public final class MossUtilityFunctions {


    public static String getValFromArray(String[] str_array) {
        if (str_array == null) {
            return "";
        } else {
            return str_array[0];
        }
    }

    public static int checkIfValidDatabusId (String databusIri) {
        String fileRegex = "^https://databus\\.dbpedia\\.org/[^\\/]+/[^/]+/[^/]+/[^/]+/[^/]+$";
        if (!databusIri.matches(fileRegex))
            return 0;
        URL url = null;
        try {
            url = new URL(databusIri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            int status = con.getResponseCode();
            if (status >= 200 && status < 400)
                return 1;
            else
                return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
