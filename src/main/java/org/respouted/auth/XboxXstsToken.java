package org.respouted.auth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class XboxXstsToken {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ");
    public final Date issued;
    public final Date expiry;
    public final String token;
    public final String userHash;

    public XboxXstsToken(String issued, String expiry, String token, String userHash) throws ParseException {
        this(Date.from(Instant.parse(issued)),
                Date.from(Instant.parse(expiry)),
                token,
                userHash);
    }

    public XboxXstsToken(Date issued, Date expiry, String token, String userHash) {
        this.issued = issued;
        this.expiry = expiry;
        this.token = token;
        this.userHash = userHash;
    }
}
