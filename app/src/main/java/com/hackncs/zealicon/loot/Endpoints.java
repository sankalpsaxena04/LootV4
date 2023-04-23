package com.hackncs.zealicon.loot;

public class Endpoints {

    //static String site = "loot-api.zealicon.in";
    static String site = "localhost:8089";

    public static String syncRequest = String.format("http://%s/api/users/",site);
    public static String register = String.format("http://%s/api/users/register/",site);
    public static String leaders = String.format("http://%s/api/users/leaderboard/",site);
    public static String fetchMission = String.format("http://%s/api/missions/",site);
    public static String updateUser = String.format("http://%s/api/users/",site);
    public static String send = String.format("http://%s/",site);
    public static String duel = String.format("http://%s/api/duels/",site);
    public static String apikey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwicGFzc3dvcmQiOiJsb290QDIwMjIiLCJpYXQiOjE1NDgzMTMyNjJ9.-UwA-73S-AkATcnuadwWh8kjslUmZXJkkDVS_-LDnxE";
}


