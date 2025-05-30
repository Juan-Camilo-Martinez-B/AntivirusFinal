package com.antivirus.Antivirus.bst;

import java.util.Date;

public class ScannedFile {
    private String filePath;
    private Date scanTime;
    private String result;
    private String category;  // Nuevo campo para la clasificación

    public ScannedFile(String filePath, Date scanTime, String result, String category) {
        this.filePath = filePath;
        this.scanTime = scanTime;
        this.result = result;
        this.category = category;
    }

    // Getter para filePath
    public String getFilePath() {
        return filePath;
    }

    public Date getScanTime() {
        return scanTime;
    }

    public String getResult() {
        return result;
    }

    public String getCategory() {
        return category;
    }

    // Setters si es que los requieres adicionalmente
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setScanTime(Date scanTime) {
        this.scanTime = scanTime;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "ScannedFile{" +
                "filePath='" + filePath + '\'' +
                ", scanTime=" + scanTime +
                ", result='" + result + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
