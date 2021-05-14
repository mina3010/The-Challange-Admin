package com.example.shararaadmin.Model;

public class CategoryModel {
    private String id;
    private String name;
    private String numOfSets;
    private String setCounter;

    public CategoryModel() {
    }

    public CategoryModel(String id, String name, String numOfSets, String setCounter) {
        this.id = id;
        this.name = name;
        this.numOfSets = numOfSets;
        this.setCounter = setCounter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumOfSets() {
        return numOfSets;
    }

    public void setNumOfSets(String numOfSets) {
        this.numOfSets = numOfSets;
    }

    public String getSetCounter() {
        return setCounter;
    }

    public void setSetCounter(String setCounter) {
        this.setCounter = setCounter;
    }
}
