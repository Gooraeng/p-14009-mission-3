package com.back.domain.wiseSaying.controller;

import com.back.domain.wiseSaying.types.CommandType;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public class Request {

    private final CommandType commandType;
    private final Map<String, String> parameters;

    public Request(String command) {
        String[] commandParts = command.split("\\?", 2);

        commandType = CommandType.valueOf(commandParts[0]);

        if (commandParts.length == 1) {
            parameters = null;
            return;
        }

        parameters = Arrays.stream(commandParts[1].split("&"))
                .map(param -> param.split("=", 2))
                .filter(pair ->
                        pair.length == 2 && !pair[0].isEmpty() && !pair[1].isEmpty()
                )
                .collect(Collectors.toMap(
                        pair -> pair[0],
                        pair -> pair[1])
                );
    }

    public CommandType getCommand() {
        return commandType;
    }

    public String getParamValue(String key) {
        return parameters.getOrDefault(key, null);
    }

    public String getParamValue(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    public int getParamValue(String key, int defaultValue) {
        String value = getParamValue(key, "");

        if (value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
}
