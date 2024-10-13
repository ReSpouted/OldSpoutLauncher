package org.respouted.auth;

public class Profile {
    public static final String NULL_UUID = "000000000000000000000000000000000000000000";

    public boolean exists;
    public String uuid;
    public String username;
    //TODO skin

    public Profile() {
        exists = false;
        uuid = NULL_UUID;
        username = "Player";
    }

    public Profile(String uuid, String username) {
        exists = !NULL_UUID.equals(uuid);
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public String toString() {
        return exists ? "Username: " + username + ", uuid: " + uuid : "Non-existent player";
    }
}
