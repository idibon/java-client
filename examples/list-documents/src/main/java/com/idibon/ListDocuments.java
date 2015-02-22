package com.idibon;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;

/**
 * Hello world!
 *
 */
public class ListDocuments
{
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.printf("Usage: %s API_KEY COLLECTION\n",
                              ListDocuments.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        for (Document doc : client.getCollection(args[1]).documents())
            System.out.printf("%s\n", doc.getJson().getString("name"));

        client.shutdown(0);
    }
}
