package com.example.aidfirsttest1;

public class User {

    private String user_id;
    private String name;
    private String email;
    private String phonenumber;
    private String token;
    private Boolean is_hero;
    private String location;
    private String certification;
    private Boolean cert_verified;
    private String blood_type;

    public User(String id, String email, String ph, String name, Boolean isHero, String cert, Boolean certVerif, String blood, String token) {
        this.user_id = id;
        this.name = name;
        this.phonenumber = ph;
        this.email = email;
        this.is_hero = isHero;
        this.location = "";
        this.certification = cert;
        this.cert_verified = certVerif;
        this.blood_type = blood;
        this.token = token;
    }



    public User(){

    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getUser_id() {
        return user_id;
    }

    public Boolean getIs_hero() {
        return is_hero;
    }

    public String getLocation() {
        return location;
    }

    public String getCertification() {
        return certification;
    }

    public Boolean getCert_verified() {
        return cert_verified;
    }

    public String getBlood_type() {
        return blood_type;
    }
}
