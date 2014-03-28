// compile with: javac -cp ../target/api-1.0-jar-with-dependencies.jar
// run with: javac -cp ../target/api-1.0-jar-with-dependencies.jar:. Predict <task> <collection> <file.txt>

import com.idibon.api.client.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;

class Predict {
    public static void main(String[] args) throws Exception {
        String collection_name = args[0];
        String task_name = args[1];
        String filename = args[2];
        String content = readFile(filename);

        Client api = new Client();

        HashMap<String, Object> params = new HashMap<String, Object>();
        ArrayList<HashMap<String, Object>> result;

        params.put("content", content);
        result = api.getPredictionsForText(collection_name, task_name, params);

        Gson jsonParser = new GsonBuilder().setPrettyPrinting().create();
        String body = jsonParser.toJson(result).toString();

        System.out.println(body);
    }

    public static String readFile(String filename) throws Exception {
        File file = new File(filename);
        FileInputStream is = new FileInputStream(file);
        byte[] data = new byte[(int)file.length()];
        is.read(data);
        is.close();
        return new String(data, "UTF-8");
    }
}

