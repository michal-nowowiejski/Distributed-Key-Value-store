package io.github.michalnowowiejski.distributedkv.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    public static Config load(String filePath) {
        try {
            return MAPPER.readValue(new File(filePath), Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config from: " + filePath, e);
        }
    }
    
}
