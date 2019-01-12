package com.example.natha.aacquestionassistant;

public class FileInfo {
    int id;
    String symbol;

    int imageLocation;


    FileInfo(String values[]) {
        id = Integer.parseInt(values[0]);
        symbol = values[1];
        imageLocation =Integer.parseInt(values[2]);


    }

    public FileInfo(int id, String values[]){
        this.id = id;
        symbol = values[0];
        imageLocation =Integer.parseInt(values[1]);
    }

    public FileInfo(int id, String filename, int imageLocation) {
        this.id = id;
        this.symbol = filename;
        this.imageLocation = imageLocation;
    }
}
