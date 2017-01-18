package com.serli.oracle.of.bacon.loader.elasticsearch;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionLoader {
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Expecting 1 arguments, actual : " + args.length);
            System.err.println("Usage : completion-loader <actors file path>");
            System.exit(-1);
        }

        String inputFilePath = args[0];
        JestClient client = ElasticSearchRepository.createClient();
        ArrayList<Index> actorList = new ArrayList<Index>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            bufferedReader.lines()
                    .forEach(line -> {
                        if(actorList.size()<100000) {
                            actorList.add(new Index.Builder(line).build());
                            System.out.println(actorList.size());
                        }

                        else{

                            Bulk bulk = new Bulk.Builder()
                                    .defaultIndex("oracle-of-bacon")
                                    .defaultType("actor")
                                    .addAction(actorList)
                                    .build();

                            try {
                                client.execute(bulk);
                                System.out.println("client execute");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            actorList.clear();
                        }
                    });
        }

        System.out.println("Inserted total of " + count.get() + " actors");
    }
}
