package com.example.weather.model;

public class CityData {
    private String name;
    private String id;
    private String country;
    private String adm1;
    private String adm2;

    public CityData() {}

    public CityData(String name, String id, String country, String adm1, String adm2) {
        this.name = name;
        this.id = id;
        this.country = country;
        this.adm1 = adm1;
        this.adm2 = adm2;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAdm1() { return adm1; }
    public void setAdm1(String adm1) { this.adm1 = adm1; }

    public String getAdm2() { return adm2; }
    public void setAdm2(String adm2) { this.adm2 = adm2; }

    public String getDisplayName() {
        return name + "," + adm2 + "," + adm1;
    }
}