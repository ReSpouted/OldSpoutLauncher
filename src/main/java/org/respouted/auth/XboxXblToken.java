package org.respouted.auth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class XboxXblToken {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.sz");
    public final Date issued;
    public final Date expiry;
    public final String token;
    public final String userHash;

    public XboxXblToken(String issued, String expiry, String token, String userHash) throws ParseException {
        this(Date.from(Instant.parse(issued)),
                Date.from(Instant.parse(expiry)),
                token,
                userHash);
    }

    public XboxXblToken(Date issued, Date expiry, String token, String userHash) {
        this.issued = issued;
        this.expiry = expiry;
        this.token = token;
        this.userHash = userHash;
    }
}
