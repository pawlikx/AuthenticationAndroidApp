package com.example.pawli.od.MongoDB.Engine;

import com.example.pawli.od.MongoDB.Classes.User;

public class Common {
    private static String DB_NAME = "al..kdb";

    private static String COLLECTION_USERS = "test";
    private static String COLLECTION_RECIPES = "recipes";
    private static String COLLECTION_INGREDIENTS = "ingredients";
    public static String API_KEY = "8C...j4";

    public static String getAddressSingle(User user){
        String baseUrl = String.format("https://api.mlab.com/api/1/databases/%s/collections/%s", DB_NAME, COLLECTION_USERS);
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("/"+user.getId().getOid()+"?apiKey="+API_KEY);
        return stringBuilder.toString();
    }

    public static String getAddressSingleFinal(String collection, String Oid){
        String baseUrl = String.format("https://api.mlab.com/api/1/databases/%s/collections/%s", DB_NAME, collection);
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("/"+Oid+"?apiKey="+API_KEY);
        return stringBuilder.toString();
    }

    public static String getAddressAPI(String collection){
        String baseUrl = String.format("https://api.mlab.com/api/1/databases/%s/collections/%s", DB_NAME, collection);
        StringBuilder stringBuilder = new StringBuilder(baseUrl);
        stringBuilder.append("?apiKey="+API_KEY);
        return stringBuilder.toString();
    }
}
