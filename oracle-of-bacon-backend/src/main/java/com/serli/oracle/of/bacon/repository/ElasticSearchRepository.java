package com.serli.oracle.of.bacon.repository;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Suggest;
import io.searchbox.core.SuggestResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticSearchRepository {

    private final JestClient jestClient;

    public ElasticSearchRepository() {
        jestClient = createClient();

    }

    public static JestClient createClient() {
        JestClient jestClient;
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
                .multiThreaded(true)
                .readTimeout(60000)
                .build());

        jestClient = factory.getObject();
        return jestClient;
    }

    public List<String> getActorsSuggests(String searchQuery) throws IOException {
        String json = "{\n" +
                "  \"actor\" : {\n" +
                "    \"text\" : \"" + searchQuery + "\",\n" +
                "    \"completion\" : {\n" +
                "      \"field\" : \"name_suggest\",\n" +
                "      \"fuzzy\" : {\"fuzziness\":1}\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Suggest suggest = new Suggest.Builder(json).build();

        SuggestResult result = jestClient.execute(suggest);
        List<String> list = new ArrayList<>();
        List<SuggestResult.Suggestion> suggestions = result.getSuggestions("actor");
        suggestions.get(0).options.forEach(x -> {
            list.add(((String)((Map<String,Object>)x.get("_source")).get("name")));
        });

        return list;
    }


}