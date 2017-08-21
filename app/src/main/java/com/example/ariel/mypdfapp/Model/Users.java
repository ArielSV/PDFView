package com.example.ariel.mypdfapp.Model;

/**
 * Created by ariel on 16/08/17.
 */

public class Users {

    private String name;
    private String email;
    private String password;
    private String photourl;

    public Users(){}

    public Users(String name, String email, String password, String photourl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.photourl = photourl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }
}
