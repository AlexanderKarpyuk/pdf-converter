package ru.lanit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person {
    private String name;
    private List<String> keySkills = new ArrayList<>();
    private List<TableInfo> tableInfo = new ArrayList<>();
    private List<TableInfo> educationInfo;
    private Map<String, String> languages = new HashMap<>();
    private String qualifications;
    private String tests;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getKeySkills() {
        return keySkills;
    }

    public void setKeySkills(List<String> keySkills) {
        this.keySkills = keySkills;
    }

    public List<TableInfo> getJobInfo() {
        return tableInfo;
    }

    public void setJobInfo(List<TableInfo> tableInfo) {
        this.tableInfo = tableInfo;
    }

    public List<TableInfo> getEducationInfo() {
        return educationInfo;
    }

    public void setEducationInfo(List<TableInfo> educationInfo) {
        this.educationInfo = educationInfo;
    }

    public Map<String, String> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, String> languages) {
        this.languages = languages;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public String getTests() {
        return tests;
    }

    public void setTests(String tests) {
        this.tests = tests;
    }
}
