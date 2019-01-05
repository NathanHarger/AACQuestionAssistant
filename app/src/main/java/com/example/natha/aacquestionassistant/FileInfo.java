package com.example.natha.aacquestionassistant;

public class FileInfo {
    int id;
    String symbol;
    String grammar;
    String category;
    String tags;

    FileInfo(String values[]) {
        id = Integer.parseInt(values[0]);
        symbol = values[1];
        grammar = values[2];
        category = values[3];
        if (values.length <= 5) {
            tags = "";
        } else {
            tags = values[5];
        }
    }
}
