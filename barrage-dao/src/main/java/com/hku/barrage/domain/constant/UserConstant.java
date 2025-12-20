package com.hku.barrage.domain.constant;

import java.util.UUID;

public interface UserConstant {

    public static final String GENDER_MALE = "0";

    public static final String GENDER_FEMALE = "1";

    public static final String GENDER_UNKNOWN = "0";

    public static final String DEFAULT_BIRTHDAY = "1990-01-01";

    public static final String DEFAULT_NICK = "萌新" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
}
