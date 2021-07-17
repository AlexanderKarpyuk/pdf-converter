package ru.lanit.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person {
    private String name;
    private List<String> keySkills = new ArrayList<>();
    private List<TableInfo> tableInfo = new ArrayList<>();
    private List<TableInfo> educationInfo = new ArrayList<>();
    private Map<String, String> languages = new HashMap<>();
    private List<String> qualifications = new ArrayList<>();
    private List<String> tests = new ArrayList<>();

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

    public List<String> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<String> qualifications) {
        this.qualifications = qualifications;
    }

    public List<String> getTests() {
        return tests;
    }

    public void setTests(List<String> tests) {
        this.tests = tests;
    }
}
