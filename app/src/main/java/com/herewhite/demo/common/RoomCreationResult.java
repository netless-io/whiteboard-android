package com.herewhite.demo.common;

import androidx.annotation.NonNull;

/**
 * appUUID: "Q9gKbQIeA9UtVA"
 * createdAt: "2021-12-14T02:03:21.188Z"
 * isBan: false
 * isRecord: true
 * limit: 0
 * teamUUID: "9ID20PQiEeu3O7-fBcAzOg"
 * uuid: "0338a2f05c8211ecbde4bd311350f7e4"
 */
public class RoomCreationResult {
    public String appUUID;
    public String uuid;
    public String limit;
    public boolean isRecord;

    @NonNull
    @Override
    public String toString() {
        return "CreateRoomResult{" +
                "uuid='" + uuid + '\'' +
                ", limit='" + limit + '\'' +
                ", isRecord=" + isRecord +
                '}';
    }
}
