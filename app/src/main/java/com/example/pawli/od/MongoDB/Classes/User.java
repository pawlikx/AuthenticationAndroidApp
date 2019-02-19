package com.example.pawli.od.MongoDB.Classes;

public class User {
    private Id _id;
    private String login;
    private String password;
    private String[] favourites;
    private boolean admin;

    public Id getId() {
        return _id;
    }

    public void setId(Id _id) {
        this._id = _id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getFavourites() {
        return favourites;
    }

    public void setFavourites(String[] favourites) {
        this.favourites = favourites;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
